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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.doc.CrawlDocInfoService;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;

/**
 * Base {@link IPipelineStage} context for collector {@link Pipeline}s.
 * @author Pascal Essiembre
 */
public abstract class AbstractPipelineContext {

    private final Crawler crawler;
//    private CrawlDocInfo docInfo;

    /**
     * Constructor.
     * @param crawler the crawler
     * @since 1.9.0
     */
    public AbstractPipelineContext(Crawler crawler) {
        super();
        this.crawler = crawler;
        //this(crawler, null);
    }

//    /**
//     * Constructor.
//     * @param crawler the crawler
//     * @param docInfo current crawl docInfo
//     */
//    public AbstractPipelineContext(Crawler crawler, CrawlDocInfo docInfo) {
//        this.crawler = crawler;
//        this.docInfo = docInfo;
//    }

    public Crawler getCrawler() {
        return crawler;
    }

    public CrawlerConfig getConfig() {
        return crawler.getCrawlerConfig();
    }

//    public CrawlDocInfo getDocInfo() {
//        return docInfo;
//    }
//    public void setDocInfo(CrawlDocInfo docInfo) {
//        this.docInfo = docInfo;
//    }

    public CrawlDocInfoService getDocInfoService() {
        return crawler.getDocInfoService();
    }

    /**
     * Fires an event.
     * @param event the event name
     * @param docInfo crawl data
     * @param subject subject triggering the event
     */
    public void fireCrawlerEvent(
            String event, CrawlDocInfo docInfo, Object subject) {
        crawler.getEventManager().fire(CrawlerEvent.create(
                event, crawler, docInfo, subject));
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
