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
package com.code.fauch.revealer.query;

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

/**
 * @author c.fauch
 *
 */
public class QInTest {

    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= QEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testInString() throws SQLException {
        final String[] expected = new String[] {"mathusalem", "jesus"};
        final QIn<String> cmd = new QIn<>("VARCHAR", "name", expected);
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                Assert.assertEquals(expected[count++], result.getString("name"));
            }
            Assert.assertEquals(2, count);
        }
    }

    @Test
    public void testInUUID() throws SQLException {
        final UUID[] expected = new UUID[] {
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000003")
        };
        final QIn<UUID> cmd = new QIn<>("UUID", "id", expected);
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                Assert.assertEquals(expected[count++].toString(), result.getString("id").toString());
            }
            Assert.assertEquals(2, count);
        }
    }

    @Test
    public void testInBool() throws SQLException {
        final QIn<Boolean> cmd = new QIn<>("BOOLEAN", "active", new Boolean[] {true});
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            final String[] expected = new String[] {"totoro", "jesus"};
            while(result.next()) {
                Assert.assertEquals(expected[count++], result.getString("name"));
                Assert.assertTrue(result.getBoolean("active"));
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void testEqNullSQLType() throws URISyntaxException, SQLException {
        new QIn<>(null, "active", new Boolean[] {true});
    }

    @Test(expected = NullPointerException.class)
    public void testEqNullArg() throws URISyntaxException, SQLException {
        new QIn<>("BOOLEAN", null, new Boolean[] {true});
    }

    @Test(expected = NullPointerException.class)
    public void testEqNullElts() throws URISyntaxException, SQLException {
        new QIn<>("BOOLEAN", "active", null);
    }

}
