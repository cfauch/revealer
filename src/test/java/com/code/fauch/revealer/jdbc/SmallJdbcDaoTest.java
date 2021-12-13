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

import com.code.fauch.revealer.DaoException;
import com.code.fauch.revealer.User;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SmallJdbcDaoTest {

    private static final BeanRWFactory<User> FACTORY = BeanRWFactory.from(User.class);

    private DataSource ds;

    @BeforeEach
    public void setUpClass() {
        ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v3;INIT=runscript from 'src/test/resources/dataset/init.sql';DB_CLOSE_DELAY=0",
                "harry",
                ""
        );
    }

    @AfterEach
    public void tearDown() {
        ((JdbcConnectionPool)ds).dispose();
    }

    @Test
    public void testInsert() throws SQLException, DaoException {
        final User user = new User(null, "totoro", "guest");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(1, new SmallJdbcDao<>(FACTORY, conn).insert(user));
        }
        Assertions.assertNotNull(user.getId());
        Assertions.assertEquals("totoro", user.getName());
        Assertions.assertEquals("guest", user.getProfile());
        checkUserExists(user, 1);
    }

    @Test
    public void testInsertNull() throws SQLException {
        try(Connection conn = ds.getConnection()) {
            Assertions.assertThrows(NullPointerException.class, () -> new SmallJdbcDao<>(FACTORY, conn).insert(null));
        }
    }

    @Test
    public void testInsertWhenIdNotNull() throws SQLException, DaoException {
        final User user = new User(4L, "totoro", "guest");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(1, new SmallJdbcDao<>(FACTORY, conn).insert(user));
        }
        Assertions.assertEquals(5L, user.getId());
    }

    @Test
    public void testGetAll() throws SQLException, DaoException {
        int size = 1;
        try(Connection conn = ds.getConnection()) {
            for (int i = 1;;i+=size) {
                List<User> founds = new SmallJdbcDao<>(FACTORY, conn).getAll(i, size);
                if (founds.isEmpty()) {
                    Assertions.assertEquals(5, i);
                } else {
                    Assertions.assertEquals(i, founds.get(0).getId());
                }
                if (founds.size() != size) {
                    break;
                }
            }
        }
    }

    @Test
    public void testGetAll0Size() throws SQLException, DaoException {
        try(Connection conn = ds.getConnection()) {
            List<User> founds = new SmallJdbcDao<>(FACTORY, conn).getAll(0, 0);
            Assertions.assertTrue(founds.isEmpty());
        }
    }

    @Test
    public void testGetAllNegSizeAllRecords() throws SQLException, DaoException {
        try(Connection conn = ds.getConnection()) {
            List<User> founds = new SmallJdbcDao<>(FACTORY, conn).getAll(0, -2);
            Assertions.assertEquals(4, founds.size());
        }
    }

    @Test
    public void testGetAllNegStartAllRecords() throws SQLException, DaoException {
        try(Connection conn = ds.getConnection()) {
            List<User> founds = new SmallJdbcDao<>(FACTORY, conn).getAll(-10, -2);
            Assertions.assertEquals(4, founds.size());
        }
    }

    @Test
    public void findWithNullQuery() throws SQLException {
        try(Connection conn = ds.getConnection()) {
            Assertions.assertThrows(NullPointerException.class, ()->new SmallJdbcDao<>(FACTORY, conn).find(null));
        }
    }

    @Test
    public void find() throws SQLException, DaoException {
        User user;
        try(Connection conn = ds.getConnection()) {
            user = new SmallJdbcDao<>(FACTORY, conn).find(
                    "select * from horcrux_users where mail=?",
                    "radj@yolo.com");
        }
        Assertions.assertNotNull(user);
        Assertions.assertEquals(3L, user.getId());
        Assertions.assertEquals("radj", user.getName());
        Assertions.assertEquals("guest", user.getProfile());
    }

    @Test
    public void findAllWithoutArgs() throws SQLException, DaoException {
        try(Connection conn = ds.getConnection()) {
            List<User> founds = new SmallJdbcDao<>(FACTORY, conn).findAll(
                    "select * from horcrux_users where mail is not null");
            Assertions.assertEquals(2, founds.size());
        }
    }

    @Test
    public void findAllPagination() throws SQLException, DaoException {
        List<User> users = new ArrayList<>();
        int size = 2;
        try(Connection conn = ds.getConnection()) {
            for (int i = 1;;i+=size) {
                List<User> founds = new SmallJdbcDao<>(FACTORY, conn).findAll(
                        "select * from horcrux_users where id>? and mail is not null order by id limit ?",
                        i, size);
                if (founds.size() != size) {
                    break;
                }
                users.addAll(founds);
            }
        }
        Assertions.assertEquals(2, users.size());
    }

    @Test
    public void testUpdate() throws SQLException, DaoException {
        User user = new User(3L, "sheldon", "administrator");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(1, new SmallJdbcDao<>(FACTORY, conn).update(user));
        }
        Assertions.assertEquals(3L, user.getId());
        checkUserExists(user, 1);
    }

    @Test
    public void testUpdateNullId() throws SQLException, DaoException {
        User user = new User(null, "leonard", "administrator");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(0, new SmallJdbcDao<>(FACTORY, conn).update(user));
        }
        Assertions.assertNull(user.getId());
        checkUserExists(user, 0);
    }

    @Test
    public void testUpdateNegId() throws SQLException, DaoException {
        User user = new User(-2L, "sheldon", "administrator");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(0, new SmallJdbcDao<>(FACTORY, conn).update(user));
        }
        Assertions.assertEquals(-2L, user.getId());
        checkUserExists(user, 0);
    }

    @Test
    public void testUpdateNotFound() throws SQLException, DaoException {
        User user = new User(10L, "sheldon", "administrator");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(0, new SmallJdbcDao<>(FACTORY, conn).update(user));
        }
        Assertions.assertEquals(10L, user.getId());
        checkUserExists(user, 0);
    }

    @Test
    public void testUpdateNull() throws SQLException {
        try(Connection conn = ds.getConnection()) {
            Assertions.assertThrows(NullPointerException.class, ()->new SmallJdbcDao<>(FACTORY, conn).update(null));
        }
    }

    @Test
    public void testGet() throws SQLException, DaoException {
        User user;
        try(Connection conn = ds.getConnection()) {
            user = new SmallJdbcDao<>(FACTORY, conn).get(2L);
        }
        Assertions.assertNotNull(user);
        Assertions.assertEquals(2L, user.getId());
        Assertions.assertEquals("casper", user.getName());
        Assertions.assertEquals("ghost", user.getProfile());
    }

    @Test
    public void testGetNullId() throws SQLException, DaoException {
        User user;
        try(Connection conn = ds.getConnection()) {
            user = new SmallJdbcDao<>(FACTORY, conn).get(null);
        }
        Assertions.assertNull(user);
    }

    @Test
    public void testGetNullNegId() throws SQLException, DaoException {
        User user;
        try(Connection conn = ds.getConnection()) {
            user = new SmallJdbcDao<>(FACTORY, conn).get(-1);
        }
        Assertions.assertNull(user);
    }

    @Test
    public void testDelete() throws SQLException, DaoException {
        User user = new User(4L, "silvester", "guest");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(1, new SmallJdbcDao<>(FACTORY, conn).delete(user));
        }
        Assertions.assertNull(user.getId());
        checkUserExists(user, 0);
    }

    @Test
    public void testDeleteNullId() throws SQLException, DaoException {
        User user = new User(null, "silvester", "guest");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(0, new SmallJdbcDao<>(FACTORY, conn).delete(user));
        }
        Assertions.assertNull(user.getId());
    }

    @Test
    public void testDeleteNegId() throws SQLException, DaoException {
        User user = new User(-1L, "silvester", "guest");
        try(Connection conn = ds.getConnection()) {
            Assertions.assertEquals(0, new SmallJdbcDao<>(FACTORY, conn).delete(user));
        }
        Assertions.assertEquals(-1L, user.getId());
    }

    @Test
    public void testDeleteNull() throws SQLException {
        try(Connection conn = ds.getConnection()) {
            Assertions.assertThrows(NullPointerException.class, ()->new SmallJdbcDao<>(FACTORY, conn).delete(null));
        }
    }

    private void checkUserExists(final User user, long count) throws SQLException {
        try(Connection conn = ds.getConnection()) {
            try(PreparedStatement stmt =
                        conn.prepareStatement("select count(*) from horcrux_users where id=? and name=? and profile=?")) {
                stmt.setObject(1, user.getId());
                stmt.setObject(2, user.getName());
                stmt.setObject(3, user.getProfile());
                try(ResultSet result = stmt.executeQuery()) {
                    Assertions.assertTrue(result.next());
                    Assertions.assertEquals(count, result.getLong(1));
                }
            }
        }
    }

}
