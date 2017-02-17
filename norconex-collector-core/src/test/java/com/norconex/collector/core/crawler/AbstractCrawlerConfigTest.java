/* Copyright 2017 Norconex Inc.
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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.norconex.collector.core.crawler.ICrawlerConfig.OrphansStrategy;
import com.norconex.commons.lang.config.XMLConfigurationUtil;

/**
 * @author Pascal Essiembre
 */
public class AbstractCrawlerConfigTest {

    @Test
    public void testWriteRead() throws IOException {
        MockCrawlerConfig c = new MockCrawlerConfig();
        c.setId("id");
        c.setMaxDocuments(33);
        c.setNumThreads(3);
        c.setOrphansStrategy(OrphansStrategy.IGNORE);
        c.setWorkDir(new File("c:\temp"));
        System.out.println("Writing/Reading this: " + c);
        XMLConfigurationUtil.assertWriteRead(c);
    }
}
