/* Copyright 2014-2020 Norconex Inc.
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

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;
import com.norconex.importer.doc.Doc;

/**
 * {@link IPipelineStage} context for collector {@link Pipeline}s dealing with
 * an {@link Doc}.
 * @author Pascal Essiembre
 */
public class DocumentPipelineContext extends BasePipelineContext {

    private Doc document;
    private CrawlDocInfo cachedCrawlRef;

    /**
     * Constructor.
     * @param crawler the crawler
     * @since 1.9.0
     */
    public DocumentPipelineContext(Crawler crawler) {
        super(crawler, null);
    }
    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlRef current crawl data
     * @since 1.9.0
     */
    public DocumentPipelineContext(Crawler crawler, CrawlDocInfo crawlRef) {
        super(crawler, crawlRef);
    }
    public DocumentPipelineContext(
            Crawler crawler,
            CrawlDocInfo crawlRef,
            CrawlDocInfo cachedCrawlRef,
            Doc document) {
        super(crawler/*, crawlDataStore*/, crawlRef);
        this.cachedCrawlRef = cachedCrawlRef;
        this.document = document;
    }

    public Doc getDocument() {
        return document;
    }

    /**
     * Gets cached crawl data.
     * @return cached crawl data
     * @since 1.9.0
     */
    public CrawlDocInfo getCachedCrawlReference() {
        return cachedCrawlRef;
    }
    /**
     * Sets cached crawl data.
     * @param cachedCrawlRef cached crawl data.
     * @since 1.9.0
     */
    public void setCachedCrawlReference(CrawlDocInfo cachedCrawlRef) {
        this.cachedCrawlRef = cachedCrawlRef;
    }
    /**
     * Sets document.
     * @param document a document
     * @since 1.9.0
     */
    public void setDocument(Doc document) {
        this.document = document;
    }
    public CachedInputStream getContent() {
        return getDocument().getInputStream();
    }

    public Reader getContentReader() {
        return new InputStreamReader(
                getDocument().getInputStream(), StandardCharsets.UTF_8);
    }
}
