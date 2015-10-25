/* Copyright 2014-2015 Norconex Inc.
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
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

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
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

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
     * Gets all crawler configurations.
     * @return crawler configurations
     */
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
    protected abstract void loadCollectorConfigFromXML(XMLConfiguration xml);

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
                .toString();
    }
}
