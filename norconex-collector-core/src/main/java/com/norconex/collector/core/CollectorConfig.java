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

//    /** Default relative directory where logs from Log4j are stored. */
//    public static final Path DEFAULT_LOGS_DIR = Paths.get("./logs");
//    /** Default relative directory where progress files are stored. */
//    public static final Path DEFAULT_PROGRESS_DIR = Paths.get("./progress");
    /** Default relative directory where progress files are stored. */
    public static final Path DEFAULT_WORK_DIR = Paths.get("./work");

    //TODO still needed?
    private final Class<? extends CrawlerConfig> crawlerConfigClass;
    //private final String xmlConfigRootTag;

    private String id;
    private final List<CrawlerConfig> crawlerConfigs = new ArrayList<>();

    private Path workDir = DEFAULT_WORK_DIR;

    // tempDir are for files that can be deleted by the OS or else.
    // default to system temp dir.
//    private Path tempDir = Paths.get(FileUtils.getTempDirectoryPath());
    private Path tempDir = null;// when null, defaults to collector workdir + /temp
    private int maxMemoryPool = ImporterConfig.DEFAULT_MAX_MEM_POOL;
    private int maxMemoryInstance = ImporterConfig.DEFAULT_MAX_MEM_INSTANCE;
//    private int maxFileCacheSize = DEFAULT_MAX_FILE_CACHE_SIZE;
//    private int maxFilePoolCacheSize = DEFAULT_MAX_FILE_POOL_CACHE_SIZE;

    private int maxParallelCrawlers = -1;

//    private Path progressDir = DEFAULT_PROGRESS_DIR;
//    private Path logsDir = DEFAULT_LOGS_DIR;

    private final List<IEventListener<?>> eventListeners = new ArrayList<>();

//    private final List<ICollectorLifeCycleListener> collectorListeners =
//            new ArrayList<>();
//    private IJobLifeCycleListener[] jobLifeCycleListeners;
//    private IJobErrorListener[] jobErrorListeners;
//    private ISuiteLifeCycleListener[] suiteLifeCycleListeners;

    public CollectorConfig() {
        this((Class<? extends CrawlerConfig>) null);
    }
    public CollectorConfig(
            Class<? extends CrawlerConfig> crawlerConfigClass) {
        this.crawlerConfigClass = crawlerConfigClass;
//        this(crawlerConfigClass, "collector");
    }
//    public CollectorConfig(String xmlConfigRootTag) {
//        this(null, xmlConfigRootTag);
//    }
//    public CollectorConfig(
//            Class<? extends CrawlerConfig> crawlerConfigClass,
//            String xmlConfigRootTag) {
//        super();
//        this.crawlerConfigClass = crawlerConfigClass;
//        this.xmlConfigRootTag = xmlConfigRootTag;
//    }


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



//    /**
//     * Gets the directory location where progress files (from JEF API)
//     * are stored.
//     * @return progress directory path
//     */
//    @Override
//    public Path getProgressDir() {
//        return progressDir;
//    }
//    /**
//     * Sets the directory location where progress files (from JEF API)
//     * are stored.
//     * @param progressDir progress directory path
//     */
//    public void setProgressDir(Path progressDir) {
//        this.progressDir = progressDir;
//    }
//
//    /**
//     * Gets the directory location of generated log files.
//     * @return logs directory path
//     */
//    @Override
//    public Path getLogsDir() {
//        return logsDir;
//    }
//    /**
//     * Sets the directory location of generated log files.
//     * @param logsDir logs directory path
//     */
//    public void setLogsDir(Path logsDir) {
//        this.logsDir = logsDir;
//    }

    //TODO document these and their defaults
    //TODO since 3.0.0
    public Path getTempDir() {
        return tempDir;
    }
    public void setTempDir(Path tempDir) {
//        Objects.requireNonNull(tempDir, "'tempDir' must not be null.");
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
     * @since 1.9.2
     */
    public int getMaxParallelCrawlers() {
        return maxParallelCrawlers;
    }
    /**
     * Sets the maximum number of crawlers that can be executed in parallel at
     * any given time.
     * Use <code>-1</code> for no maximum.
     * @param maxParallelCrawlers number of maximum parallel crawlers
     * @since 1.9.2
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


//    @Override
//    public List<ICollectorLifeCycleListener> getCollectorListeners() {
//        return collectorListeners;
//    }
//    /**
//     * Sets collector life cycle listeners.
//     * @param collectorListeners collector life cycle listeners.
//     * @since 1.8.0
//     */
//    public void setCollectorListeners(
//            ICollectorLifeCycleListener... collectorListeners) {
//        setCollectorListeners(Arrays.asList(collectorListeners));
//    }
//    /**
//     * Sets collector life cycle listeners.
//     * @param collectorListeners collector life cycle listeners.
//     * @since 2.0.0
//     */
//    public void setCollectorListeners(
//            List<ICollectorLifeCycleListener> collectorListeners) {
//        CollectionUtil.setAll(this.collectorListeners, collectorListeners);
//    }
//
//    @Override
//    public IJobLifeCycleListener[] getJobLifeCycleListeners() {
//        return jobLifeCycleListeners;
//    }
//    /**
//     * Sets JEF job life cycle listeners. A job typically represents a
//     * crawler instance. Interacting directly
//     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
//     * is normally reserved for more advanced use.
//     * @param jobLifeCycleListeners JEF job life cycle listeners.
//     * @since 1.7.0
//     */
//    public void setJobLifeCycleListeners(
//            IJobLifeCycleListener... jobLifeCycleListeners) {
//        this.jobLifeCycleListeners = jobLifeCycleListeners;
//    }
//
//    @Override
//    public IJobErrorListener[] getJobErrorListeners() {
//        return jobErrorListeners;
//    }
//    /**
//     * Sets JEF error listeners. Interacting directly
//     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
//     * is normally reserved for more advanced use.
//     * @param errorListeners JEF job error listeners
//     * @since 1.7.0
//     */
//    public void setJobErrorListeners(IJobErrorListener... errorListeners) {
//        this.jobErrorListeners = errorListeners;
//    }
//
//    @Override
//    public ISuiteLifeCycleListener[] getSuiteLifeCycleListeners() {
//        return suiteLifeCycleListeners;
//    }
//    /**
//     * Sets JEF job suite life cycle listeners.
//     * A job suite typically represents a collector instance.
//     * Interacting directly
//     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
//     * is normally reserved for more advanced use.
//     * @param suiteLifeCycleListeners JEF suite life cycle listeners
//     * @since 1.7.0
//     */
//    public void setSuiteLifeCycleListeners(
//            ISuiteLifeCycleListener... suiteLifeCycleListeners) {
//        this.suiteLifeCycleListeners = suiteLifeCycleListeners;
//    }

    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("id", getId());

        xml.addElement("workDir", getWorkDir());

        xml.addElement("maxParallelCrawlers", getMaxParallelCrawlers());

//        xml.addElement("logsDir", getLogsDir());
//        xml.addElement("progressDir", getProgressDir());
        xml.addElementList("eventListeners", "listener", eventListeners);
//        xml.addElementList(
//                "collectorListeners", "listener", getCollectorListeners());
//        writeArray(out, "jobLifeCycleListeners",
//                "listener", getJobLifeCycleListeners());
//        writeArray(out, "jobErrorListeners",
//                "listener", getJobErrorListeners());
//        writeArray(out, "suiteLifeCycleListeners",
//                "listener", getSuiteLifeCycleListeners());

        xml.addElementList("crawlers", "crawler", getCrawlerConfigs());

        saveCollectorConfigToXML(xml);

//        System.out.println("XML:\n" + xml.toString(4));
    }
    protected abstract void saveCollectorConfigToXML(XML xml);

    @Override
    public final void loadFromXML(XML xml) {

        String collectorId = xml.getString("@id", null);
        if (StringUtils.isBlank(collectorId)) {
            throw new CollectorException(
                    "Collector id attribute is mandatory.");
        }
        setId(collectorId);
        setWorkDir(xml.getPath("workDir", getWorkDir()));

        setMaxParallelCrawlers(xml.getInteger(
                "maxParallelCrawlers", getMaxParallelCrawlers()));

//        setLogsDir(xml.getPath("logsDir", getLogsDir()));
//        setProgressDir(xml.getPath("progressDir", getProgressDir()));
        setEventListeners(xml.getObjectList(
                "eventListeners/listener", eventListeners));

//        setCollectorListeners(xml.getObjectList(
//                "collectorListeners/listener", getCollectorListeners()));
//        // JEF Job listeners
//        IJobLifeCycleListener[] jlcListeners = loadJobLifeCycleListeners(
//                xml, "jobLifeCycleListeners.listener");
//        setJobLifeCycleListeners(defaultIfEmpty(jlcListeners,
//                getJobLifeCycleListeners()));
//
//        // JEF error listeners
//        IJobErrorListener[] jeListeners = loadJobErrorListeners(
//                xml, "jobErrorListeners.listener");
//        setJobErrorListeners(defaultIfEmpty(jeListeners,
//                getJobErrorListeners()));
//
//        // JEF suite listeners
//        ISuiteLifeCycleListener[] suiteListeners = loadSuiteLifeCycleListeners(
//                xml, "suiteLifeCycleListeners.listener");
//        setSuiteLifeCycleListeners(defaultIfEmpty(suiteListeners,
//                getSuiteLifeCycleListeners()));

        if (crawlerConfigClass != null) {
            List<CrawlerConfig> cfgs = new CrawlerConfigLoader(
                    crawlerConfigClass).loadCrawlerConfigs(xml);
            if (CollectionUtils.isNotEmpty(cfgs)) {
                setCrawlerConfigs(cfgs);
            }
        }

        loadCollectorConfigFromXML(xml);

        LOG.info("Configuration loaded: id={}; workDir={};",
                collectorId, workDir);
    }

//    private ICollectorLifeCycleListener[] loadCollectorListeners(
//            XML xml, String xmlPath) {
//        List<ICollectorLifeCycleListener> listeners = new ArrayList<>();
//        List<HierarchicalConfiguration> listenerNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration listenerNode : listenerNodes) {
//            ICollectorLifeCycleListener listener =
//                    XMLConfigurationUtil.newInstance(listenerNode);
//            listeners.add(listener);
//            LOG.info("Collector life cycle listener loaded: " + listener);
//        }
//        return listeners.toArray(new ICollectorLifeCycleListener[] {});
//    }
//    private IJobLifeCycleListener[] loadJobLifeCycleListeners(
//            XMLConfiguration xml, String xmlPath) {
//        List<IJobLifeCycleListener> listeners = new ArrayList<>();
//        List<HierarchicalConfiguration> listenerNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration listenerNode : listenerNodes) {
//            IJobLifeCycleListener listener =
//                    XMLConfigurationUtil.newInstance(listenerNode);
//            listeners.add(listener);
//            LOG.info("Job life cycle listener loaded: " + listener);
//        }
//        return listeners.toArray(new IJobLifeCycleListener[] {});
//    }
//    private IJobErrorListener[] loadJobErrorListeners(
//            XMLConfiguration xml, String xmlPath) {
//        List<IJobErrorListener> listeners = new ArrayList<>();
//        List<HierarchicalConfiguration> listenerNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration listenerNode : listenerNodes) {
//            IJobErrorListener listener =
//                    XMLConfigurationUtil.newInstance(listenerNode);
//            listeners.add(listener);
//            LOG.info("Job error listener loaded: " + listener);
//        }
//        return listeners.toArray(new IJobErrorListener[] {});
//    }
//    private ISuiteLifeCycleListener[] loadSuiteLifeCycleListeners(
//            XMLConfiguration xml, String xmlPath) {
//        List<ISuiteLifeCycleListener> listeners = new ArrayList<>();
//        List<HierarchicalConfiguration> listenerNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration listenerNode : listenerNodes) {
//            ISuiteLifeCycleListener listener =
//                    XMLConfigurationUtil.newInstance(listenerNode);
//            listeners.add(listener);
//            LOG.info("Suite life cycle listener loaded: " + listener);
//        }
//        return listeners.toArray(new ISuiteLifeCycleListener[] {});
//    }

    protected abstract void loadCollectorConfigFromXML(XML xml);

//    //TODO transfer to utility method in (Nx Commons Lang?) since it is
//    //duplicated code from CrawlerConfig.
//    protected void writeObject(
//            Writer out, String tagName, Object object) throws IOException {
//        writeObject(out, tagName, object, false);
//    }
//    //TODO transfer to utility method in (Nx Commons Lang?) since it is
//    //duplicated code from CrawlerConfig.
//    protected void writeObject(
//            Writer out, String tagName, Object object, boolean ignore)
//                    throws IOException {
//        out.flush();
//        if (object == null) {
//            if (ignore) {
//                out.write("<" + tagName + " ignore=\"" + ignore + "\" />");
//            }
//            return;
//        }
//        StringWriter w = new StringWriter();
//        if (object instanceof IXMLConfigurable) {
//            ((IXMLConfigurable) object).saveToXML(w);
//        } else {
//            w.write("<" + tagName + " class=\""
//                    + object.getClass().getCanonicalName() + "\" />");
//        }
//        String xml = w.toString();
//        if (ignore) {
//            xml = xml.replace("<" + tagName + " class=\"" ,
//                    "<" + tagName + " ignore=\"true\" class=\"" );
//        }
//        out.write(xml);
//        out.flush();
//    }
//    //TODO transfer to utility method in (Nx Commons Lang?) since it is
//    //duplicated code from CrawlerConfig.
//    protected void writeArray(Writer out, String listTagName,
//            String objectTagName, Object[] array) throws IOException {
//        if (ArrayUtils.isEmpty(array)) {
//            return;
//        }
//        out.write("<" + listTagName + ">");
//        for (Object obj : array) {
//            writeObject(out, objectTagName, obj);
//        }
//        out.write("</" + listTagName + ">");
//        out.flush();
//    }
//    //TODO transfer to utility method in (Nx Commons Lang?) since it is
//    //duplicated code from CrawlerConfig.
//    protected <T> T[] defaultIfEmpty(T[] array, T[] defaultArray) {
//        if (ArrayUtils.isEmpty(array)) {
//            return defaultArray;
//        }
//        return array;
//    }

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
