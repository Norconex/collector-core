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
package com.norconex.collector.core.crawler;

import com.norconex.commons.lang.event.IEventListener;

/**
 * Crawler event listener adapter for crawler startup/shutdown.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CrawlerLifeCycleListener
        implements IEventListener<CrawlerEvent<Crawler>> {

    @Override
    public final void accept(CrawlerEvent<Crawler> event) {
        if (event.isCrawlerStartup()) {
            onCrawlerStartup(event);
        } else if (event.isCrawlerShutdown()) {
            onCrawlerShutdown(event);
        } else if (event.isCrawlerCleaning()) {
            onCrawlerCleaning(event);
        } else if (event.isCrawlerCleaned()) {
            onCrawlerCleaned(event);
        }
    }
//    public static final boolean isCrawlerStartup(Event<?> event) {
//        return event instanceof CrawlerEvent
//                && event.is(CrawlerEvent.CRAWLER_STARTED);
//    }
//    public static final boolean isCrawlerShutdown(Event<?> event) {
//        return event instanceof CrawlerEvent && event.is(
//                CrawlerEvent.CRAWLER_FINISHED, CrawlerEvent.CRAWLER_STOPPED);
//    }
    protected void onCrawlerStartup(CrawlerEvent<Crawler> event) {
        //NOOP
    }
    protected void onCrawlerShutdown(CrawlerEvent<Crawler> event) {
        //NOOP
    }
    protected void onCrawlerCleaning(CrawlerEvent<Crawler> event) {
        //NOOP
    }
    protected void onCrawlerCleaned(CrawlerEvent<Crawler> event) {
        //NOOP
    }
}
