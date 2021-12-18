module com.code.fauch.revealer {
    requires transitive java.sql;
    requires java.desktop;
    requires org.slf4j;
    exports com.code.fauch.revealer.jdbc.transaction;
    exports com.code.fauch.revealer;
}