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
 * Not operator implementation.
 * 
 * @author c.fauch
 *
 */
public final class FNot implements IFilter {

    private static final String TPL = "NOT(%s)";
    
    /**
     * The filter to negate.
     */
    private final IFilter filter;
    
    /**
     * Constructor.
     * 
     * @param filter the filter to negate (not null)
     */
    public FNot(final IFilter filter) {
        this.filter = Objects.requireNonNull(filter, "filter is missing");
    }
    
    /**
     * Format SQL template statement with filter to negate.
     */
    @Override
    public String sql() {
        return String.format(TPL, this.filter.sql());
    }

    /**
     * Delegate the prepared statement processing to the query.
     */
    @Override
    public int prepareStatement(final int index, final Connection conn, final PreparedStatement statement) 
            throws SQLException {
        return this.filter.prepareStatement(index, conn, statement);
    }
    
}
