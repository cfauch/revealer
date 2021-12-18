package com.code.fauch.revealer;

import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Tools {

    private Tools() {}

    public static void checkUserExists(final DataSource ds, final User user, long count) throws SQLException {
        try(Connection conn = ds.getConnection()) {
            try(PreparedStatement stmt =
                        conn.prepareStatement("select count(*) from horcrux_users where id=? and name=? and profile=?")) {
                stmt.setObject(1, user.getId());
                stmt.setObject(2, user.getName());
                stmt.setObject(3, user.getProfile());
                try(ResultSet result = stmt.executeQuery()) {
                    Assertions.assertTrue(result.next());
                    Assertions.assertEquals(count, result.getLong(1));
                }
            }
        }
    }

}
