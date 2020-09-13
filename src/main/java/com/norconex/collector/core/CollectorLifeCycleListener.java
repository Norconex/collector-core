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

import com.norconex.commons.lang.event.IEventListener;

/**
 * Collector event listener adapter for collector startup/shutdown.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CollectorLifeCycleListener
        implements IEventListener<CollectorEvent> {

    @Override
    public final void accept(CollectorEvent event) {
        if (event == null) {
            return;
        }
        onCollectorEvent(event);
        if (event.is(CollectorEvent.COLLECTOR_RUN_BEGIN)) {
            onCollectorRunBegin(event);
        } else if (event.is(CollectorEvent.COLLECTOR_RUN_END)) {
            onCollectorRunEnd(event);
            onCollectorShutdown(event);
        } else if (event.is(CollectorEvent.COLLECTOR_STOP_BEGIN)) {
            onCollectorStopBegin(event);
        } else if (event.is(CollectorEvent.COLLECTOR_STOP_END)) {
            onCollectorStopEnd(event);
            onCollectorShutdown(event);
        } else if (event.is(CollectorEvent.COLLECTOR_CLEAN_BEGIN)) {
            onCollectorCleanBegin(event);
        } else if (event.is(CollectorEvent.COLLECTOR_CLEAN_END)) {
            onCollectorCleanEnd(event);
        } else if (event.is(CollectorEvent.COLLECTOR_ERROR)) {
            onCollectorError(event);
            onCollectorShutdown(event);
        }
    }

    protected void onCollectorEvent(CollectorEvent event) {
        //NOOP
    }

    /**
     * Triggered when a collector is ending its execution on either
     * a {@link CollectorEvent#COLLECTOR_ERROR},
     * {@link CollectorEvent#COLLECTOR_RUN_END} or
     * {@link CollectorEvent#COLLECTOR_STOP_END} event.
     * @param event collector event
     */
    protected void onCollectorShutdown(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorError(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorRunBegin(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorRunEnd(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorStopBegin(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorStopEnd(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorCleanBegin(CollectorEvent event) {
        //NOOP
    }
    protected void onCollectorCleanEnd(CollectorEvent event) {
        //NOOP
    }
}
