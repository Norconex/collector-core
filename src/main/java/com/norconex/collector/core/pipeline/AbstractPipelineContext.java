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

import java.util.function.Consumer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerCommitterService;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.crawler.CrawlerEvent.Builder;
import com.norconex.collector.core.doc.CrawlDocInfoService;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;

/**
 * Base {@link IPipelineStage} context for collector {@link Pipeline}s.
 * @author Pascal Essiembre
 */
public abstract class AbstractPipelineContext {

    private final Crawler crawler;

    /**
     * Constructor.
     * @param crawler the crawler
     * @since 1.9.0
     */
    public AbstractPipelineContext(Crawler crawler) {
        super();
        this.crawler = crawler;
    }

    public Crawler getCrawler() {
        return crawler;
    }

    public CrawlerConfig getConfig() {
        return crawler.getCrawlerConfig();
    }

    public CrawlDocInfoService getDocInfoService() {
        return crawler.getDocInfoService();
    }

    public CrawlerCommitterService getCommitterService() {
        return crawler.getCommitterService();
    }

    public EventManager getEventManager() {
        return crawler.getEventManager();
    }

    public void fire(CrawlerEvent event) {
        getEventManager().fire(event);
    }
    /**
     * Fires a crawler event with the current crawler as source.
     * @param eventName the event name
     * @param builder event builder consumer
     */
    public void fire(String eventName, Consumer<CrawlerEvent.Builder> builder) {
        Builder b = new Builder(eventName, crawler);
        if (builder != null) {
            builder.accept(b);
        }
        fire(b.build());
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
