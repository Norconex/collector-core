/* Copyright 2014-2019 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mvstore;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;

import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.BaseCrawlDataStoreTest;

public class MVStoreCrawlDataStoreTest extends BaseCrawlDataStoreTest {

    private Path store;

    @Override
    @BeforeEach
    public void setup() throws Exception {
        store = getTempfolder().resolve("dataStore");
        super.setup();
    }
    @Override
    protected ICrawlDataStore createCrawlDataStore(
            CrawlerConfig config, Path tempFolder, boolean resume) {
        return new MVStoreCrawlDataStore(store.toString(), resume);
    }
}
