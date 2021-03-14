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
package com.norconex.collector.core.monitor;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.doc.CrawlDocInfoService;
import com.norconex.commons.lang.event.EventManager;

public class CrawlerMonitor implements CrawlerMonitorMXBean {

    //Maybe have it configured to decide what to capture?
    private final CrawlDocInfoService service;
    private final Map<String, AtomicLong> eventCounts =
            new ConcurrentHashMap<>();

    public CrawlerMonitor(Crawler crawler) {
        Objects.requireNonNull(crawler, "'crawler' must not be null.");
        this.service = Objects.requireNonNull(crawler.getDocInfoService(),
                "'crawler#getDocInfoService() must not be null.");
        EventManager eventManager =
                Objects.requireNonNull(crawler.getEventManager(),
                        "'crawler#getEventManager() must not be null.");
        eventManager.addListener(e -> eventCounts.computeIfAbsent(
                e.getName(), k -> new AtomicLong()).incrementAndGet());
    }

    @Override
    public long getProcessedCount() {
        return service.getProcessedCount();
    }
    @Override
    public long getQueuedCount() {
        return service.getQueueCount();
    }
    @Override
    public long getActiveCount() {
        return service.getActiveCount();
    }

    @Override
    public Map<String, Long> getEventCounts() {
        Map<String, Long> map = new TreeMap<>();
        eventCounts.forEach((event, count) ->
            map.put(event, count.longValue()));
        return map;
    }
}
