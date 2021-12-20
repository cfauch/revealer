/*
 * Copyright 2021 Claire Fauch
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
 * This package defines annotations and mapping objects to map java bean to generic remote collection
 * like SQL database.
 * 
 * <ul>
 * <li><code>@Collection</code>: indicates the name of the collection or table is corresponding to the bean class</li>
 * <li><code>@Field</code>: indicates the name of the column is corresponding to the bean field</li>
 * <li><code>@Id</code>: indicates which field ot the bean is used as unique identifier</li>
 * </ul>
 *
 * <h3> Example of annotated bean</h3>
 * Here after is an example of how to define a bean with annotations.
 *
 * <pre>
 * @Collection(name="horcrux_users")
 * public class User {
 *
 *     @Id
 *     @Field(name = "id")
 *     private Long id;
 *
 *     @Field(name = "name")
 *     private String name;
 *
 *     @Field(name = "profile")
 *     private String profile;
 *
 *     public User(final Long id, final String name, final String profile) {
 *         this.id = id;
 *         this.name = name;
 *         this.profile = profile;
 *     }
 *
 *     public User(final Long id) {
 *         this(id, null, null);
 *     }
 *
 *     public User() {
 *         this(null, null, null);
 *     }
 *
 *     public Long getId() {
 *         return id;
 *     }
 *
 *     public String getName() {
 *         return name;
 *     }
 *
 *     public String getProfile() {
 *         return profile;
 *     }
 *
 *     public void setId(Long id) {
 *         this.id = id;
 *     }
 *
 *     public void setName(String name) {
 *         this.name = name;
 *     }
 *
 *     public void setProfile(String profile) {
 *         this.profile = profile;
 *     }
 *
 * }
 * </pre>
 *
 * Have a look to the documentation of the package <code>com.code.fauch.revealer.jdbc</code> to
 * know how to use this bean with a small generic DAO.
 *
 * @author c.fauch
 *
 */
package com.code.fauch.revealer;
