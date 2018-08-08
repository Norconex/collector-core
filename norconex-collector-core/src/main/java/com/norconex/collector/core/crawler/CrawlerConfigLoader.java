/* Copyright 2014-2018 Norconex Inc.
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.CollectorException;
import com.norconex.commons.lang.config.ConfigurationLoader;
import com.norconex.commons.lang.xml.XML;


/**
 * HTTP Crawler configuration loader.
 * @author Pascal Essiembre
 */
public class CrawlerConfigLoader {

    private static final Logger LOG = LoggerFactory.getLogger(
            CrawlerConfigLoader.class);

    private final Class<? extends ICrawlerConfig> crawlerConfigClass;


    public CrawlerConfigLoader(
            Class<? extends ICrawlerConfig> crawlerConfigClass) {
        super();
        this.crawlerConfigClass = crawlerConfigClass;
    }

    /**
     * Loads crawler configurations.
     * @param configFile configuration file
     * @return crawler configs
     * @deprecated Since 2.0.0, use {@link #loadCrawlerConfigs(Path)} instead
     */
    @Deprecated
    public List<ICrawlerConfig> loadCrawlerConfigs(File configFile) {
        return loadCrawlerConfigs(configFile, null);
    }
    /**
     * Loads crawler configurations.
     * @param configFile configuration file
     * @return crawler configs
     * @since 2.0.0
     */
    public List<ICrawlerConfig> loadCrawlerConfigs(Path configFile) {
        return loadCrawlerConfigs(configFile, null);
    }
    /**
     * Loads crawler configurations.
     * @param configFile configuration file
     * @param configVariables variables file
     * @return crawler configs
     * @deprecated Since 2.0.0, use {@link #loadCrawlerConfigs(Path, Path)}
     *             instead
     */
    @Deprecated
    public List<ICrawlerConfig> loadCrawlerConfigs(
            File configFile, File configVariables) {
        Path cfg = null;
        Path vars = null;
        if (configFile != null) {
            cfg = configFile.toPath();
        }
        if (configVariables != null) {
            vars = configVariables.toPath();
        }
        return loadCrawlerConfigs(cfg, vars);
    }
    /**
     * Loads crawler configurations.
     * @param configFile configuration file
     * @param configVariables variables file
     * @return crawler configs
     * @since 2.0.0
     */
    public List<ICrawlerConfig> loadCrawlerConfigs(
            Path configFile, Path configVariables) {
        ConfigurationLoader configLoader = new ConfigurationLoader();
        XML xml = configLoader.loadXML(configFile, configVariables);
        return loadCrawlerConfigs(xml);
    }
    public List<ICrawlerConfig> loadCrawlerConfigs(XML xml) {
        try {
            XML crawlerDefaultsXML = xml.getXML("crawlerDefaults");

            List<XML> crawlersXML = xml.getXMLList("crawlers/crawler");
            List<ICrawlerConfig> configs = new ArrayList<>();
            for (XML crawlerXML : crawlersXML) {
                ICrawlerConfig  config = crawlerConfigClass.newInstance();
                if (crawlerDefaultsXML != null) {
                    loadCrawlerConfig(config, crawlerDefaultsXML);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Crawler defaults loaded for new crawler.");
                    }
                }
                loadCrawlerConfig(config, crawlerXML);
//                        XMLConfigurationUtil.newXMLConfiguration(crawlerXML));
                configs.add(config);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Crawler configuration loaded: "
                            + config.getId());
                }
            }
            return configs;
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
     * @param xml the XML representing the crawler configuration.
     * @throws IOException problem loading crawler configuration
     */
    public void loadCrawlerConfig(
            ICrawlerConfig config, XML xml) throws IOException {
        if (xml == null) {
            LOG.warn("Passing a null configuration for "
                    + config.getId() + ", skipping.");
            return;
        }
        boolean loadingDefaults =
                "crawlerDefaults".equalsIgnoreCase(xml.getName());//getRootElementName());

        if (!loadingDefaults) {
            String crawlerId = xml.getString("@id", null);
            if (StringUtils.isBlank(crawlerId)) {
                throw new CollectorException(
                        "Crawler ID is missing in configuration.");
            }
        }

        xml.configure(config);

//        XMLConfigurationUtil.loadFromXML(config, node);
    }

}
