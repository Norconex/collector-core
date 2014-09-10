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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.ICrawler;

/**
 * @author Pascal Essiembre
 *
 */
public class CrawlerEventManager {

    private final ICrawlerEventListener[] listeners;
    private final ICrawler crawler;
    
    public CrawlerEventManager(
            ICrawler crawler, ICrawlerEventListener[] listeners) {
        this.crawler = crawler;
        if (listeners != null) {
            this.listeners = listeners;
        } else {
            this.listeners = new ICrawlerEventListener[] {};
        }
    }

    public void crawlerStarted() {
        for (ICrawlerEventListener listener : listeners) {
            listener.crawlerStarted(crawler);
        }
    }
    public void crawlerFinished() {
        for (ICrawlerEventListener listener : listeners) {
            listener.crawlerFinished(crawler);
        }
    }
    
    public void crawlerDocumentEvent(DocCrawlEvent event) {
        logEvent(event);
        for (ICrawlerEventListener listener : listeners) {
            listener.crawlerDocumentEvent(crawler, event);
        }
    }
    
    public void logEvent(DocCrawlEvent event) {
        Logger log =  LogManager.getLogger(
                DocCrawlEvent.class.getName() + "." + event.getEventType());
        log.info(getLogMessage(event));
    }
    
    protected String getLogMessage(DocCrawlEvent event) {
        StringBuilder b = new StringBuilder();
        b.append(StringUtils.leftPad(event.getEventType(), 20));
        b.append(": ");
        b.append(event.getDocCrawl().getReference());
        b.append(" (Subject: ");
        b.append(Objects.toString(event.getSubject(), "none"));
        return b.toString();
    }

}
