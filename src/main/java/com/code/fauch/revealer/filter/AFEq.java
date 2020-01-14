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

import com.code.fauch.revealer.EType;

/**
 * Abstract base class for equal/not equal filter command.
 * 
 * @author c.fauch
 *
 */
public abstract class AFEq<T> implements IFilter {

    /**
     * The column name.
     */
    private final String arg;
    
    /**
     * The parameter value
     */
    private final T value;
    
    /**
     * The type of the parameter.
     */
    private final EType type;

    /**
     * Constructor.
     * 
     * @param cls the class of the parameter value (not null)
     * @param arg the column name of the parameter (not null)
     * @param value the parameter value
     */
    AFEq(final Class<? extends T> cls, final String arg, final T value) {
        this.arg = Objects.requireNonNull(arg, "arg is missing");
        this.value = value;
        this.type = EType.from(Objects.requireNonNull(cls, "cls is mising"));
    }

    /**
     * Format SQL statement template with parameter column name.
     */
    @Override
    public String sql() {
        return String.format(this.value == null ? getIsNullTpl() : getTpl(), this.arg);
    }

    /**
     * If the parameter value is not null fill statement according to parameter type.
     */
    @Override
    public int prepareStatement(final int index, final Connection conn, final PreparedStatement statement) 
            throws SQLException {
        if (this.value == null) {
            return index;
        }
        this.type.fill(statement, index, this.value);
        return index + 1;
    }
    
    /**
     * Returns the SQL statement template when value is null.
     * @return the IS NULL/IS NOT NULL template 
     */
    abstract String getIsNullTpl();
    
    /**
     * Returns the SQL statement template when value is not null.
     * 
     * @return the '=' or '!=' template.
     */
    abstract String getTpl();
    
}
