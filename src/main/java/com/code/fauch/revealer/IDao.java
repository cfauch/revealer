package com.code.fauch.revealer;

import java.util.List;

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
