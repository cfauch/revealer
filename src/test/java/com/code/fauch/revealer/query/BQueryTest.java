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
public class BQueryTest {
    
    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= QEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testEqAnd() throws URISyntaxException, SQLException {
        final BQuery req = BQuery.equal("active", true).and(BQuery.isNull("age"));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", req.sql()));
            req.prepareStatement(conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
            }
            Assert.assertEquals(0, count);
        }
    }
    
    @Test
    public void testEqAndNotIn() throws URISyntaxException, SQLException {
        final UUID[] ids = new UUID[] {
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000003")
        };
        final BQuery req = BQuery.equal("active", true).and(BQuery.not(BQuery.in("UUID", "id", ids)));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", req.sql()));
            req.prepareStatement(conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("jesus", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }
    
    @Test
    public void testEqOr() throws URISyntaxException, SQLException {
        final BQuery req = BQuery.equal("active", true).or(BQuery.isNull("age"));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", req.sql()));
            req.prepareStatement(conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            final String[] expected = new String[] {"totoro", "mathusalem", "jesus"};
            while(result.next()) {
                Assert.assertEquals(expected[count++], result.getString("name"));
            }
            Assert.assertEquals(3, count);
        }
    }
    
    @Test
    public void testEqOrNotIn() throws URISyntaxException, SQLException {
        final UUID[] ids = new UUID[] {
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UUID.fromString("00000000-0000-0000-0000-000000000002"),
                UUID.fromString("00000000-0000-0000-0000-000000000003")
        };
        final BQuery req = BQuery.equal("active", false).or(BQuery.not(BQuery.in("UUID", "id", ids)));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", req.sql()));
            req.prepareStatement(conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            final String[] expected = new String[] {"porco rosso", "mathusalem", "jesus"};
            while(result.next()) {
                Assert.assertEquals(expected[count++], result.getString("name"));
            }
            Assert.assertEquals(3, count);
        }
    }
    
}
