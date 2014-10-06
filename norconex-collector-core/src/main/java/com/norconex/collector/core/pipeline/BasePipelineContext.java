/**
 * 
 */
package com.norconex.collector.core.pipeline;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;

/**
 * @author Pascal Essiembre
 *
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
