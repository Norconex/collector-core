/* Copyright 2018 Norconex Inc.
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
package com.norconex.collector.core;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.norconex.commons.lang.event.Event;

/**
 * A crawler event.
 * @author Pascal Essiembre
 * @param <T> Collector for this event
 * @since 2.0.0
 */
public class CollectorEvent<T extends Collector> extends Event<T> {

    private static final long serialVersionUID = 1L;

    public static final String COLLECTOR_STARTED = "COLLECTOR_STARTED";
    public static final String COLLECTOR_ENDED = "COLLECTOR_ENDED";
    public static final String COLLECTOR_STOPPING = "COLLECTOR_STOPPING";
    public static final String COLLECTOR_STOPPED = "COLLECTOR_STOPPED"; // <-- Realy an event? If stopped, it means nothing else
//    public static final String COLLECTOR_ABORTED = "COLLECTOR_ABORTED"; // <-- Realy an event? If abborted, no event can be sent.
    //TODO eliminate this one and rely on COMPLETED + status to find if failed/success?
    //TODO have a COLLECTOR_FAILED instead (or in addition??)
//    public static final String COLLECTOR_UNCOMPLETED = "COLLECTOR_UNCOMPLETED";
//    public static final String COLLECTOR_COMPLETED = "COLLECTOR_COMPLETED";

    //TODO Add COLLECTOR_ERROR?
    public static final String COLLECTOR_ERROR = "COLLECTOR_ERROR";

    //TODO have COLLECTOR_RESUMED???

    /**
     * New collector event.
     * @param name event name
     * @param source collector responsible for triggering the event
     * @param exception exception tied to this event (may be <code>null</code>)
     */
    public CollectorEvent(String name, T source, Throwable exception) {
        super(name, source, exception);
    }

    public static CollectorEvent<Collector> create(
            String name, Collector collector) {
        return create(name, collector, null);
    }
    public static CollectorEvent<Collector> create(
            String name, Collector collector, Throwable exception) {
        return new CollectorEvent<>(name, collector, exception);
    }


    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
