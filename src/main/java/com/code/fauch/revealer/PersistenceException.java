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

import java.io.Serial;

/**
 * Exception for all persistence exceptions.
 */
public final class PersistenceException extends Exception {

    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * Constructor.
     *
     * @param th the cause
     */
    public PersistenceException(final Throwable th) {
        super(th);
    }

}
