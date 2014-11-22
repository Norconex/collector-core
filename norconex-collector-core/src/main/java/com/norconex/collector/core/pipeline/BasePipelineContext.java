/* Copyright 2014 Norconex Inc.
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
