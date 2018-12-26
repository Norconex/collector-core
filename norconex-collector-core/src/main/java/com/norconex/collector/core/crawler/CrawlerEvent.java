/* Copyright 2018 Norconex Inc.
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
package com.norconex.collector.core.crawler;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.commons.lang.event.Event;

/**
 * A crawler event.
 * @author Pascal Essiembre
 * @param <T> Collector for this event
 * @since 2.0.0
 */
public class CrawlerEvent<T extends Crawler> extends Event<T> {

    private static final long serialVersionUID = 1L;

    /**
     * The crawler started.
     */
    public static final String CRAWLER_STARTED = "CRAWLER_STARTED";
    /**
     * The crawler resumed execution (from a previous incomplete crawl).
     */
    public static final String CRAWLER_RESUMED = "CRAWLER_RESUMED";
    /**
     * The crawler completed execution (without being stopped).
     */
    public static final String CRAWLER_FINISHED = "CRAWLER_FINISHED";
    /**
     * Issued when a request to stop the crawler has been received.
     */
    public static final String CRAWLER_STOPPING = "CRAWLER_STOPPING";
    /**
     * Issued when a request to stop the crawler has been fully executed
     * (crawler stopped).
     */
    public static final String CRAWLER_STOPPED = "CRAWLER_STOPPED";
    /**
     * A crawled document was rejected by a filters.
     */
    public static final String REJECTED_FILTER = "REJECTED_FILTER";
    /**
     * A crawled document was rejected as it was not modified since
     * last time it was crawled.
     */
    public static final String REJECTED_UNMODIFIED = "REJECTED_UNMODIFIED";
    /**
     * A document could not be re-crawled because it is not yet ready to be
     * re-crawled.
     */
    public static final String REJECTED_PREMATURE = "REJECTED_PREMATURE";
    /**
     * A document was rejected because it could not be found (e.g., no longer
     * exists at a given location).
     */
    public static final String REJECTED_NOTFOUND = "REJECTED_NOTFOUND";
    /**
     * A document was rejected because the status obtained when trying
     * to obtain it was not accepted (e.g., 500 HTTP error code).
     */
    public static final String REJECTED_BAD_STATUS = "REJECTED_BAD_STATUS";
    /**
     * A document was rejected by the Importer module.
     */
    public static final String REJECTED_IMPORT = "REJECTED_IMPORT";
    /**
     * A document was rejected because an error occurred when processing it.
     */
    public static final String REJECTED_ERROR = "REJECTED_ERROR";
    /**
     * A document pre-import processor was executed properly.
     */
    public static final String DOCUMENT_PREIMPORTED = "DOCUMENT_PREIMPORTED";
    /**
     * A document was imported.
     */
    public static final String DOCUMENT_IMPORTED = "DOCUMENT_IMPORTED";
    /**
     * A document post-import processor was executed properly.
     */
    public static final String DOCUMENT_POSTIMPORTED = "DOCUMENT_POSTIMPORTED";
    /**
     * A document was submitted to a committer for addition.
     */
    public static final String DOCUMENT_COMMITTED_ADD =
            "DOCUMENT_COMMITTED_ADD";
    /**
     * A document was submitted to a committer for removal.
     */
    public static final String DOCUMENT_COMMITTED_REMOVE =
            "DOCUMENT_COMMITTED_REMOVE";
    /**
     * A document metadata fields were successfully retrieved.
     */
    public static final String DOCUMENT_METADATA_FETCHED =
            "DOCUMENT_METADATA_FETCHED";
    /**
     * A document was successfully retrieved for processing.
     */
    public static final String DOCUMENT_FETCHED = "DOCUMENT_FETCHED";
    /**
     * A document was saved.
     */
    public static final String DOCUMENT_SAVED = "DOCUMENT_SAVED";


    private final ICrawlData crawlData;
    private final Object subject;
    //TODO keep a reference to actual document?


    /**
     * New crawler event.
     * @param name event name
     * @param source crawler responsible for triggering the event
     * @param crawlData information about a document being crawled
     * @param subject other relevant source related to the event
     * @param exception exception tied to this event (may be <code>null</code>)
     */
    public CrawlerEvent(String name, T source,
            ICrawlData crawlData, Object subject, Throwable exception) {
        super(name, source, exception);
        this.crawlData = crawlData;
        this.subject = subject;
    }

    public static CrawlerEvent<Crawler> create(String name, Crawler crawler) {
        return create(name, crawler, null);
    }
    public static CrawlerEvent<Crawler> create(
            String name, Crawler crawler, ICrawlData crawlData) {
        return create(name, crawler, crawlData, null, null);
    }
    public static CrawlerEvent<Crawler> create(String name, Crawler crawler,
            ICrawlData crawlData, Object subject) {
        return create(name, crawler, crawlData, subject, null);
    }
    public static CrawlerEvent<Crawler> create(String name, Crawler crawler,
            ICrawlData crawlData, Object subject, Throwable exception) {
        return new CrawlerEvent<>(name, crawler, crawlData, subject, exception);
    }

    /**
     * Gets the crawl data holding contextual information about the
     * crawled reference.  CRAWLER_* events will return a <code>null</code>
     * crawl data.
     * @return crawl data
     */
    public ICrawlData getCrawlData() {
        return crawlData;
    }

    public Object getSubject() {
        return subject;
    }

    public boolean isCrawlerStartup() {
        return is(CRAWLER_STARTED, CRAWLER_RESUMED);
    }
    public boolean isCrawlerShutdown() {
        return is(CRAWLER_FINISHED, CRAWLER_STOPPED);
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
        StringBuilder b = new StringBuilder();
        if (crawlData != null) {
            b.append(crawlData.getReference()).append(" - ");
        }
        if (subject != null) {
            b.append(subject.toString());
        } else {
            b.append(Objects.toString(source));
        }
        return b.toString();
    }
}
