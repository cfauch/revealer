module com.code.fauch.revealer {
    requires transitive java.sql;
    requires java.desktop;
    exports com.code.fauch.revealer.jdbc;
    exports com.code.fauch.revealer;
}