/* Copyright 2017-2021 Norconex Inc.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.core.crawler.CrawlerConfig.OrphansStrategy;
import com.norconex.commons.lang.xml.XML;


/**
 * @author Pascal Essiembre
 */
public class CrawlerConfigTest {

    @Test
    public void testWriteRead() {
        MockCrawlerConfig c = new MockCrawlerConfig();
        c.setId("id");
        c.setMaxDocuments(33);
        c.setNumThreads(3);
        c.setOrphansStrategy(OrphansStrategy.IGNORE);
        XML.assertWriteRead(c, "crawler");
    }

    @Test
    public void testNullingDefaultsViaXml() {
        MockCrawlerConfig c = new MockCrawlerConfig();
        c.setId("id");

        // Make sure default is set
        Assertions.assertEquals(
                new MD5DocumentChecksummer(), c.getDocumentChecksummer());

        // make sure self-closed with attribute is not treated as null.
        c.loadFromXML(new XML(
                "<crawler id=\"id\">"
              +   "<documentChecksummer keep=\"true\" />"
              + "</crawler>"
        ));
        Assertions.assertTrue(
                ((MD5DocumentChecksummer) c.getDocumentChecksummer()).isKeep());

        // make sure self-closed without attribute is treated as null.
        c.loadFromXML(new XML(
                "<crawler id=\"id\">"
              +   "<documentChecksummer />"
              + "</crawler>"
        ));
        Assertions.assertNull(c.getDocumentChecksummer());

    }
}
