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

## Getting start

### Create your business java object

Define a ` User` object like this:

```
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

### Now, use it

```
    private static final BeanRWFactory<User> FACTORY = BeanRWFactory.from(User.class);

    public static void main(String[] args) throws DaoException, SQLException {
        final PGSimpleDataSource source = new PGSimpleDataSource();
        source.setUrl("jdbc:postgresql:hx");
        source.setUser("covid19");
        source.setPassword("Qvdm!");
        try(Connection conn = source.getConnection()) {
            new SmallJdbcDao<>(FACTORY, conn).insert(new User(null, "porco rosso", "guest"));
        }
    }
```

Next open a new session and use `revealer` to obtain the list of all database versions where script is not null.

```
        try(Connection conn = db.openSession()) {
            for (Version version : new VersionDAO(conn).findAll(BFilter.isNotNull("script"))) {
                System.out.println(version);
            }
        }
```

Here is the full `main` method:

```
public static void main(String[] args) throws Exception {
    final Properties prop = new Properties();
    prop.setProperty("jdbcUrl", "jdbc:h2:/tmp/hx");
    prop.setProperty("username", "harry");
    prop.setProperty("password", "");
    final Path scripts = Paths.get(Main.class.getResource("/db").toURI());
    try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
        db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
        try(Connection conn = db.openSession()) {
            for (Version version : new VersionDAO(conn).findAll(BFilter.isNotNull("script"))) {
                System.out.println(version);
            }
        }
    }
}
```

## Use requests with revealer

You can use `BRequest` to build and execute requests.

### Select request

1. Create a new `BRequest` with a sql statement template: `new BRequest("select * from %table% where %condition% order by %field%")`
2. Complete the `Brequest` object with:
* the `%table%` to update: `.table("HORCRUX_VERSIONS")`
* the `%condition%`: `.where(BFilter.isNotNull("script"))`
* the `%field%` to use with the order by: `.field("number")`
3. Call `make()` with the connection to build the associated `PreparedStatement` and execute the query.

```
try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
    db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
    try(Connection conn = db.openSession()) {
        try (PreparedStatement statement = new BRequest("select * from %table% where %condition% order by %field%")
                .table("HORCRUX_VERSIONS")
                .where(BFilter.isNotNull("script"))
                .field("number")
                .make(conn)) {
            try (ResultSet result = statement.executeQuery()) {
                while(result.next()) {
                    System.out.println(
                            ">> version: " + result.getInt("number") 
                            + " >> script: " + result.getString("script")
                            + " >> active: " + result.getBoolean("active")
                    );
                }
            }
        }
    }
}

```

### The insert request

1. Create a new `BRequest` with a sql statement template: `new BRequest("insert into %table% %columns% values %values%")`
2. Complete the `Brequest` object with:
* the `%table%` to update: `.table("HORCRUX_VERSIONS")`
* the `%colulns%`: `.columns("number", "script", "active")`
* the `%values%` is automatically completed via the columns given earlier.
3. Call `make()` with the connection to build the associated `PreparedStatement` and fill it before to execute it.

```
try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
    db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
    try(Connection conn = db.openSession()) {
        try (PreparedStatement statement = new BRequest("insert into %table% %columns% values %values%")
                .columns("number", "script", "active")
                .table("HORCRUX_VERSIONS")
                .make(conn)) {
            statement.setInt(1, 4);
            statement.setString(2, "upgrate_to_v4.sql");
            statement.setBoolean(3, true);
            statement.executeUpdate();
        }
    }
}
 ```

### The update request

1. Create a new `BRequest` with a sql statement template: `new BRequest("update %table% set %fields% where %condition%")`
2. Complete the `Brequest` object with:
* the `%table%`to update: `.table("HORCRUX_VERSIONS")`
* the `%fields%`to set: `fields("script", "active")`
* the `%condition%`: `.where(BFilter.isNotNull("script"))`
3. Call `make()` with the connection to build the associated `PreparedStatement` and fill it before to execute it.

```
try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
    db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
    try(Connection conn = db.openSession()) {
        try (PreparedStatement statement = new BRequest("update %table% set %fields% where %condition%")
                .fields("script", "active")
                .table("HORCRUX_VERSIONS")
                .where(BFilter.isNotNull("script"))
                .make(conn)) {
            statement.setString(1, "upgrate_to_v4.sql");
            statement.setBoolean(2, true);
            statement.executeUpdate();
        }
    }
}
```

## Customize your DAO using requests

The abstract class `AbsDAO` provides methods that takes `BRequest` in argument to customize your DAO:

* `put(final T object, final BRequest req)` to insert or update one object in database
* `putAll(final Collection<T> objects, final BRequest req)`: to insert several objects
* `void remove(final BRequest req)`: to remove objects
* `T get(final BRequest req)`: to retrieve one object from database
* `List<T> getAll(final BRequest req)`: to retrieve a list of objects

Here is how to retrieve the list all versions from database ordered by a given field.

### Update your DAO definition

1. Update your `VersionDAO` by adding the following sql request template:

```
private static final String SELECT_ORDER_BY_ID = "SELECT * FROM %table% order by %field%";

```
2. Create a new method `findOrderBy` to return all versions of the database ordered by a given field:

```
    public List<User> findOrderBy(final String field) throws SQLException {
        return getAll(new BRequest(SELECT_ORDER_BY_ID).field(field));
    }
```

### Use it

Next open a new session and call `findOrderBy` with `"script"` to obtain the list of all versions of the database ordered by the script.

```
    try(Connection conn = db.openSession()) {
        for (Version version : new VersionDAO(conn).findOrderBy("script"))) {
            System.out.println(version);
        }
    }
```
