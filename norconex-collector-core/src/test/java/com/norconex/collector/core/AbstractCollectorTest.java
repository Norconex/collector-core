/* Copyright 2016-2018 Norconex Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.MockCrawlerConfig;
import com.norconex.collector.core.filter.impl.ExtensionReferenceFilter;
import com.norconex.committer.core.impl.FileSystemCommitter;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.handler.transformer.impl.ReplaceTransformer;

/**
 * @author Pascal Essiembre
 * @since 1.7.0
 */
public class AbstractCollectorTest {

    @Test
    public void testWriteRead() throws IOException {
        MockCollectorConfig config = new MockCollectorConfig();
        config.setId("test-collector");
        config.setCollectorListeners(new MockCollectorLifeCycleListener());
//        config.setJobErrorListeners(new MockJobErrorListener());
//        config.setJobLifeCycleListeners(new MockJobLifeCycleListener());
//        config.setSuiteLifeCycleListeners(new MockSuiteLifeCycleListener());

        MockCrawlerConfig crawlerCfg = new MockCrawlerConfig();
        crawlerCfg.setId("myCrawler");
        crawlerCfg.setCommitter(new FileSystemCommitter());

        config.setCrawlerConfigs(new ICrawlerConfig[] {crawlerCfg});

        XML.assertWriteRead(config, "collector");
    }

    @Test
    public void testOverwriteCrawlerDefaults() throws IOException {
        MockCollectorConfig cfg = new MockCollectorConfig();
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "overwrite-crawlerDefaults.xml"))) {
            new XML(r).configure(cfg);
        }

        MockCrawlerConfig crawlA =
                (MockCrawlerConfig) cfg.getCrawlerConfigs().get(0);
        assertEquals("crawlA", 22, crawlA.getNumThreads());
        assertEquals("crawlA", new File("crawlAWorkdir"), crawlA.getWorkDir());
        assertEquals("crawlA", "crawlAFilter", ((ExtensionReferenceFilter)
                crawlA.getReferenceFilters().get(0)).getExtensions());
        assertEquals("crawlA", "F", ((ReplaceTransformer)
                crawlA.getImporterConfig().getPreParseHandlers().get(0))
                        .getReplacements().get("E"));
        assertTrue("crawlA", CollectionUtils.isEmpty(
                crawlA.getImporterConfig().getPostParseHandlers()));
        assertEquals("crawlA", "crawlACommitter", ((FileSystemCommitter)
                crawlA.getCommitter()).getDirectory());

        MockCrawlerConfig crawlB =
                (MockCrawlerConfig) cfg.getCrawlerConfigs().get(1);
        assertEquals("crawlB", 1, crawlB.getNumThreads());
        assertEquals("crawlB", new File("defaultWorkdir"), crawlB.getWorkDir());
        assertEquals("crawlB", "defaultFilter", ((ExtensionReferenceFilter)
                crawlB.getReferenceFilters().get(0)).getExtensions());
        assertEquals("crawlB", "B", ((ReplaceTransformer)
                crawlB.getImporterConfig().getPreParseHandlers().get(0))
                        .getReplacements().get("A"));
        assertEquals("crawlB", "D", ((ReplaceTransformer)
                crawlB.getImporterConfig().getPostParseHandlers().get(0))
                        .getReplacements().get("C"));
        assertEquals("crawlB", "defaultCommitter", ((FileSystemCommitter)
                crawlB.getCommitter()).getDirectory());
    }


    @Test
    public void testValidation() throws IOException {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "/validation/collector-core-full.xml"))) {
            assertEquals("Validation warnings/errors were found.",
                    0, new XML(r).configure(new MockCollectorConfig()).size());
        }
    }
}
