package com.code.fauch.revealer;

import com.code.fauch.revealer.jdbc.transaction.Jdbc;

import java.util.List;

public interface IService {

    @Jdbc(transactional = true)
    void save(User user) throws PersistenceException;

    @Jdbc(transactional = false)
    List<User> findAll() throws PersistenceException;

    String fake();

}
