package com.code.fauch.revealer.jdbc.transaction;

import com.code.fauch.revealer.*;
import com.code.fauch.revealer.jdbc.SmallJdbcDao;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class JdbcFactoryTest {

    private static final SmallJdbcDao<User> DAO = JdbcFactory.dao(User.class);
    private DataSource ds;
    private IService service;
    private IRaiseError error;
    private IComposition composition;

    @BeforeEach
    public void setUp() {
        this.ds = JdbcConnectionPool.create(
                "jdbc:h2:mem:v3;INIT=runscript from 'src/test/resources/dataset/init.sql';DB_CLOSE_DELAY=0",
                "harry",
                ""
        );
        this.service = (IService) JdbcFactory.wrap(this.ds, new ServiceImpl(DAO));
        this.error = (IRaiseError) JdbcFactory.wrap(this.ds, new RaiseErrorImpl());
        this.composition = (IComposition) JdbcFactory.wrap(this.ds, new CompositionImpl(this.service, this.error));
    }

    @AfterEach
    public void tearDown() {
        ((JdbcConnectionPool)this.ds).dispose();
    }

    @Test
    public void testTransactionalDirectAccess() throws SQLException, PersistenceException {
        final User user = new User(null, "totoro", "guest");
        this.service.save(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertEquals("totoro", user.getName());
        Assertions.assertEquals("guest", user.getProfile());
        Tools.checkUserExists(this.ds, user, 1);
    }

    @Test
    public void testNotTransactionalDirectAccess() throws PersistenceException {
        final List<User> users = this.service.findAll();
        Assertions.assertNotNull(users);
        Assertions.assertEquals(4, users.size());
        Assertions.assertEquals("cfauch", users.get(0).getName());
        Assertions.assertEquals("casper", users.get(1).getName());
        Assertions.assertEquals("radj", users.get(2).getName());
        Assertions.assertEquals("silvester", users.get(3).getName());
    }

    @Test
    public void testDirectAccessWithoutConnection() {
        Assertions.assertEquals("yolo", this.service.fake());
    }

    @Test
    public void testTransactionalDirectAccessWhenError() {
        final User user = new User(null, "totoro", "guest");
        Assertions.assertThrows(Exception.class, ()->this.error.save(user));
    }

    @Test
    public void testNotTransactionalDirectAccessWhenError() {
        Assertions.assertThrows(Exception.class, ()->this.error.findAll());
    }

    @Test
    public void testDirectAccessWithoutConnectionWhenError() {
        Assertions.assertThrows(Exception.class, ()->this.error.fake());
    }

    @Test
    public void testTransactionalComposition() throws SQLException {
        final User user = new User(null, "totoro", "guest");
        try {
            this.composition.save(user);
        } catch (Exception e) {
        }
        Assertions.assertNull(user.getId());
        user.setId(5L);
        Tools.checkUserExists(this.ds, user, 0);

    }

    @Test
    public void testNotTransactionalComposition() {
        Assertions.assertThrows(Exception.class, ()->this.composition.findAll());
    }

    @Test
    public void testCompositionWithoutConnection() {
        Assertions.assertThrows(Exception.class, ()->this.composition.fake());
    }

    @Test
    public void testNewDAOWhenNullClass() {
        Assertions.assertThrows(NullPointerException.class, ()->JdbcFactory.dao(null));
    }

    @Test
    public void testNewWrapperWhenNullDataSource() {
        Assertions.assertThrows(NullPointerException.class, ()->JdbcFactory.wrap(null, "test"));
    }

    @Test
    public void testNewWrapperWhenNullImplementation() {
        Assertions.assertThrows(NullPointerException.class, ()->JdbcFactory.wrap(this.ds, null));
    }

}
