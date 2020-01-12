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
 * Abstract base class for in/notin filter command.
 * 
 * @author c.fauch
 *
 */
public abstract class AFIn<T> implements IFilter {

    /**
     * In values.
     */
    private final T[] elts;
    
    /**
     * The column name.
     */
    private final String arg;
        
    /**
     * The SQL type of the column
     */
    private final String sqlType;

    /**
     * Constructor.
     * 
     * @param sqlType SQL type of the column (not null)
     * @param arg the column name of the parameter (not null)
     * @param elts the values (not null)
     */
    AFIn(final String sqlType, final String arg, final T[] elts) {
        this.elts = Objects.requireNonNull(elts, "elts is missing");
        this.arg = Objects.requireNonNull(arg, "arg is missing");
        this.sqlType = Objects.requireNonNull(sqlType, "sqlType is missing");
    }
    
    /**
     * Format SQL statement template with parameter column name.
     */
    @Override
    public String sql() {
        return String.format(getTpl(), this.arg);
    }
    
    @Override
    public int prepareStatement(int index, Connection conn, PreparedStatement statement) throws SQLException {
        statement.setArray(index, conn.createArrayOf(this.sqlType, this.elts));
        return index + 1;
    }
    
    /**
     * Returns the SQL statement template.
     * @return SQL template
     */
    abstract String getTpl();
    
}
