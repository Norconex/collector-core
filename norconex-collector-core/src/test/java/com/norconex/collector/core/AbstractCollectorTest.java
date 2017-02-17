/* Copyright 2016-2017 Norconex Inc.
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.MockCrawlerConfig;
import com.norconex.committer.core.impl.FileSystemCommitter;
import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.log.CountingConsoleAppender;


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
        config.setJobErrorListeners(new MockJobErrorListener());
        config.setJobLifeCycleListeners(new MockJobLifeCycleListener());
        config.setSuiteLifeCycleListeners(new MockSuiteLifeCycleListener());
        
        MockCrawlerConfig crawlerCfg = new MockCrawlerConfig();
        crawlerCfg.setId("myCrawler");
        crawlerCfg.setCommitter(new FileSystemCommitter());
        
        config.setCrawlerConfigs(new ICrawlerConfig[] {crawlerCfg});
        
        
        System.out.println("Writing/Reading this: " + config);
        XMLConfigurationUtil.assertWriteRead(config);
    }
    
    
    @Test
    public void testValidation() throws IOException {
        CountingConsoleAppender appender = new CountingConsoleAppender();
        appender.startCountingFor(XMLConfigurationUtil.class, Level.WARN);
        try (Reader r = new InputStreamReader(getClass().getResourceAsStream(
                "/validation/collector-core-full.xml"))) {
            XMLConfigurationUtil.loadFromXML(new MockCollectorConfig(), r);
        } finally {
            appender.stopCountingFor(XMLConfigurationUtil.class);
        }
        Assert.assertEquals("Validation warnings/errors were found.", 
                0, appender.getCount());
    }
}
