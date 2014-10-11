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
package com.norconex.collector.core.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.CollectorException;
import com.norconex.commons.lang.config.ConfigurationLoader;
import com.norconex.commons.lang.config.ConfigurationUtil;

/**
 * HTTP Crawler configuration loader.
 * @author Pascal Essiembre
 */
@SuppressWarnings("nls")
public class CrawlerConfigLoader {

    private static final Logger LOG = LogManager.getLogger(
            CrawlerConfigLoader.class);

    private final Class<? extends ICrawlerConfig> crawlerConfigClass;

    
    public CrawlerConfigLoader(
            Class<? extends ICrawlerConfig> crawlerConfigClass) {
        super();
        this.crawlerConfigClass = crawlerConfigClass;
    }

    public ICrawlerConfig[] loadCrawlerConfigs(File configFile) {
        return loadCrawlerConfigs(configFile, null);
    }
    
    public ICrawlerConfig[] loadCrawlerConfigs(
            File configFile, File configVariables) {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        XMLConfiguration xml = configLoader.loadXML(
                configFile, configVariables);
        return loadCrawlerConfigs(xml);
    }
    
    public ICrawlerConfig[] loadCrawlerConfigs(
            HierarchicalConfiguration xml) {
        try {
            XMLConfiguration defaults = 
                    ConfigurationUtil.getXmlAt(xml, "crawlerDefaults");
            
            ICrawlerConfig  defaultConfig = crawlerConfigClass.newInstance();

            if (defaults != null) {
                loadCrawlerConfig(defaultConfig, defaults);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Crawler defaults loaded.");
                }
            }
            
            List<HierarchicalConfiguration> nodes = 
                    xml.configurationsAt("crawlers.crawler");
            List<ICrawlerConfig> configs = new ArrayList<>();
            for (HierarchicalConfiguration node : nodes) {
                ICrawlerConfig config = defaultConfig.clone();
                loadCrawlerConfig(config, 
                        ConfigurationUtil.newXMLConfiguration(node));
                configs.add(config);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Crawler configuration loaded: "
                            + config.getId());
                }
            }
            return configs.toArray(new ICrawlerConfig[]{});
        } catch (Exception e) {
            throw new CollectorException(
                    "Cannot load crawler configurations.", e);
        }
    }

    /**
     * Loads a crawler configuration, which can be either the default
     * crawler or real crawler configuration instances
     * (keeping defaults).
     * @param config crawler configuration to populate/overwrite
     * @param node the node representing the crawler configuration.
     * @throws IOException problem loading crawler configuration
     */
    public void loadCrawlerConfig(
            ICrawlerConfig config, XMLConfiguration node) throws IOException {
        if (node == null) {
            LOG.warn("Passing a null configuration for " 
                    + config.getId() + ", skipping.");
            return;
        }
        boolean loadingDefaults = 
                "crawlerDefaults".equalsIgnoreCase(node.getRootElementName());
        
        if (!loadingDefaults) {
            String crawlerId = node.getString("[@id]", null);
            if (StringUtils.isBlank(crawlerId)) {
                throw new CollectorException(
                        "Crawler ID is missing in configuration.");
            }
        }
        config.loadFromXML(ConfigurationUtil.newReader(node));
    }
        
}
