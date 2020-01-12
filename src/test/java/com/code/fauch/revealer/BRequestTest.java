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
                .where(BFilter.equal("active", false).and(BFilter.isNotNull("age")))
                .build();
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final ResultSet result = request.execute(conn);
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("porco rosso", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void testCondition() throws SQLException {
        final BRequest request = new BRequest("select * from users where %condition% order by id")
                .where(BFilter.equal("active", false).and(BFilter.isNotNull("age")))
                .build();
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final ResultSet result = request.execute(conn);
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("porco rosso", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void testTableAndThenCondition() throws SQLException {
        final BRequest request = new BRequest("select * from %table% where %condition% order by id")
                .table("users")
                .build();
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final ResultSet result = request.where(BFilter.equal("active", false).and(BFilter.isNotNull("age"))).build().execute(conn);
            int count = 0;
            while(result.next()) {
                count++;
                Assert.assertEquals("porco rosso", result.getString("name"));
            }
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void testWithoutTableAndCondition() throws SQLException {
        final BRequest request = new BRequest("select * from users order by id").build();
        try(Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", url), "totoro", "")) {
            final ResultSet result = request.execute(conn);
            int count = 0;
            while(result.next()) {
                count++;
            }
            Assert.assertEquals(4, count);
        }
    }


}