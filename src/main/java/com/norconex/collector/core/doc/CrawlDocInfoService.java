/* Copyright 2019-2021 Norconex Inc.
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
package com.norconex.collector.core.doc;

import java.io.Closeable;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.doc.CrawlDocInfo.Stage;
import com.norconex.collector.core.store.IDataStore;
import com.norconex.collector.core.store.IDataStoreEngine;
import com.norconex.commons.lang.PercentFormatter;

public class CrawlDocInfoService implements Closeable {

    private static final Logger LOG =
            LoggerFactory.getLogger(CrawlDocInfoService.class);

//    * The few stages a reference should have in most implementations are:</p>
//    * <ul>
//    *   <li><b>Queued:</b> References extracted from documents are first queued for
//    *       future processing.</li>
//    *   <li><b>Active:</b> A reference is being processed.</li>
//    *   <li><b>Processed:</b> A reference has been processed.  If the same URL is
//    *       encountered again during the same run, it will be ignored.</li>
//    *   <li><b>Cached:</b> When crawling is over, processed references will be
//    *       cached on the next run.</li>
//    * </ul>

    //TODO Commit on every put() is required if we want to guarantee
    // recovery on a cold JVM/OS/System crash. But do we care to reprocess
    // a handful of docs?
    //TODO if performance is too impacted, make it a configurable
    //option to offer guarantee or not?

    //TODO so we can report better... have more states? processed is vague..
    //should we have rejected/accepted instead?

//    private static final String PROP_STAGE = "processingStage";

    // new ones
    private IDataStore<CrawlDocInfo> queue;
    private IDataStore<CrawlDocInfo> active;
    //TODO split into rejected/accepted?
    private IDataStore<CrawlDocInfo> processed;
    private IDataStore<CrawlDocInfo> cached;
    private Class<? extends CrawlDocInfo> type;

    private final Crawler crawler;

    private boolean open;

    public CrawlDocInfoService(
            Crawler crawler, Class<? extends CrawlDocInfo> type) {
        this.crawler = Objects.requireNonNull(
                crawler, "'crawler' must not be null.");
        this.type = Objects.requireNonNull(
                type, "'type' must not be null.");
    }

    // return true if resuming, false otherwise
    public boolean open() {
        if (open) {
            throw new IllegalStateException("Already open.");
        }

        IDataStoreEngine storeEngine = crawler.getDataStoreEngine();

        queue = storeEngine.openStore("queued", type);
        active = storeEngine.openStore("active", type);
        processed = storeEngine.openStore("processed", type);
        cached = storeEngine.openStore("cached", type);

        boolean resuming = !isQueueEmpty() || !isActiveEmpty();

        if (resuming) {

            // Active -> Queued
            LOG.debug("Moving any {} active URLs back into queue.",
                    crawler.getId());
            active.forEach((k, v) -> {
                queue.save(k, v);
                return true;
            });
            active.clear();

            if (LOG.isInfoEnabled()) {
                //TODO use total count to track progress independently
                long processedCount = getProcessedCount();
                long totalCount =
                        processedCount + queue.count() + cached.count();
                LOG.info("RESUMING \"{}\" at {} ({}/{}).",
                        crawler.getId(),
                        PercentFormatter.format(
                                processedCount, totalCount, 2, Locale.ENGLISH),
                        processedCount, totalCount);
            }
        } else {
            //TODO really clear cache or keep to have longer history of
            // each items?
            cached.clear();
            active.clear();
            queue.clear();

            // Valid Processed -> Cached
            LOG.debug("Caching any valid references from previous run.");

            //TODO make swap a method on store engine?

            // cached -> swap
            storeEngine.renameStore(cached, "swap");
            IDataStore<CrawlDocInfo> swap = cached;

            // processed -> cached
            storeEngine.renameStore(processed, "cached");
            cached = processed;

            // swap -> processed
            storeEngine.renameStore(swap, "processed");
            processed = swap;


            if (LOG.isInfoEnabled()) {
                long cacheCount = cached.count();
                if (cacheCount > 0) {
                    LOG.info("STARTING an incremental crawl from previous {} "
                            + "valid references.", cacheCount);
                } else {
                    LOG.info("STARTING a fresh crawl.");
                }
            }
        }

        open = true;
        return resuming;
    }

    public Stage getProcessingStage(String id) {
        if (active.exists(id)) {
            return Stage.ACTIVE;
        }
        if (queue.exists(id)) {
            return Stage.QUEUED;
        }
        if (processed.exists(id)) {
            return Stage.PROCESSED;
        }
        return null;
    }

    //--- Active ---

    public long getActiveCount() {
        return active.count();
    }
    public boolean isActiveEmpty() {
        return active.isEmpty();
    }
    public boolean forEachActive(BiPredicate<String, CrawlDocInfo> predicate) {
        return active.forEach(predicate);
    }

    //--- Processed ---

    public long getProcessedCount() {
        return processed.count();
    }
    public boolean isProcessedEmpty() {
        return processed.isEmpty();
    }
    public Optional<CrawlDocInfo> getProcessed(String id) {
        return processed.find(id);
    }

    public synchronized void processed(CrawlDocInfo docInfo) {
        Objects.requireNonNull(docInfo, "'docInfo' must not be null.");
        processed.save(docInfo.getReference(), docInfo);
        boolean cacheDeleted = cached.delete(docInfo.getReference());
        boolean activeDeleted = active.delete(docInfo.getReference());
        LOG.debug("Saved processed: {} "
                + "(Deleted from cache: {}; Deleted from active: {})",
                docInfo.getReference(), cacheDeleted, activeDeleted);
        crawler.getEventManager().fire(new CrawlerEvent.Builder(
                CrawlerEvent.DOCUMENT_PROCESSED, crawler)
                    .crawlDocInfo(docInfo)
                    .build());
    }
    public boolean forEachProcessed(
            BiPredicate<String, CrawlDocInfo> predicate) {
        return processed.forEach(predicate);
    }

    //--- Queue ---

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public long getQueueCount() {
        return queue.count();
    }
    public void queue(CrawlDocInfo docInfo) {
        Objects.requireNonNull(docInfo, "'docInfo' must not be null.");
        queue.save(docInfo.getReference(), docInfo);
        LOG.debug("Saved queued: {}", docInfo.getReference());
        crawler.getEventManager().fire(new CrawlerEvent.Builder(
                CrawlerEvent.DOCUMENT_QUEUED, crawler)
                    .crawlDocInfo(docInfo)
                    .build());
    }
    // get and delete and mark as active
    public synchronized Optional<CrawlDocInfo> pollQueue() {
        Optional<CrawlDocInfo> docInfo = queue.deleteFirst();
        if (docInfo.isPresent()) {
            active.save(docInfo.get().getReference(), docInfo.get());
            LOG.debug("Saved active: {}", docInfo.get().getReference());
        }
        return docInfo;
    }
    public boolean forEachQueued(
            BiPredicate<String, CrawlDocInfo> predicate) {
        return queue.forEach(predicate);
    }


    //--- Cache ---

    public Optional<CrawlDocInfo> getCached(String id) {
        return cached.find(id);
    }
    public boolean forEachCached(
            BiPredicate<String, CrawlDocInfo> predicate) {
        return cached.forEach(predicate);
    }



    @Override
    public void close() {
        open = false;
    }
}
