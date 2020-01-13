/*
 * Copyright 2019 Claire Fauch
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
package com.code.fauch.revealer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.code.fauch.revealer.filter.BFilter;

/**
 * Base class of all DAO.
 * 
 * @author c.fauch
 *
 */
public abstract class AbsDAO<T> {
    
    private static final String SELECT = "SELECT * FROM %table%";
    private static final String SELECT_WHERE = "SELECT * FROM %table% WHERE %condition%";
    private static final String DELETE = "DELETE FROM %table% WHERE %where%";
    private static final String INSERT = "INSERT INTO %table% %fields% VALUES %values%";

    /**
     * The currently open connection.
     */
    private final Connection connection;
    
    /**
     * Constructor.
     * 
     * @param connection the currently open database connection (not null)
     */
    protected AbsDAO(final Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Find one object.
     * 
     * @return the corresponding object (or null if not found)
     * @throws SQLException
     */
    public T find() throws SQLException {
        try (PreparedStatement statement = new BRequest(SELECT).table(getTable()).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return newObject(result);
                } else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Find one object that matches the given filter.
     * 
     * @param filter the filter (not null)
     * @return the corresponding object (or null if not found)
     * @throws SQLException
     */
    public T find(final BFilter filter) throws SQLException {
        try (PreparedStatement statement = new BRequest(SELECT_WHERE).table(getTable()).where(filter).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return newObject(result);
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Find all objects.
     * 
     * @return all the corresponding objects
     * @throws SQLException
     */
    public List<T> findAll() throws SQLException {
        final List<T> events = new ArrayList<>();
        try (PreparedStatement statement = new BRequest(SELECT).table(getTable()).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final T obj = newObject(result);
                    if (obj != null) {
                        events.add(obj);
                    }
                }
            }
        }
        return events;
    }
    
    /**
     * Find all objects that matches given filter.
     * 
     * @param filter the filter (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public List<T> findAll(final BFilter filter) throws SQLException {
        final List<T> events = new ArrayList<>();
        Objects.requireNonNull(filter, "filter is mandatory");
        try (PreparedStatement statement = new BRequest(SELECT_WHERE).table(getTable()).where(filter).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final T obj = newObject(result);
                    if (obj != null) {
                        events.add(obj);
                    }
                }
            }
        }
        return events;
    }
    
    /**
     * Find all objects that matches given predicate.
     * 
     * @param predicate the predicate to apply on each object before to be return (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public List<T> findAll(final Predicate<T> predicate) throws SQLException {
        final List<T> events = new ArrayList<>();
        Objects.requireNonNull(predicate, "predicate is mandatory");
        try (PreparedStatement statement = new BRequest(SELECT).table(getTable()).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final T obj = newObject(result);
                    if (obj != null && predicate.test(obj)) {
                        events.add(obj);
                    }
                }
            }
        }
        return events;
    }
    
    /**
     * Find all objects that matches given filter and predicate.
     * 
     * @param filter the filter (not null)
     * @param predicate the predicate to apply on each object before to be return (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public List<T> findAll(final BFilter filter, final Predicate<T> predicate) throws SQLException {
        final List<T> events = new ArrayList<>();
        Objects.requireNonNull(predicate, "predicate is mandatory");
        Objects.requireNonNull(filter, "filter is mandatory");
        try (PreparedStatement statement = new BRequest(SELECT_WHERE).table(getTable()).where(filter).make(this.connection)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    final T obj = newObject(result);
                    if (obj != null && predicate.test(obj)) {
                        events.add(obj);
                    }
                }
            }
        }
        return events;
    }
    
    protected abstract String getTable();
    
    protected abstract String[] getFields();
    
    protected abstract T newObject(ResultSet result) throws SQLException;
}
