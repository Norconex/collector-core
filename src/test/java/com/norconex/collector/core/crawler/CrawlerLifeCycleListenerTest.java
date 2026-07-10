/* Copyright 2026 Norconex Inc.
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
package com.norconex.collector.core.crawler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import com.norconex.collector.core.CollectorEvent;
import com.norconex.collector.core.MockCollector;
import com.norconex.collector.core.MockCollectorConfig;
import com.norconex.commons.lang.event.Event;
import com.norconex.commons.lang.event.IEventListener;

class CrawlerLifeCycleListenerTest {

    @SuppressWarnings("unchecked")
    @Test
    void ignoresNonCrawlerEvents() {
        IEventListener<Event> listener = (IEventListener<Event>) (IEventListener<?>) new CrawlerLifeCycleListener();
        Event collectorEvent = new CollectorEvent.Builder(
                CollectorEvent.COLLECTOR_RUN_BEGIN,
                new MockCollector(new MockCollectorConfig())).build();

        assertDoesNotThrow(() -> listener.accept(collectorEvent));
    }
}
