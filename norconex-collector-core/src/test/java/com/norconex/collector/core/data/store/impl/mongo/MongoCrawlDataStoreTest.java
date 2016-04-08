/* Copyright 2013-2016 Norconex Inc.
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

import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.BaseCrawlDataStoreTest;

public class MongoCrawlDataStoreTest extends BaseCrawlDataStoreTest {

    private DB mongoDB;
    
    @Before
    public void setup() throws Exception {
        Fongo fongo = new Fongo("mongo server 1");
        mongoDB = fongo.getDB("crawl-001");
        super.setup();
    }

    @Test
    public void testNoNext() throws Exception {
        assertNull(getCrawlDataStore().nextQueued());
    }

    @Override
    protected ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, TemporaryFolder tempFolder, boolean resume) {
        return new MongoCrawlDataStore(
                resume, mongoDB, new BaseMongoSerializer());
        // To test against a real Mongo, use:
        // db = new MongoCrawlURLDatabase(config, resume, 27017, "localhost",
        // "unit-tests-001");        
    }
}
