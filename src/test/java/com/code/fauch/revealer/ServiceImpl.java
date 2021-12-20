package com.code.fauch.revealer;

import java.util.ArrayList;
import java.util.List;

public class ServiceImpl implements IService {

    private final IDao<User> dao;

    public ServiceImpl(final IDao<User> dao) {
        this.dao = dao;
    }

    @Override
    public void save(User user) throws PersistenceException {
        if (user.getId() == null) {
            this.dao.insert(user);
        } else {
            this.dao.update(user);
        }
    }

    @Override
    public List<User> findAll() throws PersistenceException {
        final ArrayList<User> all = new ArrayList<>();
        final int size = 2;
        long from = 0L;
        for (;;) {
            final List<User> page = this.dao.getAll(from, size);
            all.addAll(page);
            if (page.size() != size) {
                break;
            }
            from = page.get(page.size() - 1).getId();
        }
        return all;
    }

    @Override
    public String fake() {
        return "yolo";
    }
}
