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
 * This package defines reader and writer object to manage Read and Write operation on SQL database.
 * This package also define a small generic Dao named <code>SmallJdbcDao</code> implementing the DAO
 * pattern for all bean that use predefined annotations like <code>@Collection</code>, <code>Field</code>,
 * and <code>Id</code> defined by the package <code>com.code.fauch.revealer</code>.
 *
 * <h3>Example of Dao using</h3>
 * <p>
 *     Here after is an example of using <code>SmallJdbcDao</code> to inert a new record in the table
 *     <code>horcrux_users</code>. This example use the annotated object <code>User</code> defined in the documentation
 *     of the package <code>com.code.fauch.revealer</code>
 * </p>
 * <pre>
 *     private static final BeanRWFactory<User> FACTORY = BeanRWFactory.from(User.class);
 *
 *     public static void main(String[] args) throws DaoException, SQLException {
 *         final PGSimpleDataSource source = new PGSimpleDataSource();
 *         source.setUrl("jdbc:postgresql:hx");
 *         source.setUser("covid19");
 *         source.setPassword("Qvdm!");
 *         try(Connection conn = source.getConnection()) {
 *             new SmallJdbcDao<>(FACTORY, conn).insert(new User(null, "porco rosso", "guest"));
 *         }
 *     }
 * </pre>
 * <p>
 *     First, we create a factory responsible of User Dao creations.
 * </p>
 * <pre>
 *     private static final BeanRWFactory<User> FACTORY = BeanRWFactory.from(User.class);
 * </pre>
 * <p>
 *     Next, we create a connection to the SQL database.
 * </p>
 * <pre>
 *     public static void main(String[] args) throws DaoException, SQLException {
 *         final PGSimpleDataSource source = new PGSimpleDataSource();
 *         source.setUrl("jdbc:postgresql:hx");
 *         source.setUser("covid19");
 *         source.setPassword("Qvdm!");
 *         try(Connection conn = source.getConnection()) {
 *             ...
 *         }
 *     }
 * </pre>
 * <p>
 *     Finally, we create the DAO and call the <code>insert</code> method to insert a new User record
 *     to the database.
 * </p>
 * <pre>
 *     new SmallJdbcDao<>(FACTORY, conn).insert(new User(null, "porco rosso", "guest"));
 * </pre>
 * And that's it.
 *
 * @author c.fauch
 *
 */
package com.code.fauch.revealer.jdbc;
