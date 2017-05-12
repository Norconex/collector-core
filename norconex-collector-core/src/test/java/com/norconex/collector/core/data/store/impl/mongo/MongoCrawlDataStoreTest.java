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

import static org.junit.Assert.assertNull;

import java.io.IOException;

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
    
    @Before
    public void setup() throws Exception {
        fongo = new Fongo("mongo server 1");
        super.setup();
    }

    @Test
    public void testNoNext() throws Exception {
        assertNull(getCrawlDataStore().nextQueued());
    }

    @Override
    protected ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, TemporaryFolder tempFolder, boolean resume) {
        return new MongoCrawlDataStore(resume, 
                fongo.getMongo(), "crawl-test", new BaseMongoSerializer());
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
        System.out.println("Writing/Reading this: " + f);
        XMLConfigurationUtil.assertWriteRead(f);
    }
}
