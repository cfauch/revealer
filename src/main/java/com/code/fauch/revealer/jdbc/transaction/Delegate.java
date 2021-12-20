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
package com.code.fauch.revealer.jdbc.transaction;

import com.code.fauch.revealer.PersistenceException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class defines an object that encapsulate information to invoke method by reflection.
 */
final class Delegate {

    /**
     * The target object (not null)
     */
    private final Object target;

    /**
     * The method to invoke (not null)
     */
    private final Method method;

    /**
     * The method arguments (not null, it may be empty)
     */
    private final Object[] args;

    /**
     * Constructor.
     * @param target the target (not null)
     * @param method the method to invoke (not null)
     * @param args the method arguments (not null, it may be empty)
     */
    Delegate(final Object target, final Method method, final Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    /**
     * Invokes the method on the target object and returns the result.
     * @return the result (it may be null)
     * @throws PersistenceException if something when wrong during method invocation.
     */
    Object eval() throws PersistenceException {
        try {
            return this.method.invoke(this.target, this.args);
        } catch (InvocationTargetException err) {
            throw new PersistenceException(err.getCause());
        } catch (IllegalAccessException err) {
            throw new PersistenceException(err);
        }
    }

    /**
     * Determines whether this method need jdbc connection or not.
     * @return true if jdbc connection is needed, else false.
     */
    boolean needConnection() {
        return this.method.isAnnotationPresent(Jdbc.class);
    }

    /**
     * Determines whether this method need transaction or not.
     * @return true if transaction is needed, else false.
     */
    public boolean needTransaction() {
        return this.method.getAnnotation(Jdbc.class).transactional();
    }

}
