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
package com.norconex.collector.core.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.doc.CrawlDoc;
import com.norconex.committer.core3.CommitterContext;
import com.norconex.committer.core3.CommitterException;
import com.norconex.committer.core3.DeleteRequest;
import com.norconex.committer.core3.ICommitter;
import com.norconex.committer.core3.UpsertRequest;
import com.norconex.commons.lang.collection.CollectionUtil;

/**
 * Wrapper around multiple Committers so they can all be handled as one.
 * @author Pascal Essiembre
 * @since 3.0.0
 */
public class CrawlerCommitterService {

    private static final Logger LOG =
            LoggerFactory.getLogger(CrawlerCommitterService.class);

    private final List<ICommitter> committers = new ArrayList<>();
    private final Crawler crawler;

    public CrawlerCommitterService(Crawler crawler) {
        super();
        CollectionUtil.setAll(
                committers, crawler.getCrawlerConfig().getCommitters());
        this.crawler = crawler;

    }

    public boolean isEmpty() {
        return committers.isEmpty();
    }

    public void init(CommitterContext baseContext) {
        MutableInt idx = new MutableInt();
        executeAll("init", c -> {
            CommitterContext ctx = baseContext.withWorkdir(
                    baseContext.getWorkDir().resolve(idx.toString()));
            idx.increment();
            c.init(ctx);
        });
    }

    /**
     * Updates or inserts a document using all accepting committers.
     * @param doc the document to upsert
     * @return committers having accepted/upserted the document
     */
    public List<ICommitter> upsert(CrawlDoc doc) {
        List<ICommitter> actuals = new ArrayList<>();
        if (!committers.isEmpty()) {
            executeAll("upsert", c -> {
                UpsertRequest req = toUpserRequest(doc);
                if (c.accept(req)) {
                    actuals.add(c);
                    c.upsert(req);
                    doc.getInputStream().rewind();
                }
            });
        }
        fireCommitterRequestEvent(
                CrawlerEvent.DOCUMENT_COMMITTED_UPSERT, actuals, doc);

        return actuals;
    }

    /**
     * Delete a document operation using all accepting committers.
     * @param doc the document to delete
     * @return committers having accepted/deleted the document
     */
    public List<ICommitter> delete(CrawlDoc doc) {
        List<ICommitter> actuals = new ArrayList<>();
        if (!committers.isEmpty()) {
            executeAll("delete", c -> {
                DeleteRequest req = toDeleteRequest(doc);
                if (c.accept(req)) {
                    actuals.add(c);
                    c.delete(req);
                    // no doc content rewind necessary
                }
            });
        }
        fireCommitterRequestEvent(
                CrawlerEvent.DOCUMENT_COMMITTED_DELETE, actuals, doc);
        return actuals;
    }

    public void close() {
        executeAll("close", ICommitter::close);
    }

    public void clean() {
        executeAll("clean", ICommitter::clean);
    }

    private void executeAll(String operation, CommitterConsumer consumer) {
        List<String> failures = new ArrayList<>();
        for (ICommitter committer : committers) {
            try {
                consumer.accept(committer);
            } catch (CommitterException e) {
                LOG.error("Could not execute \"{}\" on committer: {}",
                        operation, committer, e);
                failures.add(committer.getClass().getSimpleName());
            }
        }
        if (!failures.isEmpty()) {
            throw new CollectorException(
                    "Could not execute \"" + operation + "\" on "
                    + failures.size() + " committer(s): \""
                    + StringUtils.join(failures, ", ")
                    + "\". Check the logs for more details.");
        }
    }

    // invoked for each committer to avoid tempering
    private UpsertRequest toUpserRequest(CrawlDoc doc) {
        return new UpsertRequest(
                doc.getReference(),
                doc.getMetadata(),
                doc.getInputStream());
    }
    // invoked for each committer to avoid tempering
    private DeleteRequest toDeleteRequest(CrawlDoc doc) {
        return new DeleteRequest(doc.getReference(), doc.getMetadata());
    }

    private void fireCommitterRequestEvent(
            String eventName, List<ICommitter> targets, CrawlDoc doc) {
        String msg = "Committers: " + (
                targets.isEmpty()
                ? "none"
                : targets.stream().map(c -> c.getClass().getSimpleName())
                        .collect(Collectors.joining(",")));
        crawler.getEventManager().fire(new CrawlerEvent.Builder(
                eventName, crawler)
                    .crawlDocInfo(doc.getDocInfo())
                    .subject(targets)
                    .message(msg)
                    .build());
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
        return CrawlerCommitterService.class.getSimpleName() + '[' +
                committers.stream().map(c -> c.getClass().getSimpleName())
                        .collect(Collectors.joining(",")) + ']';
    }

    @FunctionalInterface
    private interface CommitterConsumer {
        public void accept(ICommitter c) throws CommitterException;
    }
}
