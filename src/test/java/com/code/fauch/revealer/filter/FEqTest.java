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

/**
 * @author c.fauch
 *
 */
public class FEqTest {
    
    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= FEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testEqString() throws URISyntaxException, SQLException {
        final FEq<String> cmd = new FEq<>(String.class, "name", "mathusalem");
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("mathusalem", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void testEqVoid() throws URISyntaxException, SQLException {
        final FEq<Void> cmd = new FEq<>(Void.class, "age", null);
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("mathusalem", result.getString("name"));
                result.getInt("age");
                Assert.assertTrue(result.wasNull());
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void testEqUUID() throws URISyntaxException, SQLException {
        final FEq<UUID> cmd = new FEq<>(UUID.class, "id", UUID.fromString("00000000-0000-0000-0000-000000000002"));
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("porco rosso", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }
    
    @Test
    public void testEqBool() throws URISyntaxException, SQLException {
        final FEq<Boolean> cmd = new FEq<>(Boolean.class, "active", true);
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s order by id asc", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            String[] expectedName = new String [] {"totoro", "jesus"};
            while(result.next()) {
                Assert.assertEquals(expectedName[count++], result.getString("name"));
            }
            Assert.assertEquals(2, count);
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void testEqNullClass() throws URISyntaxException, SQLException {
        new FEq<>(null, "name", "mathusalem");
    }
    
    @Test(expected = NullPointerException.class)
    public void testEqNullArg() throws URISyntaxException, SQLException {
        new FEq<>(String.class, null, "mathusalem");
    }
    
}
