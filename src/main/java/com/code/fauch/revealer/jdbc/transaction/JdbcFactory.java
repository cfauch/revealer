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

import com.code.fauch.revealer.IDao;
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

/**
 * Main factory to build DAO and service wrappers.
 */
public final class JdbcFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcFactory.class);

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = ThreadLocal.withInitial(()->null);

    /**
     * Private inner object used to manage database connection creation.
     * (Chain of responsibility)
     */
    private static final class Session {

        /**
         * The Jdbc data source (not null)
         */
        private final DataSource ds;

        /**
         * The object database transaction management (not null).
         */
        private final Transaction next;

        /**
         * Constructor.
         * @param ds the DataSource to build database connection (not null)
         * @param next the object used to manage transaction (not null)
         */
        private Session(final DataSource ds, final Transaction next) {
            this.ds = ds;
            this.next = next;
        }

        /**
         * Evaluate the given method call.
         * @param delegate the method call object (not null)
         * @return the result of the method call.
         * @throws SQLException If something went wrong during database connection open/close
         * @throws PersistenceException if something went wrong during method evaluation
         */
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

    /**
     * Private inner object used to manage database transaction.
     * (chain of responsibility)
     */
    private static final class Transaction {

        /**
         * Evaluate the given method call.
         * @param delegate the method call (not null)
         * @return the result of the method call
         * @throws SQLException if something went wrong during transaction management.
         * @throws PersistenceException if something went wrong during method evaluation
         */
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

    /**
     * Private inner invocation handler used to route the database connection on the one on the current thread.
     */
    private static final class CurrentConnection implements InvocationHandler {

        @Override
        public Object invoke(Object o, Method method, Object[] args) throws PersistenceException {
            return new Delegate(CURRENT_CONNECTION.get(), method, args).eval();
        }

    }

    /**
     * Private inner invocation handler used to wrap interface method call with connection and within transaction
     * according to method annotation.
     */
    private static final class ServiceWrapper implements InvocationHandler {

        /**
         * The session object responsible to analyse method annotation to create or not
         * database connections and transactions (not null)
         */
        private final Session session;

        /**
         * The real implementation of the interface (not null)
         */
        private final Object impl;

        /**
         * Constructor.
         * @param session the session inner object to use (not null)
         * @param impl the real implementation to wrap (not null)
         */
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

    /**
     * No constructor.
     */
    private JdbcFactory() {
        //Nothing to do
    }

    /**
     * Creates a connection redirection on the current thread.
     * @return a connection redirection (not null)
     */
    public static Connection connection() {
        return (Connection) Proxy.newProxyInstance(
                JdbcFactory.class.getClassLoader(),
                new Class[]{Connection.class},
                new JdbcFactory.CurrentConnection());
    }

    /**
     * Creates a DAO to persist bean of the given class.
     * @param cls the class of the bean (not null)
     * @param <U> the type of the bean
     * @return the corresponding DAO (not null)
     */
    public static <U> IDao<U> dao(final Class<U> cls) {
        return new SmallJdbcDao<>(
                BeanRWFactory.from(cls),
                connection());
    }

    /**
     * Creates a wrapper of the given real object to manage database connections and transactions
     * automatically.
     * @param ds the DataSource to use to create needed connections (not null)
     * @param impl the real implementation to wrap (not null)
     * @return the just created wrapper (not null)
     */
    public static Object wrap(final DataSource ds, final Object impl) {
        return Proxy.newProxyInstance(
                JdbcFactory.class.getClassLoader(),
                impl.getClass().getInterfaces(),
                new JdbcFactory.ServiceWrapper(
                        new Session(Objects.requireNonNull(ds, "ds is mandatory"), new Transaction()),
                        Objects.requireNonNull(impl, "impl is mandatory")));
    }

}
