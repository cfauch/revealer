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

import com.code.fauch.revealer.IDao;
import com.code.fauch.revealer.PersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class defines Small generic DAO.
 *
 * @param <T> the type of the bean
 */
public class SmallJdbcDao<T> implements IDao<T> {

    /**
     * The jdbc connection (not null)
     */
    private final Connection connection;

    /**
     * The reader/writer factory (not null).
     */
    private final BeanRWFactory<T> rwFactory;

    /**
     * Constructor.
     * @param rwFactory the factory of bean readers and writers (not null)
     * @param conn the jdbc connection (not null)
     */
    public SmallJdbcDao(final BeanRWFactory<T> rwFactory, final Connection conn) {
        this.connection = conn;
        this.rwFactory = rwFactory;
    }

    /**
     * Insert a bean.
     * @param bean the bean to insert (not null)
     * @return newly created bean number
     * @throws PersistenceException if SQL or bean access problem.
     */
    @Override
    public final int insert(final T bean) throws PersistenceException {
        try(PreparedStatement stmt = this.connection.prepareStatement(this.rwFactory.getInsertQuery(), Statement.RETURN_GENERATED_KEYS)) {
            this.rwFactory.getWriter(stmt).write(Objects.requireNonNull(bean, "bean is mandatory"));
            final int nb = stmt.executeUpdate();
            try(ResultSet result = stmt.getGeneratedKeys()) {
                if (result.next()) {
                    this.rwFactory.getReader(stmt).updateId(bean, result.getObject(1));
                }
            }
            return nb;
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Update record using the given bean.
     * @param bean the bean to update (not null)
     * @return the updated record number
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final int update(final T bean) throws PersistenceException {
        try (PreparedStatement stmt = this.connection.prepareStatement(this.rwFactory.getUpdateQuery())) {
            this.rwFactory.getWriter(stmt).writeWithId(Objects.requireNonNull(bean, "bean is mandatory"));
            return stmt.executeUpdate();
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Delete the record corresponding to the given bean.
     * @param bean the bean to delete (not null)
     * @return the deleted record number
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final int delete(final T bean) throws PersistenceException {
        try (PreparedStatement stmt = this.connection.prepareStatement(rwFactory.getDeleteQuery())) {
            this.rwFactory.getWriter(stmt).writeId(Objects.requireNonNull(bean, "bean is mandatory"));
            int nb =  stmt.executeUpdate();
            if (nb > 0) {
                this.rwFactory.getReader(stmt).updateId(bean, null);
            }
            return nb;
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Searches and returns the bean of the given id.
     * @param id the id of the bean to research
     * @return the corresponding bean or null if not found
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final T get(final Object id) throws PersistenceException {
        try (PreparedStatement stmt = this.connection.prepareStatement(this.rwFactory.getFoundQuery())) {
            stmt.setObject(1, id);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return this.rwFactory.getReader(stmt).read(result);
                }
                return null;
            }
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Searches and returns the bean corresponding to a given SQL query.
     * @param query the SQL query (not null)
     * @param args the optional query arguments
     * @return the corresponding bean or null if not found
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final T find(final String query, final Object... args) throws PersistenceException {
        try (PreparedStatement stmt = this.connection.prepareStatement(Objects.requireNonNull(query, "query is mandatory"))) {
            for (int i = 0 ; i < args.length; i++) {
                stmt.setObject(i+1, args[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return this.rwFactory.getReader(stmt).read(result);
                }
                return null;
            }
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Search and returns beans with pagination ordered by id.
     * @param start the start id (excluded)
     * @param size the page size
     * @return the corresponding beans (it may be empty)
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final List<T> getAll(Object start, int size) throws PersistenceException {
        ArrayList<T> founds = new ArrayList<>();
        try (PreparedStatement stmt = this.connection.prepareStatement(this.rwFactory.getFoundAllQuery())) {
            stmt.setObject(1, start);
            stmt.setObject(2, size);
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    founds.add(this.rwFactory.getReader(stmt).read(result));
                }
            }
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
        return founds;
    }

    /**
     * Searches and returns beans from SQL query with pagination.
     * @param query the SQL query (not null)
     * @param args the optional query arguments
     * @return the corresponding beans (it may be empty)
     * @throws PersistenceException if SQL or bean access problem
     */
    @Override
    public final List<T> findAll(final String query, final Object... args) throws PersistenceException {
        ArrayList<T> founds = new ArrayList<>();
        try (PreparedStatement stmt = this.connection.prepareStatement(Objects.requireNonNull(query, "query is mandatory"))) {
            for (int i = 0 ; i < args.length; i++) {
                stmt.setObject(i+1, args[i]);
            }
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    founds.add(this.rwFactory.getReader(stmt).read(result));
                }
            }
        } catch (SQLException | ReflectiveOperationException err) {
            throw new PersistenceException(err);
        }
        return founds;
    }

}
