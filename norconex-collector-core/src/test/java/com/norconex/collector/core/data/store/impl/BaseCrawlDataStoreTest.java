/* Copyright 2010-2018 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.commons.lang.file.ContentType;
import com.norconex.commons.lang.xml.XML;

/**
 * Base class that includes all tests that an implementation of
 * ICrawlURLDatabase should pass.
 */
public abstract class BaseCrawlDataStoreTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private ICrawlDataStore crawlStore;
    private CrawlerConfig crawlerConfig;

    public ICrawlDataStore getCrawlDataStore() {
        return crawlStore;
    }
    public void setCrawlDataStore(ICrawlDataStore db) {
        this.crawlStore = db;
    }
    public CrawlerConfig getCrawlerConfig() {
        return crawlerConfig;
    }
    public void setCrawlerConfig(CrawlerConfig crawlerConfig) {
        this.crawlerConfig = crawlerConfig;
    }
    public TemporaryFolder getTempfolder() {
        return tempFolder;
    }

    @Before
    public void setup() throws Exception {
        crawlerConfig = createCrawlerConfig(getCrawlerId(), tempFolder);
        // the tempFolder is re-created at each test
        crawlStore = createCrawlDataStore(crawlerConfig, tempFolder, false);
    }

    @After
    public void tearDown() throws Exception {
        if (crawlStore != null) {
            crawlStore.close();
        }
    }

    protected CrawlerConfig createCrawlerConfig(
            String crawlerId, TemporaryFolder tempFolder) {

        CrawlerConfig config = new CrawlerConfig() {
            @Override
            protected void saveCrawlerConfigToXML(XML xml) {
            }
            @Override
            protected void loadCrawlerConfigFromXML(XML xml) {
            }
        };
        config.setId(crawlerId);
//        config.setWorkDir(tempFolder.getRoot().toPath());

        return config;
    }

    protected void resetDatabase(boolean resume) {
        if (crawlStore != null) {
            crawlStore.close();
        }
        crawlStore = createCrawlDataStore(getCrawlerConfig(), getTempfolder(), resume);
    }
    protected void cacheReference(String ref) {
        ICrawlData crawlData = createCrawlData(ref);
        crawlStore.processed(crawlData);
        moveProcessedToCache();
    }
    protected void cacheCrawlData(ICrawlData crawlData) {
        // To cache crawl data, it needs to be processed first, then we need to
        // transfer the processed ref to the cache.
        crawlStore.processed(crawlData);
        moveProcessedToCache();
    }
    protected void moveProcessedToCache() {
        // Resetting the database with the "resume" option disabled will
        // transfer all the processed references to the cache for most
        // implementations.
        resetDatabase(false);
    }
    protected String getCrawlerId() {
        return getClass().getSimpleName();
    }
    protected ICrawlData createCrawlData(String ref) {
        return new BaseCrawlData(ref);
    }
    protected void setCrawlState(ICrawlData crawlData, CrawlState crawlState) {
        ((BaseCrawlData) crawlData).setState(crawlState);

    }

    protected abstract ICrawlDataStore createCrawlDataStore(
            CrawlerConfig config, TemporaryFolder tempFolder, boolean resume);


    //--- Tests ----------------------------------------------------------------

    @Test
    public void testWriteReadNulls() throws Exception {
        String ref = "http://testrefnulls.com";
        ICrawlData dataIn = createCrawlData(ref);
        crawlStore.processed(dataIn);
        moveProcessedToCache();
        ICrawlData dataOut = crawlStore.getCached(ref);
        assertEquals(dataIn, dataOut);
    }
    @Test
    public void testWriteReadNoNulls() throws Exception {
        String ref = "http://testrefnonulls.com";
        BaseCrawlData dataIn = (BaseCrawlData) createCrawlData(ref);
        dataIn.setState(CrawlState.MODIFIED);
        dataIn.setMetaChecksum("metaChecksum");
        dataIn.setContentChecksum("contentChecksum");
        dataIn.setContentType(ContentType.PDF);
        dataIn.setCrawlDate(new Date());
        dataIn.setParentRootReference("parentRootReference");
        dataIn.setRootParentReference(true);
        crawlStore.processed(dataIn);
        moveProcessedToCache();
        ICrawlData dataOut = crawlStore.getCached(ref);
        assertEquals(dataIn, dataOut);
    }

    @Test
    public void testQueue() throws Exception {
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));

        // Make sure the ref is queued
        assertFalse(crawlStore.isQueueEmpty());
        assertEquals(1, crawlStore.getQueueSize());
        assertTrue(crawlStore.isQueued(ref));
    }

    @Test
    public void testNext() throws Exception {

        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));

        // Make sure the next ref is the one we just queue
        ICrawlData next = crawlStore.nextQueued();
        assertEquals(ref, next.getReference());

        // Make sure the ref was removed from queue and marked as active
        assertTrue(crawlStore.isQueueEmpty());
        assertEquals(1, crawlStore.getActiveCount());
        assertTrue(crawlStore.isActive(ref));
    }

    @Test
    public void testProcess() throws Exception {

        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));
        ICrawlData next = crawlStore.nextQueued();

        // Simulate a successful fetch

        // Mark as processed
        crawlStore.processed(next);

        // Make sure the ref was marked as processed and not active anymore
        assertTrue(crawlStore.isProcessed(ref));
        assertEquals(1, crawlStore.getProcessedCount());
        assertFalse(crawlStore.isActive(ref));
    }

    @Test
    public void testCache() throws Exception {

        // Cache an ref
        String ref = "https://www.norconex.com/";
        cacheReference(ref);

        // Make sure the ref is cached
        ICrawlData cached = crawlStore.getCached(ref);
        assertNotNull(cached);
        assertFalse(crawlStore.isCacheEmpty());
    }

    @Test
    public void testRemoveFromCacheOnProcess() throws Exception {

        // Cache an ref
        String ref = "https://www.norconex.com/";
        cacheReference(ref);

        // Process it
        crawlStore.queue(createCrawlData(ref));
        ICrawlData next = crawlStore.nextQueued();
        crawlStore.processed(next);

        // Make sure it's not cached anymore
        ICrawlData cached = crawlStore.getCached(ref);
        assertNull(cached);
    }

    @Test
    public void testCacheIterator() throws Exception {

        // Cache an ref
        String ref = "https://www.norconex.com/";
        cacheReference(ref);

        Iterator<ICrawlData> it = crawlStore.getCacheIterator();
        assertTrue(it.hasNext());
        ICrawlData httpDocReference = it.next();
        assertEquals(ref, httpDocReference.getReference());
        // read it all to be nice and have the iterator release its connection
        while (it.hasNext()) {
            it.next();
        }
    }

    @Test
    public void testQueuedUnique() throws Exception {

        String ref = "https://www.norconex.com/";
        ICrawlData crawlData = createCrawlData(ref);
        crawlStore.queue(crawlData);
        assertEquals(1, crawlStore.getQueueSize());

        // Queue the same ref. The queued size should stay the same
        crawlStore.queue(crawlData);
        assertEquals(1, crawlStore.getQueueSize());
    }

    @Test
    public void testProcessedUnique() throws Exception {

        String ref = "https://www.norconex.com/";
        ICrawlData crawlData = createCrawlData(ref);

        crawlStore.processed(crawlData);
        assertEquals(1, crawlStore.getProcessedCount());

        // Queue the same ref. The queued size should stay the same
        crawlStore.processed(crawlData);
        assertEquals(1, crawlStore.getProcessedCount());
    }

    /**
     * When instantiating a new impl with the resume option set to false, the
     * previous cache is deleted and the previous processed becomes the cache.
     * @throws Exception something went wrong
     */
    @Test
    public void testNotResume() throws Exception {

        // At this point, the cache should be empty (because the tempFolder was
        // empty)
        assertTrue(crawlStore.isCacheEmpty());

        // Simulate a successful fetch
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));
        ICrawlData next = crawlStore.nextQueued();
        crawlStore.processed(next);

        // Instantiate a new impl with the "resume" option set to false. All
        // processed refs should be moved to cache.
        resetDatabase(false);

        // Make sure the ref was cached
        ICrawlData cached = crawlStore.getCached(ref);
        assertNotNull(cached);
        assertFalse(crawlStore.isCacheEmpty());

        // Instantiate again a new impl with the "resume" option set to
        // false. There were no processed ref, so cache should be empty.
        resetDatabase(false);
        assertTrue(crawlStore.isCacheEmpty());
    }

    /**
     * When instantiating a new impl with the resume option set to false, the
     * previous cache is deleted and the previous processed becomes the cache.
     * BUT the invalid processed refs should get deleted.
     * @throws Exception something went wrong
     */
    @Test
    public void testNotResumeInvalid() throws Exception {

        // At this point, the cache should be empty (because the tempFolder was
        // empty)
        assertTrue(crawlStore.isCacheEmpty());

        // Simulate a unsuccessful fetch
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));
        ICrawlData next = crawlStore.nextQueued();
        setCrawlState(next, CrawlState.NOT_FOUND);
        crawlStore.processed(next);

        // Instantiate a new impl with the "resume" option set to false. Since
        // the ref is invalid, it should not be cached.
        resetDatabase(false);

        // Make sure the ref was NOT cached
        ICrawlData cached = crawlStore.getCached(ref);
        assertNull(cached);
        assertTrue(crawlStore.isCacheEmpty());
    }

    /**
     * When instantiating a new impl with the resume option set to true, all
     * refs should be kept in the same state, except for active refs that should
     * be queued again.
     * @throws Exception something went wrong
     */
    @Test
    public void testResumeQueued() throws Exception {

        // Queue a ref
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));

        // Instantiate a new impl with the "resume" option set to true. The
        // ref should still be queued.
        resetDatabase(true);

        // Make sure the ref is queued
        assertFalse(crawlStore.isQueueEmpty());
        assertEquals(1, crawlStore.getQueueSize());
        assertTrue(crawlStore.isQueued(ref));
    }

    /**
     * When instantiating a new impl with the resume option set to true, all
     * refs should be kept in the same state, except for active refs that should
     * be queued again.
     * @throws Exception something went wrong
     */
    @Test
    public void testResumeActived() throws Exception {

        // Activate an ref
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));
        crawlStore.nextQueued();

        // Instantiate a new impl with the "resume" option set to true. The
        // ref should be put back to queue.
        resetDatabase(true);

        // Make sure the ref is queued
        assertFalse(crawlStore.isQueueEmpty());
        assertEquals(1, crawlStore.getQueueSize());
        assertTrue(crawlStore.isQueued(ref));
    }

    @Test
    public void testResumeProcessed() throws Exception {

        // Process an ref
        String ref = "https://www.norconex.com/";
        crawlStore.queue(createCrawlData(ref));
        ICrawlData next = crawlStore.nextQueued();
        crawlStore.processed(next);

        // Instantiate a new impl with the "resume" option set to true. The
        // ref should in processed again.
        resetDatabase(true);

        // Make sure the ref is still processed
        assertTrue(crawlStore.isProcessed(ref));
        assertEquals(1, crawlStore.getProcessedCount());
    }

    @Test
    public void testResumeCached() throws Exception {

        // Cache an ref
        String ref = "https://www.norconex.com/";
        cacheReference(ref);

        // Instantiate a new impl with the "resume" option set to true. The
        // cache should still contain the ref.
        resetDatabase(true);

        // Make sure the ref is still cached
        ICrawlData cached = crawlStore.getCached(ref);
        assertNotNull(cached);
        assertFalse(crawlStore.isCacheEmpty());
    }
}
