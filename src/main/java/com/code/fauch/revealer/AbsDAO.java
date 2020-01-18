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
import java.util.Collection;
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
    private static final String DELETE_WHERE = "DELETE FROM %table% WHERE %condition%";
    private static final String DELETE = "DELETE FROM %table%";
    private static final String INSERT = "INSERT INTO %table% %columns% VALUES %values%";
    private static final String UPDATE = "UPDATE %table% SET %fields% WHERE %condition%";

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
     * Used to insert or update one object into database.
     * 
     * @param object the object (not null)
     * @param req the request to insert or update object (not null)
     * @throws SQLException
     */
    protected final void put(final T object, final BRequest req) throws SQLException {
        try(PreparedStatement statement = req.table(getTable()).make(this.connection)) {
            prepareFromObject(statement, Objects.requireNonNull(object, "object is mandatory"));
            statement.executeUpdate();
        }
    }

    /**
     * Used to insert a collection of objects into database
     * 
     * @param objects the collections (not null)
     * @param req the request to insert each object (not null)
     * @throws SQLException
     */
    protected final void putAll(final Collection<T> objects, final BRequest req) throws SQLException {
        try(PreparedStatement statement = req.table(getTable()).make(this.connection)) {
            for (T obj : Objects.requireNonNull(objects, "objects is mandatory")) {
                if (obj != null) {
                    prepareFromObject(statement, obj);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }
    
    /**
     * Used to remove objects from database.
     * 
     * @param req the request to apply to remove objects (not null)
     * @throws SQLException
     */
    protected final void remove(final BRequest req) throws SQLException {
        try (PreparedStatement statement = req.table(getTable()).make(this.connection)) {
            statement.executeUpdate();
        }
    }
    
    /**
     * Used to retrieve one object from database.
     * 
     * @param req the request to apply to find the object (not null).
     * @return the corresponding object
     */
    protected final T get(final BRequest req) throws SQLException {
        try (PreparedStatement statement = req.table(getTable()).make(this.connection)) {
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
     * Used to retrieve a list of objects from database.
     * 
     * @param req the request to apply to find all the objects (not null) 
     * @return the corresponding objects
     * @throws SQLException
     */
    protected final List<T> getAll(final BRequest req) throws SQLException {
        final List<T> events = new ArrayList<>();
        try (PreparedStatement statement = req.table(getTable()).make(this.connection)) {
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
     * Used to retrieve a list of objects from database that matches a predicate.
     * 
     * @param req the request to apply to find all the objects (not null)
     * predicate the predicate to test each object before to be return (not null) 
     * @return the corresponding objects
     * @throws SQLException
     */
    protected final List<T> getAll(final BRequest req, final Predicate<T> predicate) throws SQLException {
        Objects.requireNonNull(predicate, "predicate is mandatory");
        final List<T> events = new ArrayList<>();
        try (PreparedStatement statement = req.table(getTable()).make(this.connection)) {
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
     * Insert object into database.
     * 
     * @param object object to insert(not null)
     * @throws SQLException
     */
    public final void insert(final T object) throws SQLException {
        put(object, new BRequest(INSERT).columns(getColumns()));
    }
    
    /**
     * Insert object into database.
     * 
     * @param object object to insert(not null)
     * @throws SQLException
     */
    public final void insertAll(final Collection<T> objects) throws SQLException {
        putAll(objects, new BRequest(INSERT).columns(getColumns()));
    }
    
    public final void update(final BFilter filter, final T object) throws SQLException {
        put(object, new BRequest(UPDATE).set(getColumns()).where(filter));
    }
    
    /**
     * Delete all objects from database.
     * 
     * @throws SQLException
     */
    public final void deleteAll() throws SQLException {
        remove(new BRequest(DELETE));
    }

    /**
     * Delete all objects from database that matches the given filer.
     * 
     * @param filter the filter (not null)
     * @throws SQLException
     */
    public final void delete(final BFilter filter) throws SQLException {
        remove(new BRequest(DELETE_WHERE).where(Objects.requireNonNull(filter, "filter is mandatory")));
    }
    
    /**
     * Find one object that matches the given filter.
     * 
     * @param filter the filter (not null)
     * @return the corresponding object (or null if not found)
     * @throws SQLException
     */
    public final T find(final BFilter filter) throws SQLException {
        return get(new BRequest(SELECT_WHERE).where(Objects.requireNonNull(filter, "filter is mandatory")));
    }

    /**
     * Find all objects.
     * 
     * @return all the corresponding objects
     * @throws SQLException
     */
    public final List<T> findAll() throws SQLException {
        return getAll(new BRequest(SELECT));
    }
    
    /**
     * Find all objects that matches given filter.
     * 
     * @param filter the filter (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public final List<T> findAll(final BFilter filter) throws SQLException {
        return getAll(new BRequest(SELECT_WHERE).where(Objects.requireNonNull(filter, "filter is mandatory")));
    }
    
    /**
     * Find all objects that matches given predicate.
     * 
     * @param predicate the predicate to apply on each object before to be return (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public final List<T> findAll(final Predicate<T> predicate) throws SQLException {
        return getAll(new BRequest(SELECT), predicate);
    }
    
    /**
     * Find all objects that matches given filter and predicate.
     * 
     * @param filter the filter (not null)
     * @param predicate the predicate to apply on each object before to be return (not null)
     * @return all the corresponding objects
     * @throws SQLException
     */
    public final List<T> findAll(final BFilter filter, final Predicate<T> predicate) throws SQLException {
        return getAll(new BRequest(SELECT).where(Objects.requireNonNull(filter, "filter is mandatory")), predicate);
    }
    
    /**
     * Returns the name of the table.
     * 
     * @return the name of the table.
     */
    protected abstract String getTable();
    
    /**
     * Returns the ordered list of table columns for insertion
     * 
     * @return the ordered list of table columns
     */
    protected abstract String[] getColumns();
    
    /**
     * Creates and returns the object corresponding to result set.
     * 
     * @param result the result set (not null)
     * @return the corresponding object
     * @throws SQLException
     */
    protected abstract T newObject(ResultSet result) throws SQLException;
    
    /**
     * Prepares given statement with the corresponding object.
     * 
     * @param prep the prepared statement to fill (not null)
     * @param object the corresponding object (not null)
     * @throws SQLException
     */
    protected abstract void prepareFromObject(PreparedStatement prep, T object) throws SQLException;
    
}
