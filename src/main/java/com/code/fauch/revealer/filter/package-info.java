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
/**
 * This package defines filters and all operations to combine them.
 * 
 * <h3>How to build and combine filters</h3>
 * <p>
 * <code>BFilter</code> is the main class to use to create and combine filters.
 * </p>
 * <h4>Basic filters</h4>
 * <p>
 * <ul>
 * <li><b>EQUALS:</b> <code>BFilter.equals(field, value)</code> where <code>field</code> is the column to test, 
 * and <code>value</code> the expected value.</li>
 * <li><b>NOT EQUALS:</b> <code>BFilter.notEqual(field, value)</code> where <code>field</code> is the column to test,
 * and <code>value</code> the unexpected value.</li>
 * <li><b>IS NULL:</b> <code>BFilter.isNull(field)</code> where <code>field</code> is the column to test, and 
 * <code>null</code> is the expected value</li>
 * <li><b>IS NOT NULL:</b> <code>BFilter.isNotNull(field)</code> where <code>field</code> is the column to test</li>
 * <li><b>IN:</b> <code>BFilter.in(type, field, values)</code> where <code>type</code> is the sql type of the set of values, 
 * and <code>field</code> is the column to test.</li>
 * </ul>
 * </p>
 * <h4>Filter operations</h4>
 * <p>
 * <h5> AND </h5>
 * <p>
 * To make a logical 'and' between two filters.
 * <pre>
 * BFilter.equal("active", true).and(BFilter.isNull("age"))
 * </pre>
 * This combination means: <i>active = true <b>and</b> age is null</i>
 * </p>
 * <h5> OR </h5>
 * <p>
 * To make a logical 'or' between two filters.
 * <pre>
 * BFilter.equal("active", true).or(BFilter.isNull("age"));
 * </pre>
 * This combination means: <i>active = true <b>or</b> age is null</i>
 * </p>
 * <h5> NOT </h5>
 * <p>
 * It is used to reverse the logical state of its filter.
 * <pre>
 * BFilter.not(BFilter.in("VARCHAR", "name", new String[]{"harry", "dobby", "voldemort"}))
 * </pre>
 * This combination means: <i>name is not <b>not</b> in "harry", "dobby", "voldemort"</i>
 * </p>
 * </p>
 * 
 * @author c.fauch
 *
 */
package com.code.fauch.revealer.filter;
