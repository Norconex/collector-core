/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.commons.lang.config.ConfigurationLoader;
import com.norconex.commons.lang.config.ConfigurationUtil;

/**
 * HTTP Collector configuration loader.  Configuration options are defined
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
            collectorConfig.loadFromXML(ConfigurationUtil.newReader(xml));
            return collectorConfig;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CollectorException(
                    "Cannot load configuration for class: " 
                            + collectorConfigClass, e);
        }
    }

}
