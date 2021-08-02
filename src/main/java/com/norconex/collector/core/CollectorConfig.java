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
import java.time.Duration;
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
import com.norconex.commons.lang.time.DurationParser;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.ImporterConfig;

/**
 * <p>
 * Base Collector configuration.
 * </p>
 *
 * <h3>XML Configuration</h3>
 * <p>
 * Subclasses inherit the following XML configuration items.
 * </p>
 *
 * {@nx.xml #collector
 * <workDir>
 *   (Directory where generated files are written. Defaults to "./work")
 * </workDir>
 * <tempDir>
 *   (Directory where generated files are written. Defaults to the working
 *   directory + "./temp")
 * </tempDir>
 * <eventListeners>
 *   <!-- Repeat as needed. -->
 *   <listener class="(IEventListener implementation class name.)"/>
 * </eventListeners>
 * <maxConcurrentCrawlers>
 *   (Maximum number of crawlers that can run simultaneously.
 *    Only applicable when more than one crawler is configured.
 *    Defaults to -1, unlimited.)
 * </maxConcurrentCrawlers>
 * <crawlersStartInterval>
 *   (Millisecond interval between each crawlers start. Defaut starts them
 *    all at once.)
 * </crawlersStartInterval>
 * <maxMemoryPool>
 *   (Maximum number of bytes used for memory caching of documents data. E.g.,
 *    when processing documents. Defaults to 1 GB.)
 * </maxMemoryPool>
 * <maxMemoryInstance>
 *   (Maximum number of bytes used for memory caching of each individual
 *    documents document. Defaults to 100 MB.)
 * </maxMemoryInstance>
 *
 *  <!-- maxMemoryPool combinedmaxMemoryInstance maximum number of bytes used formemory by each cached stream instance created -->
 *
 * <crawlerDefaults>
 *   <!-- All crawler options defined in a "crawler" section (except for
 *        the crawler "id") can be set here as default shared between
 *        multiple crawlers. Configuration blocks defined for a specific
 *        crawler always takes precedence. -->
 * </crawlerDefaults>
 * }
 * {@nx.xml
 * <crawlers>
 *   <!-- You need to define at least one crawler. -->
 *   <crawler id="(Unique identifier for this crawler)">
 *     <!-- Crawler settings -->
 *   </crawler>
 * </crawlers>
 * }
 *
 * <p>
 * XML configuration entries expecting millisecond durations
 * can be provided in human-readable format (English only), as per
 * {@link DurationParser} (e.g., "5 minutes and 30 seconds" or "5m30s").
 * </p>
 *
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

    private Path tempDir;
    private long maxMemoryPool = ImporterConfig.DEFAULT_MAX_MEM_POOL;
    private long maxMemoryInstance = ImporterConfig.DEFAULT_MAX_MEM_INSTANCE;

    private int maxConcurrentCrawlers = -1;
    private Duration crawlersStartInterval;

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
     * @return crawler configurations (never <code>null</code>)
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
     * When <code>null</code> the collector will use {@value #DEFAULT_WORK_DIR}
     * at runtime.
     * @return working directory path
     */
    public Path getWorkDir() {
        return workDir;
    }
    /**
     * Sets the base directory location where files created during execution
     * are created.
     * When <code>null</code> the collector will use {@value #DEFAULT_WORK_DIR}
     * at runtime.
     * @param workDir working directory path
     */
    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    /**
     * Gets the temporary directory where files can be deleted safely by the OS
     * or other processes when the collector is not running.
     * When <code>null</code> the collector will use the working directory
     * + <code>/temp</code> at runtime.
     * @return temporary directory
     * @since 3.0.0
     */
    public Path getTempDir() {
        return tempDir;
    }
    /**
    /**
     * Sets the temporary directory where files can be deleted safely by the OS
     * or other processes when the collector is not running.
     * When <code>null</code> the collector will use the working directory
     * + <code>/temp</code> at runtime.
     * @param tempDir temporary directory
     * @since 3.0.0
     */
    public void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }
    public long getMaxMemoryPool() {
        return maxMemoryPool;
    }
    public void setMaxMemoryPool(long maxMemoryPool) {
        this.maxMemoryPool = maxMemoryPool;
    }
    public long getMaxMemoryInstance() {
        return maxMemoryInstance;
    }
    public void setMaxMemoryInstance(long maxMemoryInstance) {
        this.maxMemoryInstance = maxMemoryInstance;
    }

    /**
     * Gets the maximum number of crawlers that can be executed concurrently.
     * Default is <code>-1</code>, which means no maximum.
     * @return maximum crawlers to be executed in parallel
     * @since 1.10.0
     * @deprecated Since 2.0.0, use {@link #getMaxConcurrentCrawlers()}
     */
    @Deprecated
    public int getMaxParallelCrawlers() {
        return getMaxConcurrentCrawlers();
    }
    /**
     * Sets the maximum number of crawlers that can be executed concurrently.
     * Use <code>-1</code> for no maximum.
     * @param maxParallelCrawlers number of maximum parallel crawlers
     * @since 1.10.0
     * @deprecated Since 2.0.0, use {@link #setMaxConcurrentCrawlers(int)}
     */
    @Deprecated
    public void setMaxParallelCrawlers(int maxParallelCrawlers) {
        setMaxConcurrentCrawlers(maxParallelCrawlers);
    }
    /**
     * Gets the maximum number of crawlers that can be executed concurrently.
     * Default is <code>-1</code>, which means no maximum.
     * @return maximum crawlers to be executed concurrently
     * @since 2.0.0
     */
    public int getMaxConcurrentCrawlers() {
        return maxConcurrentCrawlers;
    }
    /**
     * Sets the maximum number of crawlers that can be executed concurrently.
     * Use <code>-1</code> for no maximum.
     * @param maxConcurrentCrawlers maximum number of concurrent crawlers
     * @since 2.0.0
     */
    public void setMaxConcurrentCrawlers(int maxConcurrentCrawlers) {
        this.maxConcurrentCrawlers = maxConcurrentCrawlers;
    }

    /**
     * Gets the amount of time between each concurrent crawlers are started.
     * Default is <code>null</code> (does not wait before launching concurrent
     * crawlers).
     * @return duration
     * @since 2.0.0
     */
    public Duration getCrawlersStartInterval() {
        return crawlersStartInterval;
    }
    /**
     * Sets the amount of time in between each concurrent crawlers are started.
     * @param crawlersStartInterval amount of time
     * @since 2.0.0
     */
    public void setCrawlersStartInterval(Duration crawlersStartInterval) {
        this.crawlersStartInterval = crawlersStartInterval;
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
        xml.addElement("tempDir", getTempDir());
        xml.addElement("maxConcurrentCrawlers", getMaxConcurrentCrawlers());
        xml.addElement("crawlersStartInterval", getCrawlersStartInterval());
        xml.addElement("maxMemoryPool", getMaxMemoryPool());
        xml.addElement("maxMemoryInstance", getMaxMemoryInstance());
        xml.addElementList("eventListeners", "listener", eventListeners);
        xml.addElementList("crawlers", "crawler", getCrawlerConfigs());
        saveCollectorConfigToXML(xml);
    }
    protected abstract void saveCollectorConfigToXML(XML xml);

    @Override
    public final void loadFromXML(XML xml) {
        xml.checkDeprecated(
                "maxParallelCrawlers", "maxConcurrentCrawlers", true);

        long then = System.currentTimeMillis();
        String collectorId = xml.getString("@id", null);
        if (StringUtils.isBlank(collectorId)) {
            throw new CollectorException(
                    "Collector id attribute is mandatory.");
        }
        setId(collectorId);
        setWorkDir(xml.getPath("workDir", getWorkDir()));
        setTempDir(xml.getPath("tempDir", getTempDir()));
        setEventListeners(xml.getObjectListImpl(IEventListener.class,
                "eventListeners/listener", eventListeners));
        setMaxConcurrentCrawlers(xml.getInteger(
                "maxConcurrentCrawlers", getMaxConcurrentCrawlers()));
        setCrawlersStartInterval(xml.getDuration(
                "crawlersStartInterval", getCrawlersStartInterval()));
        setMaxMemoryPool(xml.getDataSize("maxMemoryPool", getMaxMemoryPool()));
        setMaxMemoryInstance(
                xml.getDataSize("maxMemoryInstance", getMaxMemoryInstance()));

        if (crawlerConfigClass != null) {
            List<CrawlerConfig> cfgs = new CrawlerConfigLoader(
                    crawlerConfigClass).loadCrawlerConfigs(xml);
            if (CollectionUtils.isNotEmpty(cfgs)) {
                setCrawlerConfigs(cfgs);
            }
        }

        loadCollectorConfigFromXML(xml);

        long elapsed = System.currentTimeMillis() - then;
        if (LOG.isDebugEnabled()) {
            LOG.debug("\"{}\" Collector XML configuration loaded in {} "
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
