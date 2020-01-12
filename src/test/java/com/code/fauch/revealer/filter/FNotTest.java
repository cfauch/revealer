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
package com.code.fauch.revealer.filter;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.code.fauch.revealer.filter.FEq;
import com.code.fauch.revealer.filter.FIn;
import com.code.fauch.revealer.filter.FNot;

/**
 * @author c.fauch
 *
 */
public class FNotTest {

    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= FEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testNotEqString() throws URISyntaxException, SQLException {
        final FNot cmd = new FNot(new FEq<>(String.class, "name", "mathusalem"));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertNotEquals("mathusalem", result.getString("name"));
            }
            Assert.assertEquals(3, count);
        }
    }

    @Test
    public void testNotEqVoid() throws URISyntaxException, SQLException {
        final FNot cmd = new FNot(new FEq<>(Void.class, "age", null));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertNotEquals("mathusalem", result.getString("name"));
            }
            Assert.assertEquals(3, count);
        }
    }
    
    @Test
    public void testNotEqUUID() throws URISyntaxException, SQLException {
        final FNot cmd = new FNot(new FEq<>(UUID.class, "id", UUID.fromString("00000000-0000-0000-0000-000000000002")));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertNotEquals("porco rosso", result.getString("name"));
            }
            Assert.assertEquals(3, count);
        }
    }
    
    @Test
    public void testNotEqBool() throws URISyntaxException, SQLException {
        final FNot cmd = new FNot(new FEq<>(Boolean.class, "active", true));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            String[] expectedName = new String [] {"porco rosso", "mathusalem"};
            while(result.next()) {
                Assert.assertEquals(expectedName[count++], result.getString("name"));
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test
    public void testNotInString() throws SQLException {
        final String[] notExpected = new String[] {"mathusalem", "jesus"};
        final FNot cmd = new FNot(new FIn<>("VARCHAR", "name", notExpected));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            String[] expectedName = new String [] {"totoro", "porco rosso"};
            while(result.next()) {
                Assert.assertEquals(expectedName[count++], result.getString("name"));
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test
    public void testNotInUUID() throws SQLException {
        final UUID[] notExpected = new UUID[] {
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000003")
        };
        final FNot cmd = new FNot(new FIn<>("UUID", "id", notExpected));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            final UUID[] expected = new UUID[] {
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    UUID.fromString("00000000-0000-0000-0000-000000000004")
            };
            while(result.next()) {
                Assert.assertEquals(expected[count++].toString(), result.getString("id").toString());
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test
    public void testNotInBool() throws SQLException {
        final FNot cmd = new FNot(new FIn<>("BOOLEAN", "active", new Boolean[] {true}));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            final String[] expected = new String[] {"porco rosso", "mathusalem"};
            while(result.next()) {
                Assert.assertEquals(expected[count++], result.getString("name"));
                Assert.assertFalse(result.getBoolean("active"));
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void testNotNullQuery() throws URISyntaxException, SQLException {
        new FNot(null);
    }
    
}
