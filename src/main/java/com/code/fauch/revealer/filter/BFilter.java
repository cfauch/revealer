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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Filter builder
 * 
 * @author c.fauch
 *
 */
public final class BFilter {
    
    /**
     * Current filter.
     */
    private IFilter filter;
    
    /**
     * Constructor.
     * 
     * @param filter current filter (not null)
     */
    private BFilter(final IFilter filter) {
        this.filter = filter;
    }

    /**
     * Returns an SQL statement that may contain one or more '?' IN parameter placeholders.
     * 
     * @return SQL statement
     */
    public String sql() {
        return this.filter.sql();
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
        this.filter.prepareStatement(1, conn, statement);
    }
    
    /**
     * Build 'and' operation between current filter and an other one.
     * 
     * @param query the other query (not null)
     * @return this builder
     */
    public BFilter and(final BFilter filter) {
        this.filter = new FAnd(this.filter, Objects.requireNonNull(filter, "filter is mandatory").filter);
        return this;
    }
    
    /**
     * Build 'or' operation between current filter and an other one.
     * 
     * @param query the other query (not null)
     * @return this builder
     */
    public BFilter or(final BFilter filter) {
        this.filter = new FOr(this.filter, Objects.requireNonNull(filter, "filter is mandatory").filter);
        return this;
    }
    
    /**
     * Build 'not' operation with the given filter
     * 
     * @param filter the filter (not null)
     * @return this builder
     */
    public static BFilter not(final BFilter filter) {
        return new BFilter(new FNot(Objects.requireNonNull(filter, "filter is mandatory").filter));
    }

    /**
     * Build 'is null' query.
     * 
     * @param arg the column name to test (not null)
     * @return this builder
     */
    public static BFilter isNull(final String arg) {
        return new BFilter(new FEq<Void>(Void.class, arg, null));
    }
    
    /**
     * Build 'is not null' query.
     * 
     * @param arg the column name to test (not null)
     * @return this builder
     */
    public static BFilter isNotNull(final String arg) {
        return new BFilter(new FNotEq<Void>(Void.class, arg, null));
    }
    
    /**
     * Build '=' filter.
     * 
     * @param arg column name to test (not null)
     * @param value the expected value (not null)
     * @return this builder
     */
    public static BFilter equal(final String arg, final Object value) {
        return new BFilter(new FEq<Object>(Objects.requireNonNull(value, "value is mandatory").getClass(), arg, value));
    }
    
    /**
     * Build '!=' filter.
     * 
     * @param arg column name to test (not null)
     * @param value the unexpected value (not null)
     * @return this builder
     */
    public static BFilter notEqual(final String arg, final Object value) {
        return new BFilter(new FNotEq<Object>(Objects.requireNonNull(value, "value is mandatory").getClass(), arg, value));
    }
    
    /**
     * Build 'in' filter.
     * 
     * @param arg the name of the column to test (not null)
     * @param elt the expected values (not null)
     * @return this builder
     */
    public static BFilter in(final String sqlType, final String arg, final Object[] elt) {
        return new BFilter(new FIn<Object>(sqlType, arg, elt));
    }
    
}
