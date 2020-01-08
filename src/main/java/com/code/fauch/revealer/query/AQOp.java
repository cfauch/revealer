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
import java.util.Objects;

/**
 * Abstract base class for query operation.
 * 
 * @author c.fauch
 *
 */
public abstract class AQOp implements IQuery {

    private static final String TPL = "(%s) %s (%s)";
    
    /**
     * left query.
     */
    private final IQuery query1;
    
    /**
     * right query.
     */
    private final IQuery query2;
    
    /**
     * Constructor.
     * 
     * @param query1 left query (not null)
     * @param query2 right query (not null)
     */
    AQOp(final IQuery query1, final IQuery query2) {
        this.query1 = Objects.requireNonNull(query1, "query1 is mandatory");
        this.query2 = Objects.requireNonNull(query2, "query2 is mandatory");
    }
    
    /**
     * Format SQL statement template queries and operator.
     */
    @Override
    public String sql() {
        return String.format(TPL, this.query1.sql(), getOperator(), this.query2.sql());
    }

    /**
     * fill left query statement then right query statement.
     */
    @Override
    public int prepareStatement(int index, Connection conn, PreparedStatement statement) throws SQLException {
        final int next = this.query1.prepareStatement(index, conn, statement);
        return this.query2.prepareStatement(next, conn, statement); 
    }
    
    /**
     * Returns the SQL query operator
     * @return operator
     */
    abstract String getOperator();
    
}
