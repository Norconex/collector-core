/* Copyright 2021 Norconex Inc.
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
package com.norconex.collector.core.stop;

/**
 * Exception thrown when a problem occurred while trying to stop
 * a collector.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CollectorStopperException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CollectorStopperException(final String message) {
        super(message);
    }
    public CollectorStopperException(final Throwable exception) {
        super(exception);
    }
    public CollectorStopperException(
            final String message, final Throwable exception) {
        super(message, exception);
    }
}
