/* Copyright 2016-2019 Norconex Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.crawler.MockCrawlerConfig;
import com.norconex.collector.core.filter.impl.ExtensionReferenceFilter;
import com.norconex.committer.core.impl.FileSystemCommitter;
import com.norconex.commons.lang.xml.ErrorHandlerCapturer;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.handler.transformer.impl.ReplaceTransformer;

/**
 * @author Pascal Essiembre
 * @since 1.7.0
 */
public class CollectorTest {

    @Test
    public void testWriteRead() throws IOException {
        MockCollectorConfig config = new MockCollectorConfig();
        config.setId("test-collector");
        config.setMaxParallelCrawlers(100);
        config.setEventListeners(new MockCollectorEventListener());

        MockCrawlerConfig crawlerCfg = new MockCrawlerConfig();
        crawlerCfg.setId("myCrawler");
        crawlerCfg.setCommitter(new FileSystemCommitter());

        config.setCrawlerConfigs(new CrawlerConfig[] {crawlerCfg});

        XML.assertWriteRead(config, "collector");
    }

    @Test
    public void testOverwriteCrawlerDefaults() throws IOException {
        MockCollectorConfig cfg = new MockCollectorConfig();
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "overwrite-crawlerDefaults.xml"))) {
            XML.of(r).create().populate(cfg);
        }

        MockCrawlerConfig crawlA =
                (MockCrawlerConfig) cfg.getCrawlerConfigs().get(0);
        assertEquals( 22, crawlA.getNumThreads(),
                "crawlA");
        assertEquals( "crawlAFilter", ((ExtensionReferenceFilter)
                crawlA.getReferenceFilters().get(0)).getExtensions(),
                "crawlA");
        assertEquals( "F", ((ReplaceTransformer)
                crawlA.getImporterConfig().getPreParseHandlers().get(0))
                        .getReplacements().get("E"),
                "crawlA");
        assertTrue( CollectionUtils.isEmpty(
                crawlA.getImporterConfig().getPostParseHandlers()),
                "crawlA");
        assertEquals( "crawlACommitter", ((FileSystemCommitter)
                crawlA.getCommitter()).getDirectory(),
                "crawlA");

        MockCrawlerConfig crawlB =
                (MockCrawlerConfig) cfg.getCrawlerConfigs().get(1);
        assertEquals( 1, crawlB.getNumThreads(), "crawlB");
        assertEquals( "defaultFilter", ((ExtensionReferenceFilter)
                crawlB.getReferenceFilters().get(0)).getExtensions(),
                "crawlB");
        assertEquals( "B", ((ReplaceTransformer)
                crawlB.getImporterConfig().getPreParseHandlers().get(0))
                        .getReplacements().get("A"),
                "crawlB");
        assertEquals( "D", ((ReplaceTransformer)
                crawlB.getImporterConfig().getPostParseHandlers().get(0))
                        .getReplacements().get("C"),
                "crawlB");
        assertEquals( "defaultCommitter", ((FileSystemCommitter)
                crawlB.getCommitter()).getDirectory(),
                "crawlB");
    }


    @Test
    public void testValidation() throws IOException {
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "/validation/collector-core-full.xml"))) {
            ErrorHandlerCapturer eh = new ErrorHandlerCapturer();
            XML.of(r).setErrorHandler(eh).create().populate(
                    new MockCollectorConfig());
            assertEquals(0, eh.getErrors().size(),
                "Validation warnings/errors were found: " + eh.getErrors());
        }
    }
}
