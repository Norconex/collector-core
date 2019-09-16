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
package com.norconex.collector.core;

import java.nio.file.Path;
import java.util.Optional;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.crawler.MockCrawler;

//TODO maybe move to main/java to provide test classes for collector impls?
public class MockCollector extends Collector {

    public MockCollector(String id, Path workdir) {
        super(new MockCollectorConfig());
        getCollectorConfig().setId(id);
        getCollectorConfig().setWorkDir(workdir);
    }
    public MockCollector(CollectorConfig collectorConfig) {
        super(Optional.of(collectorConfig).get());
    }

    @Override
    protected Crawler createCrawler(CrawlerConfig crawlerConfig) {
        return new MockCrawler(crawlerConfig, this);
    }

    public CrawlerConfig getFirstCrawlerConfig() {
        return getCollectorConfig().getCrawlerConfigs().get(0);
    }
}
