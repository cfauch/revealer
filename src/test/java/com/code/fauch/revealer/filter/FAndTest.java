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

import com.code.fauch.revealer.filter.FAnd;
import com.code.fauch.revealer.filter.FEq;
import com.code.fauch.revealer.filter.FIn;
import com.code.fauch.revealer.filter.FNot;

/**
 * @author c.fauch
 *
 */
public class FAndTest {

    private static String url;
    
    @BeforeClass
    public static void beforeClass() throws URISyntaxException {
        url= FEqTest.class.getResource("/dataset/hx.mv.db").toURI().resolve("hx").getPath();
    }

    @Test
    public void testEqAnd() throws URISyntaxException, SQLException {
        final FAnd cmd = new FAnd(
                new FEq<>(Boolean.class, "active", true),
                new FEq<>(Void.class, "age", null)
        );
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
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
        final FAnd cmd = new FAnd(
                new FEq<>(Boolean.class, "active", true),
                new FNot(new FIn<UUID>("UUID", "id", ids))
        );
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final PreparedStatement prep = conn.prepareStatement(String.format("select * from users where %s", cmd.sql()));
            cmd.prepareStatement(1, conn, prep);
            final ResultSet result = prep.executeQuery();
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("jesus", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test(expected=NullPointerException.class)
    public void testEqAndNullQuery1() throws URISyntaxException, SQLException {
        new FAnd(
                null,
                new FEq<>(Void.class, "age", null)
        );
    }
    
    @Test(expected=NullPointerException.class)
    public void testEqAndNullQuery2() throws URISyntaxException, SQLException {
        new FAnd(
                new FEq<>(Boolean.class, "active", true),
                null
        );
    }
    
}
