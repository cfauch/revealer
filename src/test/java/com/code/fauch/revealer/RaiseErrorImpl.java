package com.code.fauch.revealer;

import java.util.List;

public class RaiseErrorImpl implements IRaiseError {

    @Override
    public void save(User user) throws Exception {
        throw new Exception("SAVE !!");
    }

    @Override
    public List<User> findAll() throws Exception {
        throw new Exception("FIND ALL !!");
    }

    @Override
    public String fake() throws Exception {
        throw new Exception("FAKE !!");
    }

}
