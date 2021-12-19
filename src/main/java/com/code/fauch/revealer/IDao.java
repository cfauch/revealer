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
package com.code.fauch.revealer;

import java.util.List;

/**
 * Interface describing the expected behavior of a DAO.
 *
 * @param <T>
 */
public interface IDao<T> {

    /**
     * Insert a bean.
     * @param bean the bean to insert (not null)
     * @return newly created bean number
     * @throws PersistenceException if SQL or bean access problem.
     */
    int insert(final T bean) throws PersistenceException;

    /**
     * Update record using the given bean.
     * @param bean the bean to update (not null)
     * @return the updated record number
     * @throws PersistenceException if SQL or bean access problem
     */
    int update(final T bean) throws PersistenceException;

    /**
     * Delete the record corresponding to the given bean.
     * @param bean the bean to delete (not null)
     * @return the deleted record number
     * @throws PersistenceException if SQL or bean access problem
     */
    int delete(final T bean) throws PersistenceException;

    /**
     * Searches and returns the bean of the given id.
     * @param id the id of the bean to research
     * @return the corresponding bean or null if not found
     * @throws PersistenceException if SQL or bean access problem
     */
    T get(final Object id) throws PersistenceException;

    /**
     * Searches and returns the bean corresponding to a given SQL query.
     * @param query the SQL query (not null)
     * @param args the optional query arguments
     * @return the corresponding bean or null if not found
     * @throws PersistenceException if SQL or bean access problem
     */
    T find(final String query, final Object... args) throws PersistenceException;

    /**
     * Search and returns beans with pagination ordered by id.
     * @param start the start id (excluded)
     * @param size the page size
     * @return the corresponding beans (it may be empty)
     * @throws PersistenceException if SQL or bean access problem
     */
    List<T> getAll(Object start, int size) throws PersistenceException;

    /**
     * Searches and returns beans from SQL query with pagination.
     * @param query the SQL query (not null)
     * @param args the optional query arguments
     * @return the corresponding beans (it may be empty)
     * @throws PersistenceException if SQL or bean access problem
     */
    List<T> findAll(final String query, final Object... args) throws PersistenceException;

}
