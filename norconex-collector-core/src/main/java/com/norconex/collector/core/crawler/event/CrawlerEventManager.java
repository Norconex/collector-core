/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.crawler.event;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.ICrawler;

/**
 * Holds event listeners and allows to log events.  Events are also logged
 * using Log4j.  Each events have their own appenders, following this pattern:
 * <pre>
 *    com.norconex.collector.core.crawler.event.CrawlerEvent.<EVENT_ID>
 * </pre>
 * @author Pascal Essiembre
 */
public class CrawlerEventManager {

    private final ICrawlerEventListener[] listeners;
    private final ICrawler crawler;
    private static final int ID_PRINT_WIDTH = 20;
    
    public CrawlerEventManager(
            ICrawler crawler, ICrawlerEventListener[] listeners) {
        this.crawler = crawler;
        if (listeners != null) {
            this.listeners = Arrays.copyOf(listeners, listeners.length);
        } else {
            this.listeners = new ICrawlerEventListener[] {};
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
        Logger log =  LogManager.getLogger(
                CrawlerEvent.class.getName() + "." + event.getEventType());
        if (log.isDebugEnabled()) {
            log.debug(getLogMessage(event));
        }
    }
    
    protected String getLogMessage(CrawlerEvent event) {
        StringBuilder b = new StringBuilder();
        b.append(StringUtils.leftPad(event.getEventType(), ID_PRINT_WIDTH));
        if (event.getCrawlData() != null) {
            b.append(": ");
            b.append(event.getCrawlData().getReference());
        }
        b.append(" (Subject: ");
        b.append(Objects.toString(event.getSubject(), "none"));
        b.append(")");
        return b.toString();
    }

}
