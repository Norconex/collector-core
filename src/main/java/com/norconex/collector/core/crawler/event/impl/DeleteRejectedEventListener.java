/* Copyright 2021 Norconex Inc.
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
package com.norconex.collector.core.crawler.event.impl;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.doc.CrawlDoc;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.store.IDataStore;
import com.norconex.commons.lang.event.Event;
import com.norconex.commons.lang.event.IEventListener;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * Provides the ability to send deletion requests to your configured
 * committer(s) whenever a reference is rejected, regardless whether it was
 * encountered in a previous crawling session or not.
 * </p>
 *
 * <h3>Supported events</h3>
 * <p>
 * By default this listener will send deletion requests for all references
 * associated with a {@link CrawlerEvent} name starting with
 * <code>REJECTED_</code>. To avoid performance issues when dealing with
 * too many deletion requests, it is recommended you can change this behavior
 * to match exactly the events you are interested in with
 * {@link #setEventMatcher(TextMatcher)}.
 * Keep limiting events to "rejected" ones to avoid unexpected results.
 * </p>
 *
 * <h3>Deletion requests sent once</h3>
 * <p>
 * This class tries to handles each reference for "rejected" events only once.
 * To do so it will queue all such references and wait until normal
 * crawler completion to send them. Waiting for completion also gives this
 * class a chance to listen for deletion requests sent to your committer as
 * part of the crawler regular execution (typically on subsequent crawls).
 * This helps ensure you do not get duplicate deletion requests for the same
 * reference.
 * </p>
 *
 * <h3>Only references</h3>
 * <p>
 * Since several rejection events are triggered before document are processed,
 * we can't assume there is any metadata attached with rejected
 * references. Be aware this can cause issues if you are using rules in your
 * committer (e.g., to route requests) based on metadata.
 * <p>
 *
 * {@nx.xml.usage
 * <listener
 *     class="com.norconex.collector.core.crawler.event.impl.DeleteRejectedEventListener">
 *   <eventMatcher
 *     {@nx.include com.norconex.commons.lang.text.TextMatcher#matchAttributes}>
 *       (event name-matching expression)
 *   </eventMatcher>
 * </listener>
 * }
 *
 * {@nx.xml.example
 * <listener class="DeleteRejectedEventListener">
 *   <eventMatcher method="csv">REJECTED_NOTFOUND,REJECTED_FILTER</eventMatcher>
 * </listener>
 * }
 * <p>
 * The above example will send deletion requests whenever a reference is not
 * found (e.g., a 404 response from a web server) or if it was filtered out
 * by the crawler.
 * </p>
 *
 * @author Pascal Essiembre
 * @since 3.0.0
 */
@SuppressWarnings("javadoc")
public class DeleteRejectedEventListener
        implements IEventListener<Event>, IXMLConfigurable {

    private static final Logger LOG =
            LoggerFactory.getLogger(DeleteRejectedEventListener.class);

    public static final String DEFAULT_FILENAME_PREFIX = "urlstatuses-";

    private final TextMatcher eventMatcher = TextMatcher.regex("REJECTED_.*");

    // key=reference; value=whether deletion request was already sent
    private IDataStore<Boolean> refStore;
    private boolean doneCrawling;

    /**
     * Gets the event matcher used to identify which events can trigger
     * a deletion request. Default is regular expression
     * <code>REJECTED_.*</code>.
     * @return text matcher, never <code>null</code>
     */
    public TextMatcher getEventMatcher() {
        return eventMatcher;
    }
    /**
     * Sets the event matcher used to identify which events can trigger
     * a deletion request.
     * @param eventMatcher event matcher
     */
    public void setEventMatcher(TextMatcher eventMatcher) {
        this.eventMatcher.copyFrom(eventMatcher);
    }

    @Override
    public void accept(Event event) {
        if (!(event instanceof CrawlerEvent)) {
            return;
        }

        CrawlerEvent crawlerEvent = (CrawlerEvent) event;
        if (event.is(CrawlerEvent.CRAWLER_RUN_BEGIN)) {
            init(crawlerEvent.getSource());
        } else if (event.is(CrawlerEvent.CRAWLER_RUN_END)) {
            doneCrawling = true;
            commitDeletions(crawlerEvent.getSource());
            close(crawlerEvent.getSource());
        } else if (event.is(CrawlerEvent.CRAWLER_STOP_END)) {
            close(crawlerEvent.getSource());
        } else if (event.is(CrawlerEvent.DOCUMENT_COMMITTED_DELETE)
                && !doneCrawling) {
            storeRejection(crawlerEvent.getCrawlDocInfo().getReference(), true);
        } else {
            storeRejection(crawlerEvent);
        }
    }

    private void init(Crawler crawler) {
        // Delete any previously created store. We do it here instead
        // of on completion in case users want to keep a record.
        crawler.getDataStoreEngine().dropStore("rejected-refs");
        this.refStore = crawler.getDataStoreEngine().openStore(
                "rejected-refs", Boolean.class);

    }
    private void close(Crawler crawler) {
        if (refStore != null) {
            refStore.close();
        }
    }

    private void storeRejection(CrawlerEvent event) {
        // does it match?
        if (!eventMatcher.matches(event.getName())) {
            LOG.trace("Event not matching event matcher: {}", event.getName());
            return;
        }

        // does it have a document reference?
        CrawlDocInfo docInfo = event.getCrawlDocInfo();
        if (docInfo == null) {
            LOG.warn("Listening for reference rejections on a crawler event "
                    + "that has no reference: {}", event.getName());
            return;
        }

        storeRejection(docInfo.getReference(), false);
    }

    private void storeRejection(String ref, boolean deletionSent) {
        // If deletionSent flag is false, check first if already there so we do
        // not risk overwriting a previously saved "true" flag.
        // If deletionSent is true, we want it to overwrite.
        // TODO do we care about synchronization?
        if (deletionSent || !refStore.find(ref).isPresent()) {
            refStore.save(ref, deletionSent);
        }
    }

    private void commitDeletions(Crawler crawler) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Committing {} rejected references for deletion...",
                    refStore.count());
        }
        refStore.forEach((ref, sent) -> {
            if (!sent) {
                crawler.getCommitterService().delete(new CrawlDoc(
                        new CrawlDocInfo(ref),
                        CachedInputStream.cache(new NullInputStream())));
            }
            return true;
        });
        LOG.info("Done committing rejected references.");
    }

    @Override
    public void loadFromXML(XML xml) {
        eventMatcher.loadFromXML(xml.getXML("eventMatcher"));
    }
    @Override
    public void saveToXML(XML xml) {
        eventMatcher.saveToXML(xml.addElement("eventMatcher"));
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
