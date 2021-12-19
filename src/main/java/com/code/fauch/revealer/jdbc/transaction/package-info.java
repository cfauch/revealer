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
 * This package defines wrappers for DAO and services that use DAO.
 * This two wrappers make easier the transaction management.
 * All you have to do is to annotate your service interface methods with the <code>Jdbc</code> annotation with
 * <code>transaction=true</code> to execute the method within a transaction or <code>transaction=false</code>
 * otherwise.
 *
 * <h3>Create an annotated bean</h3>
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
 * </pre>
 * Annotations are used to map field to database column of a database table.
 * <ul>
 * <li><code>@Collection</code>: indicates the name of the collection or table is corresponding to the bean class</li>
 * <li><code>@Field</code>: indicates the name of the column is corresponding to the bean field</li>
 * <li><code>@Id</code>: indicates which field ot the bean is used as unique identifier</li>
 * </ul>
 *
 * <h3>Create a new interface</h3>
 * <pre>
 * public interface IService {
 *
 *     @Jdbc(transactional = true)
 *     void save(User user) throws PersistenceException;
 *
 *     @Jdbc(transactional = false)
 *     List<User> findAll() throws PersistenceException;
 *
 * }
 * </pre>
 * The <code>save</code> method will be executed within a transaction, instead of <code>findAll</code> that will
 * be executed without transaction.
 *
 * <h3>Create interface implementation</h3>
 * Create a class that implements your service like other classes.
 * <pre>
 * public class ServiceImpl implements IService {
 *
 *     private final IDao<User> dao;
 *
 *     public ServiceImpl(final IDao<User> dao) {
 *         this.dao = dao;
 *     }
 *
 *     @Override
 *     public void save(User user) throws PersistenceException {
 *         if (user.getId() == null) {
 *             this.dao.insert(user);
 *         } else {
 *             this.dao.update(user);
 *         }
 *     }
 *
 *     @Override
 *     public List<User> findAll() throws PersistenceException {
 *         final ArrayList<User> all = new ArrayList<>();
 *         final int size = 2;
 *         long from = 0L;
 *         for (;;) {
 *             final List<User> page = this.dao.getAll(from, size);
 *             all.addAll(page);
 *             if (page.size() != size) {
 *                 break;
 *             }
 *             from = page.get(page.size() - 1).getId();
 *         }
 *         return all;
 *     }
 *
 * }
 * </pre>
 * <h3>Now, build the DAO and wrapper</h3>
 * Use the <code>JdbcFactory</code> to build the user DAO and a wrapper for your service implementation
 * that will manage de jdbc connections and transactions.
 * <pre>
 *      final IDao<User> dao = JdbcFactory.dao(User.class);
 *      final DataSource ds = JdbcConnectionPool.create(
 *                 "jdbc:h2:mem:v3;INIT=runscript from 'src/test/resources/dataset/init.sql';DB_CLOSE_DELAY=0",
 *                 "harry",
 *                 ""
 *         );
 * final IService service = (IService) JdbcFactory.wrap(this.ds, new ServiceImpl(DAO));
 * </pre>
 * <h3>Finally, use it</h3>
 * Now you can call an of the service methods, the database connection and transaction will be manage
 * automatically.
 * <pre>
 *     final User user = new User(null, "totoro", "guest");
 *     this.service.save(user);
 * </pre>
 */
package com.code.fauch.revealer.jdbc.transaction;