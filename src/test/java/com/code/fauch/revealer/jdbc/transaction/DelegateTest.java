package com.code.fauch.revealer.jdbc.transaction;

import com.code.fauch.revealer.IService;
import com.code.fauch.revealer.PersistenceException;
import com.code.fauch.revealer.ServiceImpl;
import com.code.fauch.revealer.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DelegateTest {

    @Test
    public void needConnectionTrue() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("save", User.class), new Object[0]);
        Assertions.assertTrue(delegate.needConnection());
    }

    @Test
    public void needConnectionFalse() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("fake"), new Object[0]);
        Assertions.assertFalse(delegate.needConnection());
    }

    @Test
    public void needTransactionTrue() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("save", User.class), new Object[0]);
        Assertions.assertTrue(delegate.needTransaction());
    }

    @Test
    public void needTransactionFalse() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("findAll"), new Object[0]);
        Assertions.assertTrue(delegate.needConnection());
        Assertions.assertFalse(delegate.needTransaction());
    }

    @Test
    public void needTransactionNoNeedConnection() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("fake"), new Object[0]);
        Assertions.assertThrows(NullPointerException.class, delegate::needTransaction);
    }

    @Test
    public void evalTest() throws NoSuchMethodException, PersistenceException {
        final ServiceImpl impl = new ServiceImpl(null);
        final Delegate delegate = new Delegate(impl, IService.class.getMethod("fake"), new Object[0]);
        Assertions.assertEquals("yolo", delegate.eval());
    }

    @Test
    public void evalWhenNullTarget() throws NoSuchMethodException {
        final Delegate delegate = new Delegate(null, IService.class.getMethod("fake"), new Object[0]);
        Assertions.assertThrows(NullPointerException.class, delegate::eval);
    }

    @Test
    public void evalWhenNullMethod() {
        final ServiceImpl impl = new ServiceImpl(null);
        final Delegate delegate = new Delegate(impl, null, new Object[0]);
        Assertions.assertThrows(NullPointerException.class, delegate::eval);
    }

}
