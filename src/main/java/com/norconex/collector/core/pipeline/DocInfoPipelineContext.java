/* Copyright 2020 Norconex Inc.
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
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;

/**
 * A {@link IPipelineStage} context for collector {@link Pipeline}s dealing
 * with a {@link CrawlDocInfo} (e.g. document queuing).
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class DocInfoPipelineContext extends AbstractPipelineContext {

    private CrawlDocInfo docInfo;

//    /**
//     * Constructor.
//     * @param crawler the crawler
//     * @since 1.9.0
//     */
//    public DocInfoPipelineContext(Crawler crawler) {
//        super();
//        this.crawler = crawler;
//        //this(crawler, null);
//    }

    /**
     * Constructor.
     * @param crawler the crawler
     * @param docInfo current crawl docInfo
     */
    public DocInfoPipelineContext(Crawler crawler, CrawlDocInfo docInfo) {
        super(crawler);
        this.docInfo = docInfo;
    }


    public CrawlDocInfo getDocInfo() {
        return docInfo;
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
