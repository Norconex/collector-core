/* Copyright 2020 Norconex Inc.
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
package com.norconex.collector.core.crawler;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.norconex.committer.core3.CommitterContext;
import com.norconex.committer.core3.CommitterException;
import com.norconex.committer.core3.UpsertRequest;
import com.norconex.committer.core3.fs.impl.XMLFileCommitter;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;


/**
 * @author Pascal Essiembre
 */
public class CrawlerCommittersTest {

    @TempDir
    public Path folder;

    @Test
    public void testMultipleCommitters() throws IOException {

        folder.toFile().setWritable(true, false);

        // create 3 Committers with the first two modifying content.
        XMLFileCommitter modifyTitle = new XMLFileCommitter() {
            @Override
            protected void doUpsert(UpsertRequest upsertRequest)
                    throws CommitterException {
                upsertRequest.getMetadata().set("title", "modified title");
                super.doUpsert(upsertRequest);

            }
        };
        modifyTitle.setIndent(4);

        XMLFileCommitter addKeyword = new XMLFileCommitter() {
            @Override
            protected void doUpsert(UpsertRequest upsertRequest)
                    throws CommitterException {
                upsertRequest.getMetadata().add("keyword", "added keyword");
                super.doUpsert(upsertRequest);
            }
        };
        addKeyword.setIndent(4);

        XMLFileCommitter xmlNoModif = new XMLFileCommitter();
        xmlNoModif.setIndent(4);

        CrawlerCommitters committers = new CrawlerCommitters(
                new CachedStreamFactory(),
                Arrays.asList(modifyTitle, addKeyword, xmlNoModif));

        // The test document
        Properties meta = new Properties();
        meta.set("title", "original title");
        meta.set("keyword", "original keyword");
        UpsertRequest req = new UpsertRequest("ref", meta,
                IOUtils.toInputStream("original content", UTF_8));


        // Perform the addition
        CommitterContext ctx =
                CommitterContext.build().setWorkDir(folder).create();
        committers.init(ctx);
        committers.upsert(req);
        committers.close();

        // Test committed data
        try (Stream<Path> subFolders = Files.list(folder)) {
            Assertions.assertEquals(3, subFolders.count());
        }

        XML xml;

        // Committer 0
        xml = getXML(0);
        Assertions.assertEquals("modified title", getTitle(xml));
        Assertions.assertEquals(1, getKeywords(xml).size());
        Assertions.assertEquals("original keyword", getKeywords(xml).get(0));
        Assertions.assertEquals("original content", getContent(xml));

        // Committer 1
        xml = getXML(1);
        Assertions.assertEquals("original title", getTitle(xml));
        Assertions.assertEquals(2, getKeywords(xml).size());
        Assertions.assertEquals("original keyword", getKeywords(xml).get(0));
        Assertions.assertEquals("added keyword", getKeywords(xml).get(1));
        Assertions.assertEquals("original content", getContent(xml));

        // Committer 2
        xml = getXML(2);
        Assertions.assertEquals("original title", getTitle(xml));
        Assertions.assertEquals(1, getKeywords(xml).size());
        Assertions.assertEquals("original keyword", getKeywords(xml).get(0));
        Assertions.assertEquals("original content", getContent(xml));
    }

    private XML getXML(int idx) throws IOException {
        try (Stream<Path> subFolders = Files.list(folder)) {
            Path xmlDir = subFolders.filter(
                    f -> f.getFileName().toString().startsWith(
                            Integer.toString(idx))).findFirst().get();
            try (Stream<Path> xmlFiles = Files.list(xmlDir)) {
                Path xmlFile = xmlFiles.findFirst().get();
                return XML.of(xmlFile).create();
            }
        }
    }
    private String getTitle(XML xml) {
        return xml.getString("upsert/metadata/meta[@name='title']");
    }
    private List<String> getKeywords(XML xml) {
        return xml.getStringList("upsert/metadata/meta[@name='keyword']");
    }
    private String getContent(XML xml) {
        return xml.getString("upsert/content");
    }
}
