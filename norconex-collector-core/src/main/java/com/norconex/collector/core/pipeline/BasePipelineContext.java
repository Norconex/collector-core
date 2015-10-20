/* Copyright 2014-2015 Norconex Inc.
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
package com.norconex.collector.core.pipeline;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.crawler.event.CrawlerEventManager;
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
    //TODO move this one to DocumentPipelineContext?
    private final BaseCrawlData cachedCrawlData;

    public BasePipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData) {
        this(crawler, crawlDataStore, crawlData, null);
    }
    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlDataStore crawl data store
     * @param crawlData current crawl data
     * @param cachedCrawlData crawl data from previous run, if any
     * @since 1.3.0
     */
    public BasePipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData, BaseCrawlData cachedCrawlData) {
        this.crawler = crawler;
        this.crawlDataStore = crawlDataStore;
        this.crawlData = crawlData;
        this.cachedCrawlData = cachedCrawlData;
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
    /**
     * Gets the crawl data from previous run, if any.
     * @return cached crawl data
     * @since 1.3.0
     */
    public BaseCrawlData getCachedCrawlData() {
        return cachedCrawlData;
    }

    public ICrawlDataStore getCrawlDataStore() {
        return crawlDataStore;
    }
    
    public void fireCrawlerEvent(
            String event, ICrawlData crawlData, Object subject) {
        CrawlerEventManager eventManager = crawler.getCrawlerEventManager();
        if (eventManager != null) {
            crawler.getCrawlerEventManager().fireCrawlerEvent(new CrawlerEvent(
                    event, crawlData, subject));
        }
    }
}
