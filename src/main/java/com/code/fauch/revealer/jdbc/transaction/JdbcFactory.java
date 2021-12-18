/*
 * Copyright 2021 Claire Fauch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.code.fauch.revealer.jdbc.transaction;

import com.code.fauch.revealer.PersistenceException;
import com.code.fauch.revealer.jdbc.BeanRWFactory;
import com.code.fauch.revealer.jdbc.SmallJdbcDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public final class JdbcFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcFactory.class);

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = ThreadLocal.withInitial(()->null);

    private static final class Session {

        private final DataSource ds;
        private final Transaction next;

        private Session(final DataSource ds, final Transaction next) {
            this.ds = ds;
            this.next = next;
        }

        Object eval(final Delegate delegate) throws SQLException, PersistenceException {
            if (CURRENT_CONNECTION.get() == null && delegate.needConnection()) {
                try(Connection conn = ds.getConnection()) {
                    LOGGER.info("Opening jdbc connection...");
                    CURRENT_CONNECTION.set(conn);
                    return this.next.eval(delegate);
                } finally {
                    LOGGER.info("Closing jdbc connection...");
                    CURRENT_CONNECTION.remove();
                }
            }
            return delegate.eval();
        }
    }

    private static final class Transaction {

        Object eval(final Delegate delegate) throws SQLException, PersistenceException {
            final Connection conn = CURRENT_CONNECTION.get();
            if (conn.getAutoCommit() && delegate.needTransaction()) {
                try {
                    LOGGER.info("Starting jdbc transaction...");
                    conn.setAutoCommit(false);
                    final Object result = delegate.eval();
                    LOGGER.info("Committing jdbc transaction...");
                    conn.commit();
                    return result;
                } catch (SQLException | PersistenceException err) {
                    try {
                        LOGGER.info("Rollback of the jdbc transaction");
                        conn.rollback();
                    } catch (SQLException e) {
                        LOGGER.warn("Unable to rollback the current jdbc transaction", e);
                    }
                    throw err;
                } finally {
                    conn.setAutoCommit(true);
                    LOGGER.info("End of jdbc transaction");
                }
            }
            return delegate.eval();
        }

    }

    private static final class CurrentConnection implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws PersistenceException {
            return new Delegate(CURRENT_CONNECTION.get(), method, args).eval();
        }

    }

    private static final class ServiceWrapper implements InvocationHandler {

        private final Session session;
        private final Object impl;

        private ServiceWrapper(final Session session, final Object impl) {
            this.impl = impl;
            this.session = session;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws PersistenceException {
            try {
                return this.session.eval(new Delegate(this.impl, method, args));
            } catch (SQLException err) {
                throw new PersistenceException(err);
            }
        }

    }

    private JdbcFactory() {
        //Nothing to do
    }

    public static Connection connection() {
        return (Connection) Proxy.newProxyInstance(
                JdbcFactory.class.getClassLoader(),
                new Class[]{Connection.class},
                new JdbcFactory.CurrentConnection());
    }

    public static <U> SmallJdbcDao<U> dao(final Class<U> cls) {
        return new SmallJdbcDao<>(
                BeanRWFactory.from(cls),
                connection());
    }

    public static Object wrap(final DataSource ds, final Object impl) {
        return Proxy.newProxyInstance(
                JdbcFactory.class.getClassLoader(),
                impl.getClass().getInterfaces(),
                new JdbcFactory.ServiceWrapper(
                        new Session(Objects.requireNonNull(ds, "ds is mandatory"), new Transaction()),
                        Objects.requireNonNull(impl, "impl is mandatory")));
    }

}
