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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface describing the expected behavior of a query.
 * 
 * @author c.fauch
 *
 */
public interface IQuery {

    /**
     * Returns an SQL statement that may contain one or more '?' IN parameter placeholders.
     * 
     * @return SQL statement
     */
    public String sql();

    /**
     * Fill a pre-compiled SQL statement by replacing parameters with values.
     * 
     * @param index The index of the parameter to replace in the query statement
     * @param conn the current open connection (not null)
     * @param statement the statement to complete (not null)
     * @return next index
     * @throws SQLException
     */
    public int prepareStatement(final int index, final Connection conn, final PreparedStatement statement) 
            throws SQLException;

}
