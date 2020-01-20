/*
 * Copyright 2019 Claire Fauch
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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * All available column type-JAVA class mapping.
 * 
 * @author c.fauch
 *
 */
public enum EType {
    STRING(String.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setString(index, (String)value);
        }
        
    },
    INTEGER(Integer.class) {
        
        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setInt(index, (Integer)value);
        }
        
    },
    BOOLEAN(Boolean.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setBoolean(index, (Boolean)value);
        }
        
    },
    BYTE(Byte.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setByte(index, (Byte)value);
        }
        
    },
    SHORT(Short.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setShort(index, (Short)value);
        }
        
    },
    LONG(Long.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setLong(index, (Long)value);
        }
        
    },
    DECIMAL(BigDecimal.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setBigDecimal(index, (BigDecimal)value);
        }
        
    },
    DOUBLE(Double.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setDouble(index, (Double)value);
        }
        
    },
    FLOAT(Float.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setFloat(index, (Float)value);
        }
        
    },
    TIMESTAMP(LocalDateTime.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setTimestamp(index, Timestamp.valueOf((LocalDateTime)value));
        }
        
    },
    UUID(UUID.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setObject(index, value);
        }
        
    },
    ENUM(Enum.class) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setString(index, ((Enum<?>) value).name());
        }
        
    },
    OTHER(null) {

        @Override
        public void fill(PreparedStatement prep, int index, Object value) throws SQLException {
            prep.setObject(index, value);
        }
        
    };
    
    /**
     * Corresponding JAVA class.
     */
    private final Class<?> cls;
    
    /**
     * Constructor.
     * 
     * @param cls the JAVA class to map
     */
    private EType(final Class<?> cls) {
        this.cls = cls;
    }
    
    /**
     * Search for the type corresponding to the given JAVA class.
     * 
     * @param cls the JAVA class (not null)
     * @return the corresponding type
     */
    public static EType from(final Class<?> cls) {
        if (cls.isEnum()) {
            return EType.ENUM;
        }
        for (EType t : values()) {
            if (t.cls == cls) {
                return t;
            }
        }
        return EType.OTHER;
    }
    
    /**
     * Do argument mapping and fill the prepared statement.
     * 
     * @param prep the statement to complete (not null)
     * @param index the index of the argument to replace
     * @param value the value to map
     * @throws SQLException
     */
    public abstract void fill(PreparedStatement prep, int index, Object value) throws SQLException;
    
}
