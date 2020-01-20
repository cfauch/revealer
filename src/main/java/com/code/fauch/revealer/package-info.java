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
 * This package defines a request builder <code>BRequest</code> and an 
 * abstract DAO <code>AbsDAO</code>.
 * 
 * <h3> How to use request</h3>
 * <p>
 * You can create request to insert, update, select or remove records in database.
 * </p>
 * <h4> Insert request</h4>
 * <p>
 * <ol>
 * <li>Create a new BRequest with a sql statement template: 
 * <code>new BRequest("insert into %table% %columns% values %values%")</code></li>
 * <li>Complete the Brequest object with:
 * <ul>
 * <li>the <code>%table%</code> to update: <code>.table("HORCRUX_VERSIONS")</code></li>
 * <li>the <code>%colulns%</code>: <code>.columns("number", "script", "active")</code></li>
 * <li>the <code>%values%</code> is automatically completed via the columns given earlier.</li>
 * </ul>
 * <li>Call <code>make()</code> with the connection to build the associated <code>PreparedStatement</code>
 *  and fill it before to execute it.</li>
 * </ol>
 * <pre>
 *      try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
 *          try (PreparedStatement statement = new BRequest("insert into %table% %columns% values %values%")
 *                  .columns("number", "script", "active")
 *                  .table("HORCRUX_VERSIONS")
 *                  .make(conn)) {
 *              statement.setInt(1, 4);
 *              statement.setString(2, "upgrate_to_v4.sql");
 *              statement.setBoolean(3, true);
 *              statement.executeUpdate();
 *          }
 *      }
 * </pre>
 * </p>
 * <h4> Update request</h4>
 * <p>
 * <ol>
 * <li>Create a new BRequest with a sql statement template: 
 * <code>new BRequest("update %table% set %fields% where %condition%")</code></li>
 * <li>Complete the Brequest object with:
 * <ul>
 * <li>the <code>%table%</code> to update: <code>.table("HORCRUX_VERSIONS")</code></li>
 * <li>the <code>%fields%</code> to set: <code>.fields("script", "active")</code></li>
 * <li>the <code>%condition%</code> to select the records to update: <code>.where(BFilter.isNotNull("script"))</code></li>
 * </ul>
 * <li>Call <code>make()</code> with the connection to build the associated <code>PreparedStatement</code>
 *  and fill it before to execute it.</li>
 * </ol>
 * <pre>
 *      try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
 *          try (PreparedStatement statement = new BRequest("update %table% set %fields% where %condition%")
 *                  .fields("script", "active")
 *                  .table("HORCRUX_VERSIONS")
 *                  .where(BFilter.isNotNull("script"))
 *                  .make(conn)) {
 *              statement.setString(1, "upgrate_to_v4.sql");
 *              statement.setBoolean(2, true);
 *              statement.executeUpdate();
 *          }
 *      }
 * </pre>
 * </p>
 * <h4> Select request</h4>
 * <p>
 * <ol>
 * <li>Create a new BRequest with a sql statement template: 
 * <code>new BRequest("select * from %table% where %condition% order by %field%")</code></li>
 * <li>Complete the Brequest object with:
 * <ul>
 * <li>the <code>%table%</code> to update: <code>.table("HORCRUX_VERSIONS")</code></li>
 * <li>the <code>%condition%</code>:<code>.where(BFilter.isNotNull("script"))</code></li>
 * <li>the <code>%field%</code> to use with the order by:<code>.field("number")</code></li>
 * </ul>
 * <li>Call <code>make()</code> with the connection to build the associated PreparedStatement and execute the query.</li>
 * </ol>
 * <pre>
 *      try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
 *          try (PreparedStatement statement = new BRequest("select * from %table% where %condition% order by %field%")
 *                  .table("HORCRUX_VERSIONS")
 *                  .where(BFilter.isNotNull("script"))
 *                  .field("number")
 *                  .make(conn)) {
 *              try (ResultSet result = statement.executeQuery()) {
 *                  while(result.next()) {
 *                      System.out.println(
 *                              ">> version: " + result.getInt("number") 
 *                              + " >> script: " + result.getString("script")
 *                              + " >> active: " + result.getBoolean("active")
 *                      );
 *                  }
 *              }
 *          }
 *      }
 * </pre>
 * </p>
 * <h3> How to use DAO</h3>
 * <p>
 * You can create DAOs to access to your database.
 * </p>
 * <h4> Create your DAO</h4>
 * <p>
 * Define a <code>VersionDAO</code> class that inherits of <code>AbsDAO</code> like this:
 * </p>
 * <pre>
 * public class VersionDAO extends AbsDAO<Version> {
 * 
 *      private static final String TABLE = "HORCRUX_VERSIONS";
 *      private static final String[] COLUMNS = new String[] {"number", "script", "active"};
 *      
 *      protected VersionDAO(Connection connection) {
 *          super(connection);
 *      }
 *  
 *      protected String getTable() {
 *          return TABLE;
 *      }
 *  
 *      protected String[] getColumns() {
 *          return COLUMNS;
 *      }
 *  
 *      protected Version newObject(final ResultSet result) throws SQLException {
 *          return new Version(
 *                  result.getInt("number"), 
 *                  result.getString("script"), 
 *                  result.getBoolean("active")
 *          );
 *      }
 *  
 *      protected void prepareFromObject(final PreparedStatement prep, final Version version) 
 *              throws SQLException {
 *          prep.setInt(1, version.getNumber());
 *          prep.setString(2, version.getScript());
 *          prep.setBoolean(3, version.isActive());
 *      }
 *  
 * }
 * </pre>
 * <p>
 * You have to define the following methods:
 * <ul>
 * <li><code>getTable</code>: Should returns the name of the table of the database where are stored each objects.</li>
 * <li><code>getColumns</code>: Is used to insert records in the database. 
 * It should returns the ordered list of columns of the table.</li>
 * <li><code>prepareFromObject</code>: Should complete the given <code>PreparedStatement</code> with the fields 
 * of the java object to store. The indexes used should correspond with the ones of the columns returns by the 
 * <code>getColumns</code> method.</li>
 * <li><code>newObject</code>: Should creates the java object from the given <code>ResultSet</code></li>
 * </ul>
 * </p>
 * <p>
 * The abstract class <code>AbsDAO</code> provides methods that takes <code>BRequest</code> to customize your DAO:
 * <ul>
 * <li><code>put(final T object, final BRequest req)</code> to insert or update one object in database</li>
 * <li><code>putAll(final Collection<T> objects, final BRequest req)</code> to insert several objects</li>
 * <li><code>void remove(final BRequest req)</code> to remove objects</li>
 * <li><code>T get(final BRequest req)</code> to retreive one object from database</li>
 * <li><code>List<T> getAll(final BRequest req)</code> to retrieve a list of objects</li>
 * </ul>
 * </p>
 * <h4> Use it...</h4>
 * <p>
 * Open a connection and create a new DAO instance.
 * <pre>
 *     try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
 *          for (Version version : new VersionDAO(conn).findAll(BFilter.isNotNull("script"))) {
 *              System.out.println(version);
 *          }
 *      }
 * </pre>
 * </p>
 * 
 * @author c.fauch
 *
 */
package com.code.fauch.revealer;
