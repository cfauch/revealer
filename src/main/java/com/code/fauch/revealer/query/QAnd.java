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
 * And query operation.
 * 
 * @author c.fauch
 *
 */
public final class QAnd extends AQOp {

    private static final String OPERATOR = "AND";
    
    /**
     * Constructor.
     * 
     * @param query1 left query (not null)
     * @param query2 right query (not null)
     */
    public QAnd(final IQuery query1, final IQuery query2) {
        super(query1, query2);
    }

    @Override
    String getOperator() {
        return OPERATOR;
    }

}
