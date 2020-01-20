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
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.code.fauch.revealer.filter.BFilter;
import com.code.fauch.revealer.filter.FEqTest;

/**
 * @author c.fauch
 *
 */
public class BRequestTest {

    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= FEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testTableAndCondition() throws SQLException {
        final BRequest request = new BRequest("select * from %table% where %condition% order by id")
                .table("users")
                .where(BFilter.equal("active", false).and(BFilter.isNotNull("age")));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            try (PreparedStatement statement = request.make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                        Assert.assertEquals("porco rosso", result.getString("name"));
                    }
                    Assert.assertEquals(1, count);
                }
            }
        }
    }

    @Test
    public void testCondition() throws SQLException {
        final BRequest request = new BRequest("select * from users where %condition% order by %field%").field("id")
                .where(BFilter.equal("active", false).and(BFilter.isNotNull("age")));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            try (PreparedStatement statement = request.make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                        Assert.assertEquals("porco rosso", result.getString("name"));
                    }
                    Assert.assertEquals(1, count);
                }
            }
        }
    }

    @Test
    public void testWithoutTableAndCondition() throws SQLException {
        final BRequest request = new BRequest("select * from users order by id");
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            try (PreparedStatement statement = request.make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                    }
                    Assert.assertEquals(4, count);
                }
            }
        }
    }

    @Test
    public void testInsert() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            try (PreparedStatement statement = new BRequest("insert into %table% %columns% values %values%")
                    .columns("id", "name", "age", "active")
                    .table("users")
                    .make(conn)) {
                statement.setObject(1, UUID.fromString("00000000-0000-0000-0000-000000000005"));
                statement.setString(2, "Hérode");
                statement.setNull(3, Types.INTEGER);
                statement.setBoolean(4, true);
                statement.executeUpdate();
            }
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
    
    @Test
    public void testUpdate() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "");
            conn.setAutoCommit(false);
            try (PreparedStatement statement = new BRequest("update %table% set %fields% where %condition%")
                    .table("users")
                    .where(BFilter.isNull("age"))
                    .set("age")
                    .make(conn)) {
                statement.setInt(1, 969);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = new BRequest("select * from users order by id").make(conn)) {
                try (ResultSet result = statement.executeQuery()) {
                    int count = 0;
                    while(result.next()) {
                        count++;
                        result.getInt("age");
                        Assert.assertFalse(result.wasNull());
                    }
                    Assert.assertEquals(4, count);
                }
            }
        } finally {
            conn.rollback();
        }
    }

}
