/* Copyright 2010-2019 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.jdbc;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.norconex.collector.core.TestUtil;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.BaseCrawlDataStoreTest;
import com.norconex.commons.lang.xml.XML;


public class H2CrawlDataStoreTest extends BaseCrawlDataStoreTest {

    @Override
    protected ICrawlDataStore createCrawlDataStore(
            CrawlerConfig config, Path tempFolder, boolean resume) {

        BasicJDBCCrawlDataStoreFactory f = new BasicJDBCCrawlDataStoreFactory();
        f.setStoreDir(tempFolder);
        return f.createCrawlDataStore(config, resume);
    }

    @Test
    public void testValidation() throws IOException {
        TestUtil.testValidation(getClass());
    }

    @Test
    public void testWriteRead() throws IOException {
        BasicJDBCCrawlDataStoreFactory f = new BasicJDBCCrawlDataStoreFactory();
        XML.assertWriteRead(f, "crawlDataStoreFactory");
    }
}
