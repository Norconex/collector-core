/* Copyright 2019 Norconex Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    // Commit on every put() is required if we want to guarantee
    // recovery on a cold JVM/OS/System crash.
    //TODO if performance is too impacted, make it a configurable
    //option to offer guarantee or not?

    //TODO so we can report better... have more states? processed is vague..
    //should we have rejected/accepted instead?

    private static final String PROP_STAGE = "processingStage";
    private final IDataStore<CrawlDocInfo> store;
    private final IDataStore<CrawlDocInfo> cache;
    private boolean open;
    private String crawlerId;

    @SuppressWarnings("unchecked")
    public CrawlDocInfoService(
            String crawlerId, IDataStoreEngine storeEngine,
            Class<? extends CrawlDocInfo> type) {
        this.crawlerId = crawlerId;
        this.store = (IDataStore<CrawlDocInfo>) storeEngine.openStore(
                crawlerId + "-store", type);
        this.cache = (IDataStore<CrawlDocInfo>) storeEngine.openStore(
                crawlerId + "-cache", type);
    }

    // return true if resuming, false otherwise
    public boolean open() {
        if (open) {
            throw new IllegalStateException("Already open.");
        }

        boolean resuming = !isQueueEmpty() || !isActiveEmpty();

        if (resuming) {

            // Active -> Queued
            LOG.debug("Moving any {} active URLs back in the queue.",
                    crawlerId);
            store.modifyBy(PROP_STAGE, Stage.ACTIVE, Stage.QUEUED);

            if (LOG.isInfoEnabled()) {
                long processedCount = getProcessedCount();
                long totalCount = store.count();
                LOG.info("RESUMING {} at {} ({}/{}).",
                        crawlerId,
                        PercentFormatter.format(
                                processedCount, totalCount, 2, Locale.ENGLISH),
                        processedCount, totalCount);
            }
        } else {
            //TODO really clear cache or keep to have longer history of
            // each items?
            cache.clear();

            store.deleteBy(PROP_STAGE, Stage.ACTIVE);
            store.deleteBy(PROP_STAGE, Stage.QUEUED);

            // Valid Processed -> Cached
            LOG.debug("Caching any valid references from previous run.");

            store.findBy(PROP_STAGE, Stage.PROCESSED).forEach((ref) -> {
                if (ref.getState().isGoodState()) {
                    cache.save(ref);
                }
                store.deleteById(ref.getReference());
            });

            if (LOG.isInfoEnabled()) {
                long cacheCount = cache.count();
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

    public IDataStore<CrawlDocInfo> getDataStore() {
        return store;
    }

    public Stage getProcessingStage(String id) {
        Optional<CrawlDocInfo> ref = store.findById(id);
        if (ref.isPresent()) {
            return ref.get().getProcessingStage();
        }
        //TODO return NONE instead of null?
        return null;
    }

    //--- Active ---

    public long getActiveCount() {
        return store.countBy(PROP_STAGE, Stage.ACTIVE);
    }
    public boolean isActiveEmpty() {
        return !store.existsBy(PROP_STAGE, Stage.ACTIVE);
    }


    //--- Processed ---

    public long getProcessedCount() {
        return store.countBy(PROP_STAGE, Stage.PROCESSED);
    }
    public synchronized void processed(CrawlDocInfo crawlRef) {
        crawlRef.setProcessingStage(Stage.PROCESSED);
        store.save(crawlRef);
        boolean deleted = cache.deleteById(crawlRef.getReference());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saved processed: {} (Deleted from cache: {})",
                    crawlRef.getReference(), deleted);
        }
    }

    //--- Queue ---

    public long getQueuedCount() {
        return store.countBy(PROP_STAGE, Stage.QUEUED);
    }
    public void queue(CrawlDocInfo ref) {
        Objects.requireNonNull(ref, "'ref' must not be null.");
        ref.setProcessingStage(Stage.QUEUED);
        store.save(ref);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saved queued: {}", ref.getReference());
        }
    }
    public synchronized Optional<CrawlDocInfo> nextQueued() {
        Optional<CrawlDocInfo> ref =
                store.findFirstBy(PROP_STAGE, Stage.QUEUED);
        if (ref.isPresent()) {
            ref.get().setProcessingStage(Stage.ACTIVE);
            store.save(ref.get());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Saved active: {}", ref.get().getReference());
            }
        }
        return ref;
    }
    public boolean isQueueEmpty() {
        return !store.existsBy(PROP_STAGE, Stage.QUEUED);
    }

    //--- Cache ---

    public Optional<CrawlDocInfo> getCached(String cachedReference) {
        return cache.findById(cachedReference);
    }
    public Iterable<CrawlDocInfo> getCachedIterable() {
        return cache.findAll();
    }

    @Override
    public void close() {
        store.close();
        cache.close();
        open = false;
    }
}
