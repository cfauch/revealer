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

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.code.fauch.revealer.filter.BFilter;
import com.code.fauch.revealer.filter.FEqTest;

/**
 * @author c.fauch
 *
 */
public class AbsDAOTest {

    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= FEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    private final static class User {
        
        private final UUID id;
        
        private final String name;
        
        private final boolean active; 
        
        private final Integer age;
        
        private User(final UUID id, final String name, final boolean active, final Integer age) {
            this.id = id;
            this.name = name;
            this.active = active;
            this.age = age;
        }
        
    }
    
    private final static class UserDAO extends AbsDAO<User> {
        
        private static final String TABLE = "users";
        
        private static final String[] FIELDS = new String[] {"id", "name", "active", "age"};
        
        private UserDAO(final Connection connection) {
            super(connection);
        }

        @Override
        protected String getTable() {
            return TABLE;
        }

        @Override
        protected String[] getFields() {
            return FIELDS;
        }

        @Override
        protected User newObject(final ResultSet result) throws SQLException {
            Integer age = result.getInt("age");
            if (result.wasNull()) {
                age = null;
            }
            return new User(
                    (UUID)result.getObject("id"), 
                    result.getString("name"),
                    result.getBoolean("active"), 
                    age);
        }
        
    }
    
    @Test
    public void testFindAll() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final List<User> users = new UserDAO(conn).findAll();
            Assert.assertEquals(4, users.size());
        }
    }
    
    @Test
    public void testFindAllWithPredicate() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final List<User> users = new UserDAO(conn).findAll(u -> u.name.endsWith("o"));
            Assert.assertEquals(2, users.size());
            for (User user : users) {
                Assert.assertTrue(user.name.endsWith("o"));
            }
        }
    }

    @Test(expected=NullPointerException.class)
    public void testFindAllWithNullPredicate() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            new UserDAO(conn).findAll((Predicate<User>)null);
        }
    }

    @Test
    public void testFindAllWithFilter() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final List<User> users = new UserDAO(conn).findAll(BFilter.isNotNull("age"));
            Assert.assertEquals(3, users.size());
            for (User user : users) {
                if (!user.name.endsWith("o")) {
                    Assert.assertEquals("jesus", user.name);
                }
                Assert.assertNotNull(user.age);
            }
        }
    }

    @Test
    public void testFindWithFilter() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final User user = new UserDAO(conn).find(BFilter.isNull("age"));
            Assert.assertNull(user.age);
            Assert.assertFalse(user.active);
            Assert.assertEquals("mathusalem", user.name);
            Assert.assertEquals("00000000-0000-0000-0000-000000000003", user.id.toString());
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void testFindAllWithNullFilter() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            new UserDAO(conn).findAll((BFilter)null);
        }
    }

    @Test
    public void testFindAllWithFilterAndPredicate() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final List<User> users = new UserDAO(conn).findAll(BFilter.isNotNull("age"), u -> u.name.endsWith("o"));
            Assert.assertEquals(2, users.size());
            for (User user : users) {
                Assert.assertTrue(user.name.endsWith("o"));
                Assert.assertNotNull(user.age);
            }
        }
    }

    @Test(expected=NullPointerException.class)
    public void testFindAllWithFilterAndNullPredicate() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            new UserDAO(conn).findAll(BFilter.isNotNull("age"), null);
        }
    }

    @Test(expected=NullPointerException.class)
    public void testFindAllWithNullFilterAndPredicate() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            new UserDAO(conn).findAll(null, u -> u.name.endsWith("o"));
        }
    }

}
