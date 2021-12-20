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
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Factory used to create the bean reader and writer.
 *
 * @param <T> the type of the bean
 */
public final class BeanRWFactory<T> {

    private static final String INSERT_SQL = "insert into %s (%s) values (%s)";
    private static final String DELETE_SQL = "delete from %s where %s=?";
    private static final String UPDATE_SQL = "update %s set %s where %s=?";
    private static final String FOUND_SQL = "select * from %s where %s=?";
    private static final String FOUND_ALL_SQL = "select * from %s where %s>? order by %s limit ?";

    /**
     * The bean mapping (not null).
     */
    private final BeanMapping<T> mapping;

    /**
     * The SQL insert query (not null).
     */
    private final String insertQuery;

    /**
     * The SQL delete query (not null).
     */
    private final String deleteQuery;

    /**
     * The SQL update query (not null).
     */
    private final String updateQuery;

    /**
     * The SQL select by id query (not null).
     */
    private final String foundQuery;

    /**
     * The SQL select by id with pagination query (not null).
     */
    private final String foundAllQuery;

    /**
     * Creates a new factory for the given bean class.
     *
     * @param cls the class of the bean (not null)
     * @param <U> the type of the bean
     * @return the just created factory
     */
    public static <U> BeanRWFactory<U> from(final Class<U> cls) {
        return new BeanRWFactory<>(BeanMapping.from(Objects.requireNonNull(cls, "cls is mandatory")));
    }

    /**
     * Constructor.
     * @param mapping the bean class mapping (not null)
     */
    private BeanRWFactory(final BeanMapping<T> mapping) {
        this.mapping = mapping;
        this.insertQuery = String.format(
                INSERT_SQL,
                this.mapping.getCollection(),
                this.mapping.withoutIdFields().collect(Collectors.joining(",")),
                String.join(",", Collections.nCopies(this.mapping.size() - 1, "?")));
        this.deleteQuery = String.format(
                DELETE_SQL,
                this.mapping.getCollection(),
                this.mapping.getId());
        this.updateQuery = String.format(
                UPDATE_SQL,
                this.mapping.getCollection(),
                this.mapping.withoutIdFields().map(e->String.format("%s=?", e)).collect(Collectors.joining(",")),
                this.mapping.getId());
        this.foundQuery = String.format(
                FOUND_SQL,
                this.mapping.getCollection(),
                this.mapping.getId());
        this.foundAllQuery = String.format(
                FOUND_ALL_SQL,
                this.mapping.getCollection(),
                this.mapping.getId(),
                this.mapping.getId());
    }

    /**
     * Returns the insert query
     * @return insert query (not null)
     */
    String getInsertQuery() {
        return this.insertQuery;
    }

    /**
     * Returns the update query
     * @return update query (not null)
     */
    String getUpdateQuery() {
        return this.updateQuery;
    }

    /**
     * Returns the delete query
     * @return delete query (not null)
     */
    String getDeleteQuery() {
        return this.deleteQuery;
    }

    /**
     * Returns the select by id query
     * @return the select by id query (not null)
     */
    String getFoundQuery() {
        return this.foundQuery;
    }

    /**
     * Returns the select by id with pagination query
     * @return the select by id with pagination query (not null)
     */
    String getFoundAllQuery() {
        return this.foundAllQuery;
    }

    /**
     * Builds and returns a new bean reader for the given prepared statement
     * @param stmt the prepared statement (not null)
     * @return the new bean reader
     */
    BeanReader<T> getReader(final PreparedStatement stmt) {
        return new BeanReader<>(stmt, this.mapping);
    }

    /**
     * Builds and returns a new bean writer for the given prepared statement
     * @param stmt the prepared statement (not null)
     * @return the new bean writer
     */
    BeanWriter<T> getWriter(final PreparedStatement stmt) {
        return new BeanWriter<>(stmt, this.mapping);
    }

}
