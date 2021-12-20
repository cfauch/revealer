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
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Object used to write a bean on a prepared statement.
 *
 * @param <T> type of the bean
 */
final class BeanWriter<T> {

    /**
     * The jdbc prepared statement (not null)
     */
    private final PreparedStatement stmt;

    /**
     * The bean class mapping (not null)
     */
    private final BeanMapping<T> mapping;

    /**
     * Ordered list of expected columns without id.
     */
    private final List<String> columns;

    /**
     * Constructor.
     * @param stmt The jdbc statement (not null)
     * @param mapping the class mapping (not null)
     */
    BeanWriter(final PreparedStatement stmt, final BeanMapping<T> mapping) {
        this.stmt = stmt;
        this.mapping = mapping;
        this.columns = mapping.withoutIdFields().collect(Collectors.toList());
    }

    /**
     * Write a bean and values of the prepared statement.
     * @param bean the bean on to write (it may be null)
     * @param values the optional values (to write after the bean fields)
     * @throws SQLException if SQL exception
     * @throws ReflectiveOperationException if the bean is not accessible for reflexion
     */
    void write(final T bean, final Object... values) throws SQLException, ReflectiveOperationException {
        int i = 0;
        if (bean != null) {
            for (i = 0; i < columns.size(); i++) {
                this.stmt.setObject(i + 1, this.mapping.get(bean, columns.get(i)));
            }
        }
        for (Object value : values) {
            this.stmt.setObject(i+1, value);
        }
    }

    /**
     * Write the given bean with its id on the prepared statement.
     * @param bean the bean to write (not null)
     * @throws SQLException SQL exception
     * @throws ReflectiveOperationException if the bean is not accessible for reflexion
     */
    void writeWithId(final T bean) throws SQLException, ReflectiveOperationException {
        write(bean, this.mapping.get(bean));
    }

    /**
     * Write only the id of the given bean on the prepared statement.
     * @param bean the bean (not null)
     * @throws SQLException SQL Exception
     * @throws ReflectiveOperationException if the bean is not accessible for reflexion
     */
    void writeId(final T bean) throws SQLException, ReflectiveOperationException {
        this.stmt.setObject(1, this.mapping.get(bean));
    }

}
