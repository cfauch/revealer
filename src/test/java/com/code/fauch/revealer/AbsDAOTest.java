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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
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

        @Override
        public String toString() {
            return "User [id=" + id + ", name=" + name + ", active=" + active + ", age=" + age + "]";
        }
        
    }
    
    private final static class UserDAO extends AbsDAO<User> {
        
        private static final String SELECT_ORDER_BY_ID = "SELECT * FROM %table% order by %field%";
        
        private static final String TABLE = "users";
        
        private static final String[] COLULMNS = new String[] {"id", "name", "age", "active"};
        
        private UserDAO(final Connection connection) {
            super(connection);
        }

        public List<User> findOrderBy(final String field) throws SQLException {
            return getAll(new BRequest(SELECT_ORDER_BY_ID).field(field));
        }
        
        @Override
        protected String getTable() {
            return TABLE;
        }

        @Override
        protected String[] getColumns() {
            return COLULMNS;
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

        @Override
        protected void prepareFromObject(final PreparedStatement prep, final User object) throws SQLException {
            prep.setObject(1, object.id);
            prep.setString(2, object.name);
            if (object.age == null) {
                prep.setNull(3, Types.INTEGER);
            } else {
                prep.setInt(3, object.age);
            }
            prep.setBoolean(4, object.active);
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

    @Test
    public void testFindWithFilterNotFound() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final User user = new UserDAO(conn).find(BFilter.isNull("age").and(BFilter.equal("name", "jesus")));
            Assert.assertNull(user);
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

    @Test
    public void testInsert() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).insert(new User(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Hérode", true, 69));
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                    }
                    Assert.assertEquals(5, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void testInsertNull() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).insert(null);
        } finally {
            conn.rollback();
        }
    }
    
    @Test
    public void testInsertAll() throws SQLException {
        final User herode = new User(UUID.fromString("00000000-0000-0000-0000-000000000005"), "Hérode", true, 69);
        final User calvin = new User(UUID.fromString("00000000-0000-0000-0000-000000000006"), "Calvin", true, 6);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).insertAll(Arrays.asList(herode, calvin));
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                    }
                    Assert.assertEquals(6, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void testInsertAllNull() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).insertAll(null);
        } finally {
            conn.rollback();
        }
    }
    
    @Test
    public void testDeleteAll() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).deleteAll();
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                    }
                    Assert.assertEquals(0, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }
    
    @Test
    public void testDelete() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).delete(BFilter.isNull("age"));
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                        Assert.assertNotEquals("mathusalem", result.getString("name"));
                    }
                    Assert.assertEquals(3, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void testDeleteWithNullFilter() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).delete(null);
        } finally {
            conn.rollback();
        }
    }
    
    @Test
    public void testUpdate() throws SQLException {
        final User mathusalem = new User(UUID.fromString("00000000-0000-0000-0000-000000000003"), "mathusalem", true, 969);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            new UserDAO(conn).update(BFilter.isNull("age"), mathusalem);
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                        if (result.getObject("id").equals("00000000-0000-0000-0000-000000000003")) {
                            Assert.assertEquals("methusalem", result.getString("name"));
                            Assert.assertTrue(result.getBoolean("active"));
                            Assert.assertEquals(969, result.getInt("age"));
                        }
                    }
                    Assert.assertEquals(4, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }
    
    @Test
    public void testFindOrderByName() throws SQLException {
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final List<User> users = new UserDAO(conn).findOrderBy("name");
            Assert.assertEquals(4, users.size());
        }
    }
}
