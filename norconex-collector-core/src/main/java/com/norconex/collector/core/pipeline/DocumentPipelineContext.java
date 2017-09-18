/* Copyright 2014-2017 Norconex Inc.
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;
import com.norconex.importer.doc.ImporterDocument;

/**
 * {@link IPipelineStage} context for collector {@link Pipeline}s dealing with 
 * an {@link ImporterDocument}.
 * @author Pascal Essiembre
 */
public class DocumentPipelineContext extends BasePipelineContext {

    private ImporterDocument document;
    private BaseCrawlData cachedCrawlData;

    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlDataStore crawl data store
     * @since 1.9.0
     */
    public DocumentPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore) {
        super(crawler, crawlDataStore, null);
    }
    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlDataStore crawl data store
     * @param crawlData current crawl data
     * @since 1.9.0
     */
    public DocumentPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData) {
        super(crawler, crawlDataStore, crawlData);
    }
    public DocumentPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData,
            BaseCrawlData cachedCrawlData,
            ImporterDocument document) {
        super(crawler, crawlDataStore, crawlData);
        this.cachedCrawlData = cachedCrawlData;
        this.document = document;
    }

    public ImporterDocument getDocument() {
        return document;
    }

    /**
     * Gets cached crawl data.
     * @return cached crawl data
     * @since 1.9.0
     */
    public BaseCrawlData getCachedCrawlData() {
        return cachedCrawlData;
    }
    /**
     * Sets cached crawl data.
     * @param cachedCrawlData cached crawl data.
     * @since 1.9.0
     */
    public void setCachedCrawlData(BaseCrawlData cachedCrawlData) {
        this.cachedCrawlData = cachedCrawlData;
    }
    /**
     * Sets document.
     * @param document a document
     * @since 1.9.0
     */
    public void setDocument(ImporterDocument document) {
        this.document = document;
    }
    public CachedInputStream getContent() {
        return getDocument().getContent();
    }

    public Reader getContentReader() {
        return new InputStreamReader(
                getDocument().getContent(), StandardCharsets.UTF_8);
    }    
}
