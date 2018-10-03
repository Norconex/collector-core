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
package com.norconex.collector.core.crawler;

import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_FINISHED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_RESUMED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STARTED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STOPPED;

import com.norconex.commons.lang.event.Event;
import com.norconex.commons.lang.event.IEventListener;

/**
 * Crawler event listener adapter for crawler startup/shutdown.
 * @author Pascal Essiembre
 * @since 2.0.0
 */


//TODO provide an easy and intuitive way to distinguish between
// "any" crawler event and only specific ones (based on which crawler
// config it was found when scanned.  Maybe we need to add crawler-level
// listeners in config for this?

public class CrawlerLifeCycleListener
        implements IEventListener<CrawlerEvent<Crawler>> {

    @Override
    public final void accept(CrawlerEvent<Crawler> event) {
        if (isCrawlerStartup(event)) {
            crawlerStartup(event);
        } else if (isCrawlerShutdown(event)) {
            crawlerShutdown(event);
        }
    }
    public static final boolean isCrawlerStartup(Event<?> event) {

        // solution?
//        if (event.getSource() == Crawler.get()) {
            return event instanceof CrawlerEvent
                    && event.is(CRAWLER_STARTED, CRAWLER_RESUMED);
//        }
//        return false;
    }
    public static final boolean isCrawlerShutdown(Event<?> event) {
//        if (event.getSource() == Crawler.get()) {
            return event instanceof CrawlerEvent
                    && event.is(CRAWLER_FINISHED, CRAWLER_STOPPED);
//        }
//        return false;
    }
    protected void crawlerStartup(CrawlerEvent<Crawler> event) {
        //NOOP
    }
    protected void crawlerShutdown(CrawlerEvent<Crawler> event) {
        //NOOP
    }
}
