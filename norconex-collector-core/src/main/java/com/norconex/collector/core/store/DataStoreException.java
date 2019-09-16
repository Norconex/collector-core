/* Copyright 2019 Norconex Inc.
 *
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
package com.norconex.collector.core.store;

import com.norconex.collector.core.CollectorException;

/**
 * Crawl data store runtime exception.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class DataStoreException extends CollectorException {


    private static final long serialVersionUID = 5416591514078326431L;

    public DataStoreException() {
        super();
    }

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(Throwable cause) {
        super(cause);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
