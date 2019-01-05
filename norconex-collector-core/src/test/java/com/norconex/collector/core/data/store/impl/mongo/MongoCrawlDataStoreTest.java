/* Copyright 2013-2017 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.fakemongo.Fongo;
import com.norconex.collector.core.TestUtil;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.BaseCrawlDataStoreTest;
import com.norconex.commons.lang.config.XMLConfigurationUtil;

public class MongoCrawlDataStoreTest extends BaseCrawlDataStoreTest {

    private Fongo fongo;

    @Override
    @Before
    public void setup() throws Exception {
        fongo = new Fongo("mongo server 1");
        super.setup();
    }

    @Test
    public void testNoNext() throws Exception {
        assertNull(getCrawlDataStore().nextQueued());
    }

//    @Override
//    protected ICrawlDataStore createCrawlDataStore(
//            ICrawlerConfig config, TemporaryFolder tempFolder, boolean resume) {
//
//        MongoConnectionDetails conn = new MongoConnectionDetails();
//        conn.setDatabaseName("testdb");
//        conn.setHost("localhost");
//        conn.setPort(27017);
//        return new MongoCrawlDataStore("crawl-test", resume,
//                conn, new BaseMongoSerializer());
//    }


    @Override
    protected ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, TemporaryFolder tempFolder, boolean resume) {
        return new MongoCrawlDataStore(resume,
                fongo.getMongo(), "crawl-test", new BaseMongoSerializer());
    }

    //TODO make this a test for all implementations?
    @Test
    public void testQueueVeryLongId() throws Exception {
        String ref = "https://www.norconex.com/verylong"
                + StringUtils.repeat("-filler", 500);
        ICrawlDataStore crawlStore = getCrawlDataStore();
        crawlStore.queue(createCrawlData(ref));

        // Make sure the long ref is queued and is the same when retrieved
        assertFalse(crawlStore.isQueueEmpty());
        assertEquals(1, crawlStore.getQueueSize());
        assertTrue(crawlStore.isQueued(ref));
        assertEquals(ref, crawlStore.nextQueued().getReference());
    }

    @Test
    public void testValidation() throws IOException {
        TestUtil.testValidation(getClass());
    }

    @Test
    public void testWriteRead() throws IOException {
        MockMongoCrawlDataStoreFactory f = new MockMongoCrawlDataStoreFactory();
        f.getConnectionDetails().setDatabaseName("dbName");
        f.getConnectionDetails().setHost("host");
        f.getConnectionDetails().setPort(123);
        f.getConnectionDetails().setUsername("username");
        f.getConnectionDetails().setPassword("password");
        f.getConnectionDetails().setMechanism("MONGODB-CR");
        f.getConnectionDetails().setSslEnabled(true);
        f.getConnectionDetails().setSslInvalidHostNameAllowed(true);
        f.setCachedCollectionName("mycache");
        f.setReferencesCollectionName("myrefs");
        System.out.println("Writing/Reading this: " + f);
        XMLConfigurationUtil.assertWriteRead(f);
    }
}
