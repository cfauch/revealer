package com.code.fauch.revealer;

import com.code.fauch.revealer.jdbc.transaction.Jdbc;

import java.util.List;

public interface IRaiseError {

    @Jdbc(transactional = true)
    void save(User user) throws Exception;

    @Jdbc(transactional = false)
    List<User> findAll() throws Exception;

    String fake() throws Exception;

}
