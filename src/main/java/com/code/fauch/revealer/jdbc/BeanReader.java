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
package com.code.fauch.revealer.jdbc;

import com.code.fauch.revealer.BeanMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Object used to read a bean from SQL database record.
 *
 * @param <T> type of the bean
 */
final class BeanReader<T> {

    /**
     * The Jdbc statement (not null)
     */
    private final PreparedStatement stmt;

    /**
     * The bean class mapping (not null)
     */
    private final BeanMapping<T> mapping;

    /**
     * Constructor.
     *
     * @param stmt the Jdbc statement (not null)
     * @param mapping the bean mapping (not null)
     */
    BeanReader(final PreparedStatement stmt, final BeanMapping<T> mapping) {
        this.stmt = stmt;
        this.mapping = mapping;
    }

    /**
     * Read a bean from the given jdbc result set.
     * @param result the result set (not null)
     * @return the new instance of bean (not null)
     * @throws ReflectiveOperationException if the bean is not accessible for reflexion
     * @throws SQLException if SQL problem
     */
    T read(final ResultSet result) throws ReflectiveOperationException, SQLException {
        final ResultSetMetaData metaData = this.stmt.getMetaData();
        T lazyBean = null;
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            lazyBean = this.mapping.set(lazyBean, metaData.getColumnName(i), result.getObject(i));
        }
        return lazyBean;
    }

    /**
     * Update the id of the given bean.
     * @param bean the bean to update (not null)
     * @param value the new value of the id
     * @throws ReflectiveOperationException if the bean is not accessible for reflexion
     */
    void updateId(final T bean, final Object value) throws ReflectiveOperationException {
        this.mapping.set(bean, value);
    }

}
