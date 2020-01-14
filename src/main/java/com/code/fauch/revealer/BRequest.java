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
import java.util.StringJoiner;

import com.code.fauch.revealer.filter.BFilter;

/**
 * Request builder.
 * 
 * @author c.fauch
 *
 */
public class BRequest {

    private static final String TABLE_STR = "%table%";
    
    private static final String WHERE_STR = "%condition%";
    
    private static final String FIELDS_STR = "%fields%";
    
    private static final String VALUES_STR = "%values%";
    
    /**
     * SQL template.
     */
    private final String tpl;
    
    /**
     * Ordered list of fields to insert.
     */
    private String[] fields;

    /**
     * Optional table.
     */
    private String table;
    
    /**
     * Optional filter.
     */
    private BFilter filter;

    /**
     * Constructor.
     * 
     * @param tpl the SQL template (not null)
     * @param filter the optional filter
     */
    private BRequest(final String tpl, final BFilter filter) {
        this.tpl = Objects.requireNonNull(tpl, "tpl is mandatory");
        this.filter = filter;
    }

    /**
     * Constructor.
     * 
     * @param tpl the SQL template (not null)
     */
    public BRequest(final String tpl) {
        this(tpl, null);
    }

    /**
     * Specify a table name.
     * 
     * @param table the name of the table 
     * @return this builder
     */
    public BRequest table(final String table) {
        this.table = table;
        return this;
    }
    
    /**
     * Specify a filter.
     * 
     * @param filter the filter
     * @return this builder
     */
    public BRequest where(final BFilter filter) {
        this.filter = filter;
        return this;
    }
    
    /**
     * Specify ordered list of field for insertion.
     * 
     * @param fields ordered list of fields
     * @return this builder
     */
    public BRequest fields(final String... fields) {
        this.fields = fields;
        return this;
    }
    
    /**
     * Make the pre-compiled SQL statement from the given connection.
     * 
     * @param conn the open database connection (not null)
     * @return The pre-compiled SQL statement
     * @throws SQLException
     */
    public PreparedStatement make(final Connection conn) throws SQLException {
        String sql = this.tpl;
        if (this.table != null) {
            sql = sql.replace(TABLE_STR, this.table);
        }
        if (this.filter != null) {
            sql = sql.replace(WHERE_STR, this.filter.sql());
        }
        if (this.fields != null) {
            final StringJoiner fjoiner = new StringJoiner(",", "(", ")");
            final StringJoiner vjoiner = new StringJoiner(",", "(", ")");
            for (String field : this.fields) {
                fjoiner.add(field);
                vjoiner.add("?");
            }
            sql = sql.replace(FIELDS_STR, fjoiner.toString()).replace(VALUES_STR, vjoiner.toString());
        }
        final PreparedStatement statement = conn.prepareStatement(sql);
        if (this.filter != null) {
            this.filter.prepareStatement(conn, statement);
        }
        return statement;
    }
    
}
