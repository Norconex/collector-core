/* Copyright 2014-2018 Norconex Inc.
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
package com.norconex.collector.core.crawler.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.ICrawler;

/**
 * Manage event listeners and log events.  Events are logged
 * using Log4j with the INFO level.
 * Each events have their own Log4j appenders, following this pattern:
 * <pre>
 *    CrawlerEvent.&lt;EVENT_ID&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 */
public class CrawlerEventManager {

    private final List<ICrawlerEventListener> listeners = new ArrayList<>();
    private final ICrawler crawler;
    private static final int ID_PRINT_WIDTH = 25;

    public CrawlerEventManager(
            ICrawler crawler, List<ICrawlerEventListener> listeners) {
        this.crawler = crawler;
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }

    public void fireCrawlerEvent(CrawlerEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Cannot fire a null CrawlerEvent.");
        }
        logEvent(event);
        for (ICrawlerEventListener listener : listeners) {
            listener.crawlerEvent(crawler, event);
        }
    }

    private void logEvent(CrawlerEvent event) {
        Logger log =  LoggerFactory.getLogger(CrawlerEvent.class.getSimpleName()
                + "." + event.getEventType());
        if (log.isInfoEnabled()) {
            log.info(getLogMessage(event, log.isDebugEnabled()));
        }
    }

    private String getLogMessage(CrawlerEvent event, boolean includeSubject) {
        StringBuilder b = new StringBuilder();
        b.append(StringUtils.leftPad(event.getEventType(), ID_PRINT_WIDTH));
        if (event.getCrawlData() != null) {
            b.append(": ");
            b.append(event.getCrawlData().getReference());
        }
        if (includeSubject) {
            b.append(" (");
            b.append(Objects.toString(event.getSubject(),
                    "No additional information available."));
            b.append(")");
        }
        return b.toString();
    }

}
