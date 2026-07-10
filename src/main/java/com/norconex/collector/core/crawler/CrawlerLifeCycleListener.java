/* Copyright 2019-2020 Norconex Inc.
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

import com.norconex.commons.lang.event.Event;
import com.norconex.commons.lang.event.IEventListener;

/**
 * Listener adapter for crawler events.
 * 
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CrawlerLifeCycleListener
        implements IEventListener<Event> {

    @Override
    public final void accept(Event event) {
        if (!(event instanceof CrawlerEvent)) {
            return;
        }
        var crawlerEvent = (CrawlerEvent) event;
        onCrawlerEvent(crawlerEvent);
        if (crawlerEvent.is(CrawlerEvent.CRAWLER_INIT_BEGIN)) {
            onCrawlerInitBegin(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_INIT_END)) {
            onCrawlerInitEnd(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_RUN_BEGIN)) {
            onCrawlerRunBegin(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_RUN_END)) {
            onCrawlerRunEnd(crawlerEvent);
            onCrawlerShutdown(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_RUN_THREAD_BEGIN)) {
            onCrawlerRunThreadBegin(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_RUN_THREAD_END)) {
            onCrawlerRunThreadEnd(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_STOP_BEGIN)) {
            onCrawlerStopBegin(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_STOP_END)) {
            onCrawlerStopEnd(crawlerEvent);
            onCrawlerShutdown(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_CLEAN_BEGIN)) {
            onCrawlerCleanBegin(crawlerEvent);
        } else if (crawlerEvent.is(CrawlerEvent.CRAWLER_CLEAN_END)) {
            onCrawlerCleanEnd(crawlerEvent);
        }
    }

    protected void onCrawlerEvent(CrawlerEvent event) {
        // NOOP
    }

    /**
     * Triggered when a crawler is ending its execution on either
     * a {@link CrawlerEvent#CRAWLER_RUN_END} or
     * {@link CrawlerEvent#CRAWLER_STOP_END} event.
     * 
     * @param event crawler event
     */
    protected void onCrawlerShutdown(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerInitBegin(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerInitEnd(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerRunBegin(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerRunEnd(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerRunThreadBegin(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerRunThreadEnd(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerStopBegin(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerStopEnd(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerCleanBegin(CrawlerEvent event) {
        // NOOP
    }

    protected void onCrawlerCleanEnd(CrawlerEvent event) {
        // NOOP
    }
}
