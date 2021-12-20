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
package com.code.fauch.revealer;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class defines an object to map a java bean with a generic collection.
 *
 * @param <T> the type of the bean
 */
public final class BeanMapping<T> {

    /**
     * The name of the corresponding collection (not null)
     */
    private final String collection;

    /**
     * The mapping between the fields ot column of the collection with the java bean field (not null).
     */
    private final SortedMap<String, PropertyDescriptor> descriptors;

    /**
     * The default constructor of the bean (not null).
     */
    private final Constructor<T> constructor;

    /**
     * The name of the bean id (not null)
     */
    private final String id;

    /**
     * Builds and returns a bean mapping
     * @param cls the class of the bean (not null)
     * @param <U> the type of the bean
     * @return the just created mapping
     */
    public static <U> BeanMapping<U> from(final Class<U> cls) {
        final TreeMap<String, PropertyDescriptor> mapping = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String idName = null;
        try {
            for (java.lang.reflect.Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Field.class)) {
                    final String columnName = field.getAnnotation(Field.class).name();
                    mapping.put(
                            columnName,
                            new PropertyDescriptor(field.getName(), cls));
                    if (field.isAnnotationPresent(Id.class)) {
                        idName = columnName;
                    }
                }
            }
            return new BeanMapping<>(
                    cls.getAnnotation(Collection.class).name(),
                    cls.getConstructor(),
                    idName,
                    mapping);
        } catch (IntrospectionException | NoSuchMethodException err) {
            throw new IllegalArgumentException("Class cannot be used for with DAO: " + cls, err);
        }
    }

    /**
     * Constructor.
     * @param collection the name of the collection (not null)
     * @param constructor the default constructor of the bean (not null)
     * @param id the name of the id of the bean (not null)
     * @param descriptors the fields of the bean (not null)
     */
    private BeanMapping(final String collection, final Constructor<T> constructor, final String id,
                       final SortedMap<String, PropertyDescriptor> descriptors) {
        this.collection = collection;
        this.descriptors = descriptors;
        this.constructor = constructor;
        this.id = id;
    }

    /**
     * Returns the stream of the all the fields except the id
     * @return all the fields except the id (not null)
     */
    public Stream<String> withoutIdFields() {
        return this.descriptors.keySet().stream().filter(e->!e.equals(this.id));
    }

    /**
     * The name of the collection.
     * @return the name of the collection (not null)
     */
    public String getCollection() {
        return this.collection;
    }

    /**
     * Returns the number of mapped fields.
     * @return the number of mapped fields
     */
    public int size() {
        return this.descriptors.size();
    }

    /**
     * Returns the name of the id.
     * @return the name of the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set a value.
     * @param bean the bean to modify (if null a new one is created)
     * @param col the name of the field to update (not null)
     * @param value the value to set
     * @return the updated bean (not null)
     * @throws ReflectiveOperationException If the bean can't be accessible from reflexion
     */
    public T set(T bean, final String col, final Object value) throws ReflectiveOperationException {
        final PropertyDescriptor desc = this.descriptors.get(col);
        if (desc != null) {
            if (bean == null) {
                bean = this.constructor.newInstance();
            }
            desc.getWriteMethod().invoke(bean, value);
        }
       return bean;
    }

    /**
     * Set a new id.
     * @param bean the bean to modify. If null, a new one is created
     * @param value the new id value
     * @throws ReflectiveOperationException If the bean can't be accessible from reflexion
     */
    public void set(T bean, final Object value) throws ReflectiveOperationException {
        set(bean, this.id, value);
    }

    /**
     * Retrieves the value of a bean field.
     * @param bean the given bean (not null)
     * @param col the name of the field (not null)
     * @return the corresponding value
     * @throws ReflectiveOperationException If the bean can't be accessible from reflexion
     */
    public Object get(T bean, final String col) throws ReflectiveOperationException {
        return this.descriptors.get(col).getReadMethod().invoke(bean);
    }

    /**
     * Retrieves the id of a bean.
     * @param bean the given bean (not null)
     * @return the corresponding value
     * @throws ReflectiveOperationException If the bean can't be accessible from reflexion
     */
    public Object get(T bean) throws ReflectiveOperationException {
        return get(bean, this.id);
    }

}
