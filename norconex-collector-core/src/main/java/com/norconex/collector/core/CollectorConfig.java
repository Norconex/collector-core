/* Copyright 2014-2020 Norconex Inc.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.crawler.CrawlerConfigLoader;
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.event.IEventListener;
import com.norconex.commons.lang.time.DurationFormatter;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.ImporterConfig;

/**
 * Base Collector configuration.
 * @author Pascal Essiembre
 */
public abstract class CollectorConfig implements IXMLConfigurable {

    private static final Logger LOG = LoggerFactory.getLogger(
            CollectorConfig.class);

    /** Default relative directory where progress files are stored. */
    public static final Path DEFAULT_WORK_DIR = Paths.get("./work");

    //TODO still needed?
    private final Class<? extends CrawlerConfig> crawlerConfigClass;

    private String id;
    private final List<CrawlerConfig> crawlerConfigs = new ArrayList<>();

    private Path workDir = DEFAULT_WORK_DIR;

    // tempDir are for files that can be deleted by the OS or else.
    // default to system temp dir.
    // when left null, defaults to collector workdir + /temp
    private Path tempDir = null;
    private int maxMemoryPool = ImporterConfig.DEFAULT_MAX_MEM_POOL;
    private int maxMemoryInstance = ImporterConfig.DEFAULT_MAX_MEM_INSTANCE;

    private int maxParallelCrawlers = -1;

    private final List<IEventListener<?>> eventListeners = new ArrayList<>();

    public CollectorConfig() {
        this((Class<? extends CrawlerConfig>) null);
    }
    public CollectorConfig(
            Class<? extends CrawlerConfig> crawlerConfigClass) {
        this.crawlerConfigClass = crawlerConfigClass;
    }

	/**
	 * Gets this collector unique identifier.
	 * @return unique identifier
	 */
    public String getId() {
        return id;
    }
    /**
     * Sets this collector unique identifier. It is important
     * the id of the collector is unique amongst your collectors.  This
     * facilitates integration with different systems and facilitates
     * tracking.
     * @param id unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets crawler configurations.
     * @return crawler configurations
     * @since 1.7.0
     */
    public List<CrawlerConfig> getCrawlerConfigs() {
        return Collections.unmodifiableList(crawlerConfigs);
    }
    /**
     * Sets crawler configurations.
     * @param crawlerConfigs crawler configurations
     */
    public void setCrawlerConfigs(CrawlerConfig... crawlerConfigs) {
        setCrawlerConfigs(Arrays.asList(crawlerConfigs));
    }
    /**
     * Sets crawler configurations.
     * @param crawlerConfigs crawler configurations
     * @since 2.0.0
     */
    public void setCrawlerConfigs(List<CrawlerConfig> crawlerConfigs) {
        CollectionUtil.setAll(this.crawlerConfigs, crawlerConfigs);
    }

    /**
     * Gets the base directory location where files created during execution
     * are created.
     * @return working directory path
     */
    public Path getWorkDir() {
        return workDir;
    }
    /**
     * Sets the base directory location where files created during execution
     * are created.
     * @param workDir working directory path
     */
    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    //TODO document these and their defaults
    //TODO since 3.0.0
    public Path getTempDir() {
        return tempDir;
    }
    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }
    public int getMaxMemoryPool() {
        return maxMemoryPool;
    }
    public void setMaxMemoryPool(int maxMemoryPool) {
        this.maxMemoryPool = maxMemoryPool;
    }
    public int getMaxMemoryInstance() {
        return maxMemoryInstance;
    }
    public void setMaxMemoryInstance(int maxMemoryInstance) {
        this.maxMemoryInstance = maxMemoryInstance;
    }

    /**
     * Gets the maximum number of crawlers that can be executed in parallel at
     * any given time.
     * Default is <code>-1</code>, which means no maximum.
     * @return maximum crawlers to be executed in parallel
     * @since 1.10.0
     */
    public int getMaxParallelCrawlers() {
        return maxParallelCrawlers;
    }
    /**
     * Sets the maximum number of crawlers that can be executed in parallel at
     * any given time.
     * Use <code>-1</code> for no maximum.
     * @param maxParallelCrawlers number of maximum parallel crawlers
     * @since 1.10.0
     */
    public void setMaxParallelCrawlers(int maxParallelCrawlers) {
        this.maxParallelCrawlers = maxParallelCrawlers;
    }


    /**
     * Gets event listeners.
     * Those are considered additions to automatically
     * detected configuration objects implementing {@link IEventListener}.
     * @return event listeners.
     * @since 2.0.0
     */
    public List<IEventListener<?>> getEventListeners() {
        return Collections.unmodifiableList(eventListeners);
    }
    /**
     * Sets event listeners.
     * Those are considered additions to automatically
     * detected configuration objects implementing {@link IEventListener}.
     * @param eventListeners event listeners.
     * @since 2.0.0
     */
    public void setEventListeners(IEventListener<?>... eventListeners) {
        setEventListeners(Arrays.asList(eventListeners));
    }
    /**
     * Sets event listeners.
     * Those are considered additions to automatically
     * detected configuration objects implementing {@link IEventListener}.
     * @param eventListeners event listeners.
     * @since 2.0.0
     */
    public void setEventListeners(List<IEventListener<?>> eventListeners) {
        CollectionUtil.setAll(this.eventListeners, eventListeners);
    }
    /**
     * Adds event listeners.
     * Those are considered additions to automatically
     * detected configuration objects implementing {@link IEventListener}.
     * @param eventListeners event listeners.
     * @since 2.0.0
     */
    public void addEventListeners(IEventListener<?>... eventListeners) {
        addEventListeners(Arrays.asList(eventListeners));
    }
    /**
     * Adds event listeners.
     * Those are considered additions to automatically
     * detected configuration objects implementing {@link IEventListener}.
     * @param eventListeners event listeners.
     * @since 2.0.0
     */
    public void addEventListeners(List<IEventListener<?>> eventListeners) {
        this.eventListeners.addAll(eventListeners);
    }
    /**
     * Clears all event listeners. The automatically
     * detected configuration objects implementing {@link IEventListener}
     * are not cleared.
     * @since 2.0.0
     */
    public void clearEventListeners() {
        this.eventListeners.clear();
    }

    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("id", getId());
        xml.addElement("workDir", getWorkDir());
        xml.addElement("maxParallelCrawlers", getMaxParallelCrawlers());
        xml.addElementList("eventListeners", "listener", eventListeners);
        xml.addElementList("crawlers", "crawler", getCrawlerConfigs());
        saveCollectorConfigToXML(xml);
    }
    protected abstract void saveCollectorConfigToXML(XML xml);

    @Override
    public final void loadFromXML(XML xml) {
        long then = System.currentTimeMillis();
        String collectorId = xml.getString("@id", null);
        if (StringUtils.isBlank(collectorId)) {
            throw new CollectorException(
                    "Collector id attribute is mandatory.");
        }
        setId(collectorId);
        setWorkDir(xml.getPath("workDir", getWorkDir()));
        setMaxParallelCrawlers(xml.getInteger(
                "maxParallelCrawlers", getMaxParallelCrawlers()));
        setEventListeners(xml.getObjectListImpl(
                IEventListener.class, "eventListeners/listener", eventListeners));

        if (crawlerConfigClass != null) {
            List<CrawlerConfig> cfgs = new CrawlerConfigLoader(
                    crawlerConfigClass).loadCrawlerConfigs(xml);
            if (CollectionUtils.isNotEmpty(cfgs)) {
                setCrawlerConfigs(cfgs);
            }
        }

        loadCollectorConfigFromXML(xml);

        long elapsed = System.currentTimeMillis() - then;
        //TODO make debug?
        if (LOG.isInfoEnabled()) {
            LOG.info("\"{}\" Collector XML configuration loaded in {} "
                    + "(workdir={})", collectorId,
                    DurationFormatter.FULL.format(elapsed), workDir);
        }
    }

    protected abstract void loadCollectorConfigFromXML(XML xml);

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
