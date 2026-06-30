/* Copyright 2018-2020 Norconex Inc.
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

import com.norconex.commons.lang.event.Event;

/**
 * A crawler event.
 * 
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CollectorEvent extends Event {

    private static final long serialVersionUID = 1L;

    /** Event name fired when a collector run starts. */
    public static final String COLLECTOR_RUN_BEGIN = "COLLECTOR_RUN_BEGIN";
    /** Event name fired when a collector run ends. */
    public static final String COLLECTOR_RUN_END = "COLLECTOR_RUN_END";
    /** Event name fired when a collector stop sequence starts. */
    public static final String COLLECTOR_STOP_BEGIN = "COLLECTOR_STOP_BEGIN";
    /** Event name fired when a collector stop sequence ends. */
    public static final String COLLECTOR_STOP_END = "COLLECTOR_STOP_END";
    /** Event name fired when a collector clean sequence starts. */
    public static final String COLLECTOR_CLEAN_BEGIN = "COLLECTOR_CLEAN_BEGIN";
    /** Event name fired when a collector clean sequence ends. */
    public static final String COLLECTOR_CLEAN_END = "COLLECTOR_CLEAN_END";
    /** Event name fired when data store export starts. */
    public static final String COLLECTOR_STORE_EXPORT_BEGIN = "COLLECTOR_STORE_EXPORT_BEGIN";
    /** Event name fired when data store export ends. */
    public static final String COLLECTOR_STORE_EXPORT_END = "COLLECTOR_STORE_EXPORT_END";
    /** Event name fired when data store import starts. */
    public static final String COLLECTOR_STORE_IMPORT_BEGIN = "COLLECTOR_STORE_IMPORT_BEGIN";
    /** Event name fired when data store import ends. */
    public static final String COLLECTOR_STORE_IMPORT_END = "COLLECTOR_STORE_IMPORT_END";

    // TODO Not used. Needed?
    /** Event name fired when the collector emits an error event. */
    public static final String COLLECTOR_ERROR = "COLLECTOR_ERROR";

    /** Builder for {@link CollectorEvent}. */
    public static class Builder extends Event.Builder<Builder> {

        /**
         * Constructor.
         * 
         * @param name   event name
         * @param source collector source
         */
        public Builder(String name, Collector source) {
            super(name, source);
        }

        @Override
        public CollectorEvent build() {
            return new CollectorEvent(this);
        }
    }

    /**
     * New event. Name and source cannot be <code>null</code>.
     * 
     * @param b builder
     */
    CollectorEvent(Builder b) {
        super(b);
    }

    @Override
    public Collector getSource() {
        return (Collector) super.getSource();
    }

    /**
     * Whether the given event represents a collector shutdown state.
     * 
     * @param event event to evaluate
     * @return <code>true</code> when the event is a shutdown marker
     */
    public boolean isCollectorShutdown(Event/* <?> */ event) {
        return is(COLLECTOR_RUN_END, COLLECTOR_ERROR, COLLECTOR_STOP_END);
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        // Cannot use HashCodeBuilder.reflectionHashCode here to prevent
        // "An illegal reflective access operation has occurred"
        return super.hashCode();
    }
}
