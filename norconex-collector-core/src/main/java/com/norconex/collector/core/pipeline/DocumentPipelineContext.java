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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
    private CrawlDocInfo cachedDocInfo;

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
     * @param docInfo current crawl data
     * @since 1.9.0
     */
    public DocumentPipelineContext(Crawler crawler, CrawlDocInfo docInfo) {
        super(crawler, docInfo);
    }
    public DocumentPipelineContext(
            Crawler crawler,
            CrawlDocInfo docInfo,
            CrawlDocInfo cachedDocInfo,
            Doc document) {
        super(crawler, docInfo);
        this.cachedDocInfo = cachedDocInfo;
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
    public CrawlDocInfo getCachedDocInfo() {
        return cachedDocInfo;
    }
    /**
     * Sets cached crawl data.
     * @param cachedDocInfo cached crawl data.
     * @since 1.9.0
     */
    public void setCachedDocInfo(CrawlDocInfo cachedDocInfo) {
        this.cachedDocInfo = cachedDocInfo;
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

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this,
                ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
