/* Copyright 2016 Norconex Inc.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.CharEncoding;
import org.junit.Assert;
import org.junit.Test;

import com.norconex.commons.lang.file.FileUtil;


/**
 * @author Pascal Essiembre
 * @since 1.7.0
 */
public class AbstractCollectorTest {

    @Test
    public void testWriteRead() throws IOException {
        MockCollectorConfig config1 = new MockCollectorConfig();
        config1.setId("test-collector");
        config1.setJobErrorListeners(new MockJobErrorListener());
        config1.setJobLifeCycleListeners(new MockJobLifeCycleListener());
        config1.setSuiteLifeCycleListeners(new MockSuiteLifeCycleListener());
        
        
        File tempFile = File.createTempFile("AbstractCollectorTest", ".xml");
        
        // Write
        try (Writer out = new OutputStreamWriter(
                new FileOutputStream(tempFile), CharEncoding.UTF_8)) {
            config1.saveToXML(out);
        }
        
        // Read
        MockCollectorConfig config2 = new MockCollectorConfig();
        
        try (Reader in = new FileReader(tempFile)) {
            config2.loadFromXML(in);
        }

        FileUtil.delete(tempFile);

        Assert.assertEquals(config1, config2);
    }
    
    class MockCollectorConfig extends AbstractCollectorConfig {
        @Override
        protected void loadCollectorConfigFromXML(XMLConfiguration xml) {
            // TODO Auto-generated method stub
            
        }
        @Override
        protected void saveCollectorConfigToXML(Writer out) {
            // TODO Auto-generated method stub
            
        }
    }
}
