# revealer
How to make SQL statements for a small project without using hibernate.

## Installation

If you use Maven just add this dependency:

```
    <dependency>
      <groupId>com.fauch.code</groupId>
      <artifactId>revealer</artifactId>
      <version>1.0.0</version>
    </dependency>
```

## Combine revealer with horcrux

By combining horcrux with developer, you will be able to not only link your java objects to the different tables of your database but also manage the upgrade of your database. Here is a complete example.

### Create your business java object

Define a ` Version` object like this:

```
public final class Version {

    private final String script;
        
    private final boolean active;
    
    private final int number;
    
    public Version(final int number, final String script, final boolean active) {
        this.number = number;
        this.active = active;
        this.script = script;
    }

    
    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }


    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }


    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }


    @Override
    public String toString() {
        return "Versions [script=" + script + ", active=" + active + ", number=" + number + "]";
    }
    
}
```

### Next, create the corresponding DAO

Define a `VersionDAO` class that inherits of `AbsDAO` like this:

```
public class VersionDAO extends AbsDAO<Version> {

    private static final String TABLE = "HORCRUX_VERSIONS";
    
    private static final String[] COLUMNS = new String[] {"number", "script", "active"};
    
    /**
     * @param connection
     */
    protected VersionDAO(Connection connection) {
        super(connection);
    }

    @Override
    protected String getTable() {
        return TABLE;
    }

    @Override
    protected String[] getColumns() {
        return COLUMNS;
    }

    @Override
    protected Version newObject(final ResultSet result) throws SQLException {
        return new Version(
                result.getInt("number"), 
                result.getString("script"), 
                result.getBoolean("active")
        );
    }

    @Override
    protected void prepareFromObject(final PreparedStatement prep, final Version version) 
            throws SQLException {
        prep.setInt(1, version.getNumber());
        prep.setString(2, version.getScript());
        prep.setBoolean(3, version.isActive());
    }

}
```

Notice that you have to define the following methods:

* `getTable`: Should returns the name of the table of the database where are stored each objects.
* `getColumns`: Is used to insert records in the database. It should returns the ordered list of columns of the table.
* `prepareFromObject`: Should complete the given `PreparedStatement` with the fields of the java object to store. The indexes used should correspond with the ones of the columns returns by the `getColumns` method.
* `newObject`: Should creates the java object from the given `ResultSet`.

### Now, use it

Use `horcrux` to create a connection pool and manages database upgrading.

```
    final Properties prop = new Properties();
    prop.setProperty("jdbcUrl", "jdbc:h2:/tmp/hx");
    prop.setProperty("username", "harry");
    prop.setProperty("password", "");
    final Path scripts = Paths.get(Main.class.getResource("/db").toURI());
    try(DataBase db = DataBase.init("pool").withScripts(scripts).versionTable("HORCRUX_VERSIONS").build(prop)) {
        db.open(ECreateOption.SCHEMA, ECreateOption.UPGRADE);
        [...]
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

## Use request with revealer

You can use `BRequest` to build and execute request.

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

### Insert request

1. Create a new `BRequest` with a sql statement template: `new BRequest("insert into %table% %columns% values %values%")`
2. Complete the `Brequest` object with:
* the `%table%` to update: `.table("HORCRUX_VERSIONS")`
* the `%colulns%`: `.columns("number", "script", "active")`
* the `%values%` is automatically completed via the columns given earlier.
3. Call `make()` with the connection to build the associated `PreparedStatement` and fill it before execution.

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

### Update request

1. Create a new `BRequest` with a sql statement template: `new BRequest("update %table% set %fields% where %condition%")`
2. Complete the `Brequest` object with:
* the `%table%`to update: `.table("HORCRUX_VERSIONS")`
* the `%fields%`to set: `fields("script", "active")`
* the `%condition%`: `.where(BFilter.isNotNull("script"))`
3. Call `make()` with the connection to build the associated `PreparedStatement` and fill it before execution.

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

## Customize your DAO using request

The abstract class `AbsDAO` provides methods that takes `BRequest` to customize your DAO:

* `put(final T object, final BRequest req)` to insert or update one object in database
* `putAll(final Collection<T> objects, final BRequest req)`: to insert several objects
* `void remove(final BRequest req)`: to remove objects
* `T get(final BRequest req)`: to retreive one object from database
* `List<T> getAll(final BRequest req)`: to retreive a list of objects

Here is how to retrieve the list all versions from database ordered by a given field.

### Update your DAO definition

1. Update your `VersionDAO` by adding the following sql request :

```
private static final String SELECT_ORDER_BY_ID = "SELECT * FROM %table% order by %field%";

```
2. Create a new method `findOrderBy` to returns all versions of the database ordered by a given field:

```
    public List<User> findOrderBy(final String field) throws SQLException {
        return getAll(new BRequest(SELECT_ORDER_BY_ID).field(field));
    }
```

### Use it

Next open a new session and call `findOrderBy` with `"script"` to obtain the list of all versions of the database ordered by the script name.

```
    try(Connection conn = db.openSession()) {
        for (Version version : new VersionDAO(conn).findOrderBy("script"))) {
            System.out.println(version);
        }
    }
```

## Revealer without horcrux

Naturally, it's also possible to use revealer without horcrux.

* With DAO:

```
    public static void main(String[] args) throws Exception {
        try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
            for (Version version : new VersionDAO(conn).findAll(BFilter.isNotNull("script"))) {
                System.out.println(version);
            }
        }
    }
```

* With request:

```
    public static void main(String[] args) throws Exception {
        try(Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/hx", "totoro", "")) {
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