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
package com.norconex.collector.core.crawler.event;

import com.norconex.collector.core.data.ICrawlData;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A crawler event.
 * @author Pascal Essiembre
 * @see ICrawlerEventListener
 */
public class CrawlerEvent {

    public static final String CRAWLER_STARTED = "CRAWLER_STARTED";
    public static final String CRAWLER_RESUMED = "CRAWLER_RESUMED";
    public static final String CRAWLER_FINISHED = "CRAWLER_FINISHED";

    public static final String REJECTED_FILTER = "REJECTED_FILTER";
    public static final String REJECTED_UNMODIFIED = "REJECTED_UNMODIFIED";
    public static final String REJECTED_NOTFOUND = "REJECTED_NOTFOUND";
    public static final String REJECTED_BAD_STATUS = "REJECTED_BAD_STATUS";
    public static final String REJECTED_IMPORT = "REJECTED_IMPORT";
    public static final String REJECTED_ERROR = "REJECTED_ERROR";
    
    public static final String DOCUMENT_PREIMPORTED = "DOCUMENT_PREIMPORTED";
    public static final String DOCUMENT_POSTIMPORTED = "DOCUMENT_POSTIMPORTED";
    public static final String DOCUMENT_COMMITTED_ADD = 
            "DOCUMENT_COMMITTED_ADD";
    public static final String DOCUMENT_COMMITTED_REMOVE = 
            "DOCUMENT_COMMITTED_REMOV";
    public static final String DOCUMENT_IMPORTED = "DOCUMENT_IMPORTED";
    public static final String DOCUMENT_METADATA_FETCHED = 
            "DOCUMENT_METADATA_FETCHED";
    public static final String DOCUMENT_FETCHED = "DOCUMENT_FETCHED";
    public static final String DOCUMENT_SAVED = "DOCUMENT_SAVED";


    private final ICrawlData crawlData;
    private final Object subject;
    private final String eventType;

    public CrawlerEvent(String eventType, ICrawlData crawlData, Object subject) {
        super();
        this.crawlData = crawlData;
        this.subject = subject;
        this.eventType = eventType;
    }
    
    /**
     * Gets the subject of this event.  A subject is typically the class
     * or a string representing the source or cause if the event.
     * @return the subject
     */
    public Object getSubject() {
        return subject;
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

    /**
     * Gets the event type.
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CrawlerEvent))
            return false;
        CrawlerEvent castOther = (CrawlerEvent) other;
        return new EqualsBuilder().append(crawlData, castOther.crawlData)
                .append(subject, castOther.subject)
                .append(eventType, castOther.eventType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(crawlData).append(subject)
                .append(eventType).toHashCode();
    }

    private transient String toString;

    @Override
    public String toString() {
        if (toString == null) {
            toString = new ToStringBuilder(this).appendSuper(super.toString())
                    .append("crawlData", crawlData).append("subject", subject)
                    .append("eventType", eventType).toString();
        }
        return toString;
    }
}
