/* Copyright 2014-2017 Norconex Inc.
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
import java.io.IOException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.commons.lang.config.ConfigurationLoader;
import com.norconex.commons.lang.config.XMLConfigurationUtil;

/**
 * Collector configuration loader.  Configuration options are defined
 * as part of general product documentation.
 * @author Pascal Essiembre
 */
public class CollectorConfigLoader {

    private static final Logger LOG = LogManager.getLogger(
            CollectorConfigLoader.class);

    private final Class<? extends ICollectorConfig> collectorConfigClass;
    
    public CollectorConfigLoader(
            Class<? extends ICollectorConfig> collectorConfigClass) {
        super();
        this.collectorConfigClass = collectorConfigClass;
    }
    
    /**
     * Loads a collection configuration from file.
     * @param configFile configuration file
     * @return collector configuration
     * @throws IOException Could not load collector configuration
     */
    public ICollectorConfig loadCollectorConfig(File configFile) throws IOException {
        return loadCollectorConfig(configFile, null);
    }
    
    /**
     * Loads a collection configuration from file.
     * @param configFile configuration file
     * @param configVariables configuration variables file
     * @return collector configuration
     * @throws IOException Could not load collector configuration
     */
    public ICollectorConfig loadCollectorConfig(
            File configFile, File configVariables) throws IOException {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading configuration file: " + configFile);
        }
        if (!configFile.exists()) {
            return null;
        }
        
        ConfigurationLoader configLoader = new ConfigurationLoader();
        XMLConfiguration xml = configLoader.loadXML(
                configFile, configVariables);
        try {
            ICollectorConfig collectorConfig = 
                    collectorConfigClass.newInstance();
            collectorConfig.loadFromXML(XMLConfigurationUtil.newReader(xml));
            return collectorConfig;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CollectorException(
                    "Cannot load configuration for class: " 
                            + collectorConfigClass, e);
        }
    }
}
