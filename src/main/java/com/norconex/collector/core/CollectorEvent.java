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
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CollectorEvent extends Event {

    private static final long serialVersionUID = 1L;

    public static final String COLLECTOR_RUN_BEGIN = "COLLECTOR_RUN_BEGIN";
    public static final String COLLECTOR_RUN_END = "COLLECTOR_RUN_END";
    public static final String COLLECTOR_STOP_BEGIN = "COLLECTOR_STOP_BEGIN";
    public static final String COLLECTOR_STOP_END = "COLLECTOR_STOP_END";
    public static final String COLLECTOR_CLEAN_BEGIN = "COLLECTOR_CLEAN_BEGIN";
    public static final String COLLECTOR_CLEAN_END = "COLLECTOR_CLEAN_END";
    public static final String COLLECTOR_STORE_EXPORT_BEGIN =
            "COLLECTOR_STORE_EXPORT_BEGIN";
    public static final String COLLECTOR_STORE_EXPORT_END =
            "COLLECTOR_STORE_EXPORT_END";
    public static final String COLLECTOR_STORE_IMPORT_BEGIN =
            "COLLECTOR_STORE_IMPORT_BEGIN";
    public static final String COLLECTOR_STORE_IMPORT_END =
            "COLLECTOR_STORE_IMPORT_END";


    //TODO Not used. Needed?
    public static final String COLLECTOR_ERROR = "COLLECTOR_ERROR";

    public static class Builder extends Event.Builder<Builder> {

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
     * @param b builder
     */
    CollectorEvent(Builder b) {
        super(b);
    }

    @Override
    public Collector getSource() {
        return (Collector) super.getSource();
    }

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
