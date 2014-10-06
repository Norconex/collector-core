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
package com.norconex.collector.core.pipeline;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;

/**
 * Base {@link IPipelineStage} context for collector {@link Pipeline}s.
 * @author Pascal Essiembre
 */
public class BasePipelineContext {

    private final ICrawler crawler;
    private final ICrawlDataStore crawlDataStore;
    private final BaseCrawlData crawlData;

    public BasePipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData) {
        this.crawler = crawler;
        this.crawlDataStore = crawlDataStore;
        this.crawlData = crawlData;
    }

    public ICrawler getCrawler() {
        return crawler;
    }

    public ICrawlerConfig getConfig() {
        return crawler.getCrawlerConfig();
    }
    
    public BaseCrawlData getCrawlData() {
        return crawlData;
    }

    public ICrawlDataStore getCrawlDataStore() {
        return crawlDataStore;
    }
    
    public void fireCrawlerEvent(
            String event, ICrawlData crawlData, Object subject) {
        crawler.getCrawlerEventManager().fireCrawlerEvent(new CrawlerEvent(
                event, crawlData, subject));
    }
}
