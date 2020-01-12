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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

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
    
    /**
     * SQL template.
     */
    private final String tpl;
    
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
     * Build a new request by replacing table and condition if needed.
     * 
     * @return the new request
     */
    public BRequest build() {
        String sql = this.tpl;
        if (this.table != null) {
            sql = sql.replace(TABLE_STR, this.table);
        }
        if (this.filter != null) {
            sql = sql.replace(WHERE_STR, this.filter.sql());
        }
        return new BRequest(sql, this.filter);
    }
    
    /**
     * Execute this request through the given connection.
     * 
     * @param conn the open database connection (not null)
     * @return the result set
     * @throws SQLException
     */
    public ResultSet execute(final Connection conn) throws SQLException {
        final PreparedStatement prep = conn.prepareStatement(this.tpl);
        if (this.filter != null) {
            this.filter.prepareStatement(conn, prep);
        }
        return prep.executeQuery();
    }
    
}
