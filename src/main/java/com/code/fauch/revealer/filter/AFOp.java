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
 * Abstract base class for operation to combine filter command.
 * 
 * @author c.fauch
 *
 */
public abstract class AFOp implements IFilter {

    private static final String TPL = "(%s) %s (%s)";
    
    /**
     * left filter command.
     */
    private final IFilter filter1;
    
    /**
     * right filter command.
     */
    private final IFilter filter2;
    
    /**
     * Constructor.
     * 
     * @param filter1 left query (not null)
     * @param filter2 right query (not null)
     */
    AFOp(final IFilter filter1, final IFilter filter2) {
        this.filter1 = Objects.requireNonNull(filter1, "filter1 is mandatory");
        this.filter2 = Objects.requireNonNull(filter2, "filter2 is mandatory");
    }
    
    /**
     * Format SQL statement template queries and operator.
     */
    @Override
    public String sql() {
        return String.format(TPL, this.filter1.sql(), getOperator(), this.filter2.sql());
    }

    /**
     * fill left query statement then right query statement.
     */
    @Override
    public int prepareStatement(int index, Connection conn, PreparedStatement statement) throws SQLException {
        final int next = this.filter1.prepareStatement(index, conn, statement);
        return this.filter2.prepareStatement(next, conn, statement); 
    }
    
    /**
     * Returns the SQL query operator
     * @return operator
     */
    abstract String getOperator();
    
}
