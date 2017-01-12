/* Copyright 2014-2016 Norconex Inc.
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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.CrawlerConfigLoader;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.jef4.job.IJobErrorListener;
import com.norconex.jef4.job.IJobLifeCycleListener;
import com.norconex.jef4.suite.ISuiteLifeCycleListener;

/**
 * Base Collector configuration.
 * @author Pascal Essiembre
 */
public abstract class AbstractCollectorConfig implements ICollectorConfig {

    private static final Logger LOG = LogManager.getLogger(
            AbstractCollectorConfig.class);
    
    /** Default relative directory where logs from Log4j are stored. */
    public static final String DEFAULT_LOGS_DIR = "./logs";
    /** Default relative directory where progress files are stored. */
    public static final String DEFAULT_PROGRESS_DIR = "./progress";
    
    private final Class<? extends ICrawlerConfig> crawlerConfigClass;
    
    private String id;
    private ICrawlerConfig[] crawlerConfigs;
    private String progressDir = DEFAULT_PROGRESS_DIR;
    private String logsDir = DEFAULT_LOGS_DIR;
    private ICollectorLifeCycleListener[] collectorListeners;
    private IJobLifeCycleListener[] jobLifeCycleListeners;
    private IJobErrorListener[] jobErrorListeners;
    private ISuiteLifeCycleListener[] suiteLifeCycleListeners;

    public AbstractCollectorConfig() {
        this(null);
    }
    
    public AbstractCollectorConfig(
            Class<? extends ICrawlerConfig> crawlerConfigClass) {
        super();
        this.crawlerConfigClass = crawlerConfigClass;
    }

	/**
	 * Gets this collector unique identifier.
	 * @return unique identifier
	 */
    @Override
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

    @Override
    public ICrawlerConfig[] getCrawlerConfigs() {
        return ArrayUtils.clone(crawlerConfigs);
    }
    /**
     * Sets crawler configurations.
     * @param crawlerConfigs crawler configurations
     */
    public void setCrawlerConfigs(ICrawlerConfig[] crawlerConfigs) {
        this.crawlerConfigs = ArrayUtils.clone(crawlerConfigs);
    }

    /**
     * Gets the directory location where progress files (from JEF API)
     * are stored.
     * @return progress directory path
     */
    @Override
    public String getProgressDir() {
        return progressDir;
    }
    /**
     * Sets the directory location where progress files (from JEF API)
     * are stored.
     * @param progressDir progress directory path
     */
    public void setProgressDir(String progressDir) {
        this.progressDir = progressDir;
    }

    /**
     * Gets the directory location of generated log files.
     * @return logs directory path
     */
    @Override
    public String getLogsDir() {
        return logsDir;
    }
    /**
     * Sets the directory location of generated log files.
     * @param logsDir logs directory path
     */
    public void setLogsDir(String logsDir) {
        this.logsDir = logsDir;
    }

    @Override    
    public ICollectorLifeCycleListener[] getCollectorListeners() {
        return collectorListeners;
    }
    /**
     * Sets collector life cycle listeners. 
     * @param listener collector life cycle listeners. 
     * @since 1.8.0
     */    
    public void setCollectorListeners(
            ICollectorLifeCycleListener... collectorListeners) {
        this.collectorListeners = collectorListeners;
    }

    @Override
    public IJobLifeCycleListener[] getJobLifeCycleListeners() {
        return jobLifeCycleListeners;
    }
    /**
     * Sets JEF job life cycle listeners. A job typically represents a 
     * crawler instance. Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * @param jobLifeCycleListeners JEF job life cycle listeners. 
     * @since 1.7.0
     */    
    public void setJobLifeCycleListeners(
            IJobLifeCycleListener... jobLifeCycleListeners) {
        this.jobLifeCycleListeners = jobLifeCycleListeners;
    }

    @Override
    public IJobErrorListener[] getJobErrorListeners() {
        return jobErrorListeners;
    }
    /**
     * Sets JEF error listeners. Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * @param errorListeners JEF job error listeners
     * @since 1.7.0
     */    
    public void setJobErrorListeners(IJobErrorListener... errorListeners) {
        this.jobErrorListeners = errorListeners;
    }

    @Override
    public ISuiteLifeCycleListener[] getSuiteLifeCycleListeners() {
        return suiteLifeCycleListeners;
    }
    /**
     * Sets JEF job suite life cycle listeners. 
     * A job suite typically represents a collector instance. 
     * Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * @param suiteLifeCycleListeners JEF suite life cycle listeners 
     * @since 1.7.0
     */    
    public void setSuiteLifeCycleListeners(
            ISuiteLifeCycleListener... suiteLifeCycleListeners) {
        this.suiteLifeCycleListeners = suiteLifeCycleListeners;
    }
    
    public void saveToXML(Writer out) throws IOException {
        try {
            out.flush();
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("collector");
            writer.writeAttributeClass("class", getClass());
            writer.writeAttribute("id", getId());
            
            writer.writeElementString("logsDir", getLogsDir());
            writer.writeElementString("progressDir", getProgressDir());
            writer.flush();

            writeArray(out, "collectorListeners", 
                    "listener", getCollectorListeners());
            writeArray(out, "jobLifeCycleListeners", 
                    "listener", getJobLifeCycleListeners());
            writeArray(out, "jobErrorListeners", 
                    "listener", getJobErrorListeners());
            writeArray(out, "suiteLifeCycleListeners", 
                    "listener", getSuiteLifeCycleListeners());
            
            out.write("<crawlers>");
            out.flush();
            if (crawlerConfigs != null) {
                for (ICrawlerConfig crawlerConfig : crawlerConfigs) {
                    crawlerConfig.saveToXML(out);
                    out.flush();
                }
            }
            out.write("</crawlers>");
            out.flush();
            
            saveCollectorConfigToXML(out);
            
            writer.writeEndElement();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }   
        
    }
    protected abstract void saveCollectorConfigToXML(Writer out);
    
    @Override
    public final void loadFromXML(Reader in) throws IOException {
        
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        String collectorId = xml.getString("[@id]", null);
        if (StringUtils.isBlank(collectorId)) {
            throw new CollectorException(
                    "Collector id attribute is mandatory.");
        }
        setId(collectorId);
        setLogsDir(xml.getString("logsDir", getLogsDir()));
        setProgressDir(xml.getString("progressDir", getProgressDir()));
        
        // Collector listeners
        ICollectorLifeCycleListener[] collListeners = loadCollectorListeners(
                xml, "collectorListeners.listener");
        setCollectorListeners(defaultIfEmpty(collListeners,
                getCollectorListeners()));
        
        // JEF Job listeners
        IJobLifeCycleListener[] jlcListeners = loadJobLifeCycleListeners(
                xml, "jobLifeCycleListeners.listener");
        setJobLifeCycleListeners(defaultIfEmpty(jlcListeners,
                getJobLifeCycleListeners()));

        // JEF error listeners
        IJobErrorListener[] jeListeners = loadJobErrorListeners(
                xml, "jobErrorListeners.listener");
        setJobErrorListeners(defaultIfEmpty(jeListeners,
                getJobErrorListeners()));

        // JEF suite listeners
        ISuiteLifeCycleListener[] suiteListeners = loadSuiteLifeCycleListeners(
                xml, "suiteLifeCycleListeners.listener");
        setSuiteLifeCycleListeners(defaultIfEmpty(suiteListeners,
                getSuiteLifeCycleListeners()));

        if (crawlerConfigClass != null) {
            setCrawlerConfigs(new CrawlerConfigLoader(
                    crawlerConfigClass).loadCrawlerConfigs(xml));
        }

        loadCollectorConfigFromXML(xml);

        if (LOG.isInfoEnabled()) {
            LOG.info("Configuration loaded: id=" + collectorId + "; logsDir="
                    + getLogsDir() + "; progressDir=" + getProgressDir());
        }
    }

    private ICollectorLifeCycleListener[] loadCollectorListeners(
            XMLConfiguration xml, String xmlPath) {
        List<ICollectorLifeCycleListener> listeners = new ArrayList<>();
        List<HierarchicalConfiguration> listenerNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration listenerNode : listenerNodes) {
            ICollectorLifeCycleListener listener = 
                    ConfigurationUtil.newInstance(listenerNode);
            listeners.add(listener);
            LOG.info("Collector life cycle listener loaded: " + listener);
        }
        return listeners.toArray(new ICollectorLifeCycleListener[] {});
    }
    private IJobLifeCycleListener[] loadJobLifeCycleListeners(
            XMLConfiguration xml, String xmlPath) {
        List<IJobLifeCycleListener> listeners = new ArrayList<>();
        List<HierarchicalConfiguration> listenerNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration listenerNode : listenerNodes) {
            IJobLifeCycleListener listener = 
                    ConfigurationUtil.newInstance(listenerNode);
            listeners.add(listener);
            LOG.info("Job life cycle listener loaded: " + listener);
        }
        return listeners.toArray(new IJobLifeCycleListener[] {});
    }
    private IJobErrorListener[] loadJobErrorListeners(
            XMLConfiguration xml, String xmlPath) {
        List<IJobErrorListener> listeners = new ArrayList<>();
        List<HierarchicalConfiguration> listenerNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration listenerNode : listenerNodes) {
            IJobErrorListener listener = 
                    ConfigurationUtil.newInstance(listenerNode);
            listeners.add(listener);
            LOG.info("Job error listener loaded: " + listener);
        }
        return listeners.toArray(new IJobErrorListener[] {});
    }
    private ISuiteLifeCycleListener[] loadSuiteLifeCycleListeners(
            XMLConfiguration xml, String xmlPath) {
        List<ISuiteLifeCycleListener> listeners = new ArrayList<>();
        List<HierarchicalConfiguration> listenerNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration listenerNode : listenerNodes) {
            ISuiteLifeCycleListener listener = 
                    ConfigurationUtil.newInstance(listenerNode);
            listeners.add(listener);
            LOG.info("Suite life cycle listener loaded: " + listener);
        }
        return listeners.toArray(new ISuiteLifeCycleListener[] {});
    }    
    
    protected abstract void loadCollectorConfigFromXML(XMLConfiguration xml);

    //TODO transfer to utility method in (Nx Commons Lang?) since it is
    //duplicated code from AbstractCrawlerConfig. 
    protected void writeObject(
            Writer out, String tagName, Object object) throws IOException {
        writeObject(out, tagName, object, false);
    }
    //TODO transfer to utility method in (Nx Commons Lang?) since it is
    //duplicated code from AbstractCrawlerConfig. 
    protected void writeObject(
            Writer out, String tagName, Object object, boolean ignore) 
                    throws IOException {
        out.flush();
        if (object == null) {
            if (ignore) {
                out.write("<" + tagName + " ignore=\"" + ignore + "\" />");
            }
            return;
        }
        StringWriter w = new StringWriter();
        if (object instanceof IXMLConfigurable) {
            ((IXMLConfigurable) object).saveToXML(w);
        } else {
            w.write("<" + tagName + " class=\"" 
                    + object.getClass().getCanonicalName() + "\" />");
        }
        String xml = w.toString();
        if (ignore) {
            xml = xml.replace("<" + tagName + " class=\"" , 
                    "<" + tagName + " ignore=\"true\" class=\"" );
        }
        out.write(xml);
        out.flush();
    }
    //TODO transfer to utility method in (Nx Commons Lang?) since it is
    //duplicated code from AbstractCrawlerConfig. 
    protected void writeArray(Writer out, String listTagName, 
            String objectTagName, Object[] array) throws IOException {
        if (ArrayUtils.isEmpty(array)) {
            return;
        }
        out.write("<" + listTagName + ">"); 
        for (Object obj : array) {
            writeObject(out, objectTagName, obj);
        }
        out.write("</" + listTagName + ">"); 
        out.flush();
    }
    //TODO transfer to utility method in (Nx Commons Lang?) since it is
    //duplicated code from AbstractCrawlerConfig. 
    protected <T> T[] defaultIfEmpty(T[] array, T[] defaultArray) {
        if (ArrayUtils.isEmpty(array)) {
            return defaultArray;
        }
        return array;
    }
    
    
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AbstractCollectorConfig)) {
            return false;
        }
        AbstractCollectorConfig castOther = (AbstractCollectorConfig) other;
        return new EqualsBuilder()
                .append(crawlerConfigClass, castOther.crawlerConfigClass)
                .append(id, castOther.id)
                .append(crawlerConfigs, castOther.crawlerConfigs)
                .append(progressDir, castOther.progressDir)
                .append(logsDir, castOther.logsDir)
                .append(collectorListeners, castOther.collectorListeners)
                .append(jobLifeCycleListeners, castOther.jobLifeCycleListeners)
                .append(jobErrorListeners, castOther.jobErrorListeners)
                .append(suiteLifeCycleListeners, 
                        castOther.suiteLifeCycleListeners)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(crawlerConfigClass)
                .append(id)
                .append(crawlerConfigs)
                .append(progressDir)
                .append(logsDir)
                .append(collectorListeners)
                .append(jobLifeCycleListeners)
                .append(jobErrorListeners)
                .append(suiteLifeCycleListeners)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("crawlerConfigClass", crawlerConfigClass)
                .append("id", id)
                .append("crawlerConfigs", crawlerConfigs)
                .append("progressDir", progressDir)
                .append("logsDir", logsDir)
                .append("collectorListeners", collectorListeners)
                .append("jobLifeCycleListeners", jobLifeCycleListeners)
                .append("jobErrorListeners", jobErrorListeners)
                .append("suiteLifeCycleListeners", suiteLifeCycleListeners)
                .toString();
    }
}
