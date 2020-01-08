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

/**
 * Not in query implementation.
 * 
 * @author c.fauch
 *
 */
public final class QNotIn<T> extends AQIn<T> {

    private static final String TPL = "%s!=ANY(?)";
    
    /**
     * Constructor.
     * 
     * @param sqlType SQL type of the column (not null)
     * @param arg the column name of the parameter (not null)
     * @param elts the values (not null)
     */
    public QNotIn(String sqlType, String arg, T[] elts) {
        super(sqlType, arg, elts);
    }

    @Override
    String getTpl() {
        return TPL;
    }

}
