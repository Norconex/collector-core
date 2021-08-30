/* Copyright 2021 Norconex Inc.
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
package com.norconex.collector.core.pipeline.committer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.norconex.collector.core.crawler.MockCrawler;
import com.norconex.collector.core.doc.CrawlDoc;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.xml.XML;


class DocumentChecksumStageTest {

    @TempDir
    Path tempDir;

    @Test
    void testDocumentChecksumStage() {
        CrawlDoc doc = new CrawlDoc(
                new CrawlDocInfo("http://test.com"),
                CachedInputStream.cache(toInputStream("test content", UTF_8)));

        MockCrawler crawler = new MockCrawler("id", tempDir);
        crawler.getCrawlerConfig().loadFromXML(new XML(
                "<crawler id=\"id\">"
              +   "<documentChecksummer />"
              + "</crawler>"
        ));

        DocumentPipelineContext ctx = new DocumentPipelineContext(
                crawler, doc);
        DocumentChecksumStage stage = new DocumentChecksumStage();
        stage.execute(ctx);

        Assertions.assertNull(doc.getDocInfo().getContentChecksum());
    }
}