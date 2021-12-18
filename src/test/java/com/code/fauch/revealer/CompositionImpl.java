package com.code.fauch.revealer;

import java.util.List;

public class CompositionImpl implements IComposition {

    private final IService service;
    private final IRaiseError error;

    public CompositionImpl(final IService service, final IRaiseError error) {
        this.service = service;
        this.error = error;
    }

    @Override
    public void save(User user) throws Exception {
        try {
            this.service.save(user);
            this.error.save(user);
        } catch (Exception err) {
            user.setId(null);
            throw err;
        }
    }

    @Override
    public List<User> findAll() throws Exception {
        final List<User> all = this.service.findAll();
        all.addAll(this.error.findAll());
        return all;
    }

    @Override
    public String fake() throws Exception {
        return String.format("%s - %s", this.service.fake(), this.error.fake());
    }

}
