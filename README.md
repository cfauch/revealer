# revealer
How to make SQL statements for a small project without using hibernate.

## Installation

If you use Maven just add this dependency:

```
    <dependency>
      <groupId>com.fauch.code</groupId>
      <artifactId>revealer</artifactId>
      <version>1.0.1</version>
    </dependency>
```

If you decide to use postgresql database you have also to include this dependency:

```
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.2.7</version>
    </dependency>
```

In case of `module.info` you have to import the module with the line:
`requires com.code.fauch.revealer` and optionally if you use postgresql driver
you have also to import this module: `requires org.postgresql.jdbc`

## Getting start

### Create your business java object

Define a ` User` object like this:

```
package com.fauch.code.test.domain;

import com.code.fauch.revealer.Collection;
import com.code.fauch.revealer.Field;
import com.code.fauch.revealer.Id;

@Collection(name="horcrux_users")
public class User {

    @Id
    @Field(name = "id")
    private Long id;

    @Field(name = "name")
    private String name;

    @Field(name = "profile")
    private String profile;

    public User(final Long id, final String name, final String profile) {
        this.id = id;
        this.name = name;
        this.profile = profile;
    }

    public User(final Long id) {
        this(id, null, null);
    }

    public User() {
        this(null, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

```
Notice the use of the following annotations:
* `@Collection`: to specify the name of the corresponding sql table.
* `@Field`: to specify the name of the corresponding sql table column.
* `@Id`: to indicate the unique identifier.

### Defines the interface of your service that will manage your business object

```
package com.fauch.code.test.api;

import com.code.fauch.revealer.PersistenceException;
import com.code.fauch.revealer.jdbc.transaction.Jdbc;
import com.fauch.code.test.domain.User;

public interface IUserService {

    @Jdbc(transactional = true)
    int save(User user) throws PersistenceException;

}
```

Annotate all the method of the interface that need a database connection
with the `@Jdbc(transactional=fale)` annotation. For method that need to be executed
within transactional use `@Jdbc(transactional=true)` instead.

### Give an implementation of this interface

```
package com.fauch.code.test.core;

import com.code.fauch.revealer.IDao;
import com.code.fauch.revealer.PersistenceException;
import com.fauch.code.test.domain.User;
import com.fauch.code.test.api.IUserService;

public class UserServiceImpl implements IUserService {

    private final IDao<User> dao;

    public UserServiceImpl(final IDao<User> dao) {
        this.dao = dao;
    }

    @Override
    public int save(final User user) throws PersistenceException {
        if (user.getId() == null) {
            return this.dao.insert(user);
        } else {
            return this.dao.update(user);
        }
    }

}
```

This implementation use `IDao<User>` to persist the `User` business object created
earlier.

### Now, use it

* We first build a new DAO to persist `User` object by calling `JdbcFactory.dao(User.class)`.
* Next, we build a wrapper of the `IUserService` implementation by calling 
`JdbcFactory.wrap(source, new UserServiceImpl(DAO))`.
* Finally, we just have to invoke one of the method and a transactional or not connection will be created or not automatically.

```
    private static final IDao<User> DAO = JdbcFactory.dao(User.class);

    public static void main(String[] args) throws PersistenceException, SQLException {
        final PGSimpleDataSource source = new PGSimpleDataSource();
        source.setUrl("jdbc:postgresql:hx");
        source.setUser("totoro");
        source.setPassword("Yolo!");
        IUserService userService = (IUserService) JdbcFactory.wrap(source, new UserServiceImpl(DAO));
        final User user = new User(null, "porco rosso", "guest");
        System.out.println("Affected rows:" + userService.save(user));
        System.out.println("User id:" + user.getId());
    }
```

### Notes on module-info

If you use module-info, you have to export the package we are defined your annotated classes.
* the package containing your business object definition (`User`): `com.fauch.code.test.domain`
* The package containing your service interface (`IUserService`): `package com.fauch.code.test.api`

Here is the module-info used for this example:

```
module test {
    requires com.code.fauch.revealer;
    requires org.postgresql.jdbc;
    exports com.fauch.code.test.domain;
    exports com.fauch.code.test.api;
}
```
