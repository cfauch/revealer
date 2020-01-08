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
 * Not equal query implementation.
 * 
 * @author c.fauch
 *
 */
public final class QNotEq<T> extends AQEq<T> {

    private static final String TPL = "%s!=?";
    
    private static final String TPL_NOT_NULL = "%s IS NOT NULL";
    
    /**
     * Constructor.
     * 
     * @param cls the class of the parameter value (not null)
     * @param arg the column name of the parameter (not null)
     * @param value the parameter value
     */
    public QNotEq(Class<? extends T> cls, String arg, T value) {
        super(cls, arg, value);
    }

    @Override
    String getIsNullTpl() {
        return TPL_NOT_NULL;
    }

    @Override
    String getTpl() {
        return TPL;
    }

}
