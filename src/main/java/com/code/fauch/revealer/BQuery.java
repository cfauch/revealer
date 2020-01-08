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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

import com.code.fauch.revealer.query.IQuery;
import com.code.fauch.revealer.query.QAnd;
import com.code.fauch.revealer.query.QEq;
import com.code.fauch.revealer.query.QIn;
import com.code.fauch.revealer.query.QNot;
import com.code.fauch.revealer.query.QNotEq;
import com.code.fauch.revealer.query.QNotIn;
import com.code.fauch.revealer.query.QOr;

/**
 * Query builder
 * 
 * @author c.fauch
 *
 */
public final class BQuery {
    
    /**
     * Current query.
     */
    private IQuery query;
    
    /**
     * Constructor.
     * 
     * @param query current query (not null)
     */
    private BQuery(final IQuery query) {
        this.query = query;
    }

    /**
     * Returns an SQL statement that may contain one or more '?' IN parameter placeholders.
     * 
     * @return SQL statement
     */
    public String sql() {
        return this.query.sql();
    }
    
    /**
     * Fill a pre-compiled SQL statement by replacing parameters with values.
     * 
     * @param conn the current open connection (not null)
     * @param statement the statement to complete (not null)
     * @throws SQLException
     */
    public void prepareStatement(final Connection conn, final PreparedStatement statement) 
        throws SQLException {
        this.query.prepareStatement(1, conn, statement);
    }
    
    /**
     * Build 'and' operation between current query and an other one.
     * 
     * @param query the other query (not null)
     * @return this builder
     */
    public BQuery and(final BQuery query) {
        this.query = new QAnd(this.query, Objects.requireNonNull(query, "query is mandatory").query);
        return this;
    }
    
    /**
     * Build 'or' operation between current query and an other one.
     * 
     * @param query the other query (not null)
     * @return this builder
     */
    public BQuery or(final BQuery query) {
        this.query = new QOr(this.query, Objects.requireNonNull(query, "query is mandatory").query);
        return this;
    }
    
    /**
     * Build 'not' operation with the given query
     * 
     * @param query the query (not null)
     * @return this builder
     */
    public static BQuery not(final BQuery query) {
        return new BQuery(new QNot(Objects.requireNonNull(query, "query is mandatory").query));
    }

    /**
     * Build 'is null' query.
     * 
     * @param arg the column name to test (not null)
     * @return this builder
     */
    public static BQuery isNull(final String arg) {
        return new BQuery(new QEq<Void>(Void.class, arg, null));
    }
    
    /**
     * Build 'is not null' query.
     * 
     * @param arg the column name to test (not null)
     * @return this builder
     */
    public static BQuery isNotNull(final String arg) {
        return new BQuery(new QNotEq<Void>(Void.class, arg, null));
    }
    
    /**
     * Build '=' query.
     * 
     * @param arg column name to test (not null)
     * @param value the expected value (not null)
     * @return this builder
     */
    public static BQuery equal(final String arg, final Object value) {
        return new BQuery(new QEq<Object>(Objects.requireNonNull(value, "value is mandatory").getClass(), arg, value));
    }
    
    /**
     * Build '!=' query.
     * 
     * @param arg column name to test (not null)
     * @param value the unexpected value (not null)
     * @return this builder
     */
    public static BQuery notEqual(final String arg, final Object value) {
        return new BQuery(new QNotEq<Object>(Objects.requireNonNull(value, "value is mandatory").getClass(), arg, value));
    }
    
    /**
     * Build 'in' query.
     * 
     * @param arg the name of the column to test (not null)
     * @param elt the expected values (not null)
     * @return this builder
     */
    public static BQuery in(final String sqlType, final String arg, final Object[] elt) {
        return new BQuery(new QIn<Object>(sqlType, "number", elt));
    }
    
    /**
     * Build 'not in' query.
     * 
     * @param arg the name of the column to test (not null)
     * @param elt the unexpected values (not null)
     * @return this builder
     */
    public static BQuery notIn(final String sqlType, final String arg, final Object[] elt) {
        return new BQuery(new QNotIn<Object>(sqlType, "number", elt));
    }
    
}
