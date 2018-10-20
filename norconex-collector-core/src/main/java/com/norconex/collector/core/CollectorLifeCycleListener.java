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

import static com.norconex.collector.core.CollectorEvent.COLLECTOR_ENDED;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_ERROR;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STARTED;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STOPPED;

import com.norconex.commons.lang.event.Event;
import com.norconex.commons.lang.event.IEventListener;

/**
 * Collector event listener adapter for collector startup/shutdown.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CollectorLifeCycleListener
        implements IEventListener<CollectorEvent<Collector>> {

    @Override
    public final void accept(CollectorEvent<Collector> event) {
        if (isCollectorStartup(event)) {
            collectorStartup(event);
        } else if (isCollectorShutdown(event)) {
            collectorShutdown(event);
        }
    }
    public static final boolean isCollectorStartup(Event<?> event) {
        //XXX check we filter on current instance?  No longer needed
        // since we have parent event managers... it would prevent
        // a parent to listen for many children at once using this class
        // where as when set on a collector, only the collector events
        // will be receive as desired (provided a dedicated instance).
//        if (event.getSource() == Collector.get()) {
            return event instanceof CollectorEvent
                    && event.is(COLLECTOR_STARTED);
//        }
//        return false;
    }
    public static final boolean isCollectorShutdown(Event<?> event) {
//        if (event.getSource() == Collector.get()) {
            return event instanceof CollectorEvent && event.is(
                    COLLECTOR_ENDED, COLLECTOR_ERROR, COLLECTOR_STOPPED);
//        }
//        return false;
    }
    protected void collectorStartup(CollectorEvent<Collector> event) {
        //NOOP
    }
    protected void collectorShutdown(CollectorEvent<Collector> event) {
        //NOOP
    }
}
