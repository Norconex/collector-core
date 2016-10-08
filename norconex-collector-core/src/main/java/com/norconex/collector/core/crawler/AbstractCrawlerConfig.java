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
package com.norconex.collector.core.crawler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.core.crawler.event.ICrawlerEventListener;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStoreFactory;
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.ImporterConfig;
import com.norconex.importer.ImporterConfigLoader;

/**
 * Base Collector configuration.
 * @author Pascal Essiembre
 */
public abstract class AbstractCrawlerConfig implements ICrawlerConfig {

    private static final Logger LOG = LogManager.getLogger(
            AbstractCrawlerConfig.class);
    
    private String id;
    private int numThreads = 2;
    private File workDir = new File("./work");
    private int maxDocuments = -1;
    private OrphansStrategy orphansStrategy = OrphansStrategy.PROCESS;
    
    private ICrawlDataStoreFactory crawlDataStoreFactory = 
            new MVStoreCrawlDataStoreFactory();

    private IReferenceFilter[] referenceFilters;
    private IMetadataFilter[] metadataFilters;
    private IDocumentFilter[] documentFilters;
    
    private ICrawlerEventListener[] crawlerListeners;
    private ImporterConfig importerConfig = new ImporterConfig();
    private ICommitter committer;

    private IDocumentChecksummer documentChecksummer =
            new MD5DocumentChecksummer();
    
    private ISpoiledReferenceStrategizer spoiledReferenceStrategizer = 
            new GenericSpoiledReferenceStrategizer();
        
    /**
     * Creates a new crawler configuration.
     */
	public AbstractCrawlerConfig() {
        super();
    }

	/**
	 * Gets this crawler unique identifier.
	 * @return unique identifier
	 */
	@Override
    public String getId() {
        return id;
    }
    /**
     * Sets this crawler unique identifier. It is important
     * the id of the crawler is unique amongst your collector crawlers.  This
     * facilitates integration with different systems and facilitates
     * tracking.
     * @param id unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int getNumThreads() {
        return numThreads;
    }
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }
    
    @Override
    public File getWorkDir() {
        return workDir;
    }
    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }
    
    @Override
    public int getMaxDocuments() {
        return maxDocuments;
    }
    public void setMaxDocuments(int maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    public OrphansStrategy getOrphansStrategy() {
        return orphansStrategy;
    }
    public void setOrphansStrategy(OrphansStrategy orphansStrategy) {
        this.orphansStrategy = orphansStrategy;
    }

    @Override
    public ICrawlDataStoreFactory getCrawlDataStoreFactory() {
        return crawlDataStoreFactory;
    }
    public void setCrawlDataStoreFactory(
            ICrawlDataStoreFactory crawlDataStoreFactory) {
        this.crawlDataStoreFactory = crawlDataStoreFactory;
    }

    @Override
    public ICrawlerEventListener[] getCrawlerListeners() {
        return ArrayUtils.clone(crawlerListeners);
    }
    public void setCrawlerListeners(
            ICrawlerEventListener... crawlerListeners) {
        this.crawlerListeners = ArrayUtils.clone(crawlerListeners);
    }
    
    
    @Override
    public ISpoiledReferenceStrategizer getSpoiledReferenceStrategizer() {
        return spoiledReferenceStrategizer;
    }
    public void setSpoiledReferenceStrategizer(
            ISpoiledReferenceStrategizer spoiledReferenceStrategizer) {
        this.spoiledReferenceStrategizer = spoiledReferenceStrategizer;
    }

    /**
     * Gets the reference filters
     * @return the referenceFilters
     */
    @Override
    public IReferenceFilter[] getReferenceFilters() {
        return ArrayUtils.clone(referenceFilters);
    }
    /**
     * Sets the reference filters.
     * @param referenceFilters the referenceFilters to set
     */
    public void setReferenceFilters(IReferenceFilter... referenceFilters) {
        this.referenceFilters = ArrayUtils.clone(referenceFilters);
    }
    @Override
    public IDocumentFilter[] getDocumentFilters() {
        return ArrayUtils.clone(documentFilters);
    }
    public void setDocumentFilters(IDocumentFilter... documentfilters) {
        this.documentFilters = ArrayUtils.clone(documentfilters);
    }

    @Override
    public IMetadataFilter[] getMetadataFilters() {
        return ArrayUtils.clone(metadataFilters);
    }
    public void setMetadataFilters(IMetadataFilter... metadataFilters) {
        this.metadataFilters = ArrayUtils.clone(metadataFilters);
    }

    @Override
    public IDocumentChecksummer getDocumentChecksummer() {
        return documentChecksummer;
    }
    public void setDocumentChecksummer(
            IDocumentChecksummer documentChecksummer) {
        this.documentChecksummer = documentChecksummer;
    }

    @Override
    public ImporterConfig getImporterConfig() {
        return importerConfig;
    }
    public void setImporterConfig(ImporterConfig importerConfig) {
        this.importerConfig = importerConfig;
    }
    
    @Override
    public ICommitter getCommitter() {
        return committer;
    }
    public void setCommitter(ICommitter committer) {
        this.committer = committer;
    }
    
    @Override
    public ICrawlerConfig clone() {
        try {
            return (ICrawlerConfig) BeanUtils.cloneBean(this);
        } catch (Exception e) {
            throw new CollectorException(
                    "Cannot clone crawler configuration.", e);
        }
    }
    
    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            out.flush();
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawler");
            writer.writeAttributeClass("class", getClass());
            writer.writeAttribute("id", getId());

            writer.writeElementInteger("numThreads", getNumThreads());
            writer.writeElementString("workDir", getWorkDir().toString()); 
            writer.writeElementInteger("maxDocuments", getMaxDocuments());
            
            OrphansStrategy strategy = getOrphansStrategy();
            if (strategy != null) {
                writer.writeElementString(
                        "orphansStrategy", strategy.toString());
            }
            writer.flush();
            
            writeObject(out, "crawlDataStoreFactory", 
                    getCrawlDataStoreFactory());
            writeArray(out, "referenceFilters", 
                    "filter", getReferenceFilters());
            writeArray(out, "metadataFilters", "filter", getMetadataFilters());
            writeArray(out, "documentFilters", "filter", getDocumentFilters());
            writeArray(out, "crawlerListeners", "listener", 
                    getCrawlerListeners());
            writeObject(out, "importer", getImporterConfig());
            writeObject(out, "committer", getCommitter());
            writeObject(out, "documentChecksummer", getDocumentChecksummer());
            writeObject(out, "spoiledReferenceStrategizer", 
                    getSpoiledReferenceStrategizer());
            
            saveCrawlerConfigToXML(out);
            
            writer.writeEndElement();
            writer.flush();
            
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }   
        
    }
    protected abstract void saveCrawlerConfigToXML(Writer out)
            throws IOException;
    
    @Override
    public final void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        

        String crawlerId = xml.getString("[@id]", null);
        setId(crawlerId);
        setNumThreads(xml.getInt("numThreads", getNumThreads()));
        OrphansStrategy strategy = getOrphansStrategy();
        String strategyStr = xml.getString("orphansStrategy", null);
        if (StringUtils.isNotBlank(strategyStr)) {
            strategy = OrphansStrategy.valueOf(strategyStr.toUpperCase());
        }
        setOrphansStrategy(strategy);
        
        // Work directory
        File dir = workDir;
        String dirStr = xml.getString("workDir", null);
        if (StringUtils.isNotBlank(dirStr)) {
            dir = new File(dirStr);
        }
        setWorkDir(dir);
        setMaxDocuments(xml.getInt("maxDocuments", getMaxDocuments()));

        //--- Reference Filters ------------------------------------------------
        IReferenceFilter[] referenceFilters = 
                loadReferenceFilters(xml, "referenceFilters.filter");
        setReferenceFilters(defaultIfEmpty(
                referenceFilters, getReferenceFilters()));
        
        //--- Metadata Filters ---------------------------------------------
        IMetadataFilter[] metadataFilters = 
                loadMetadataFilters(xml, "metadataFilters.filter");
        setMetadataFilters(defaultIfEmpty(
                metadataFilters, getMetadataFilters()));
        
        //--- Document Filters -------------------------------------------------
        IDocumentFilter[] docFilters = 
                loadDocumentFilters(xml, "documentFilters.filter");
        setDocumentFilters(defaultIfEmpty(docFilters, getDocumentFilters()));
        
        //--- Crawler Listeners ------------------------------------------------
        ICrawlerEventListener[] crawlerListeners = 
                loadListeners(xml, "crawlerListeners.listener");
        setCrawlerListeners(defaultIfEmpty(crawlerListeners,
                getCrawlerListeners()));

        //--- IMPORTER ---------------------------------------------------------
        XMLConfiguration importerNode = 
                ConfigurationUtil.getXmlAt(xml, "importer");
        ImporterConfig importerConfig = ImporterConfigLoader
                .loadImporterConfig(importerNode);
        setImporterConfig(ObjectUtils.defaultIfNull(importerConfig,
                getImporterConfig()));

        //--- Data Store -------------------------------------------------------
        setCrawlDataStoreFactory(ConfigurationUtil.newInstance(xml,
                "crawlDataStoreFactory", getCrawlDataStoreFactory()));

        //--- Document Committer -----------------------------------------------
        setCommitter(ConfigurationUtil.newInstance(
                xml, "committer", getCommitter()));
        
        //--- Document Checksummer ---------------------------------------------
        setDocumentChecksummer(ConfigurationUtil.newInstance(
                xml, "documentChecksummer", getDocumentChecksummer()));

        //--- Spoiled State Strategy Resolver ----------------------------------
        setSpoiledReferenceStrategizer(ConfigurationUtil.newInstance(
                xml, "spoiledReferenceStrategizer", 
                        getSpoiledReferenceStrategizer()));
        
        loadCrawlerConfigFromXML(xml);
    }
    protected abstract void loadCrawlerConfigFromXML(XMLConfiguration xml)
            throws IOException;
    

    private ICrawlerEventListener[] loadListeners(XMLConfiguration xml,
            String xmlPath) {
        List<ICrawlerEventListener> listeners = new ArrayList<>();
        List<HierarchicalConfiguration> listenerNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration listenerNode : listenerNodes) {
            ICrawlerEventListener listener = ConfigurationUtil
                    .newInstance(listenerNode);
            listeners.add(listener);
            LOG.info("Crawler event listener loaded: " + listener);
        }
        return listeners.toArray(new ICrawlerEventListener[] {});
    }
    
    private IReferenceFilter[] loadReferenceFilters(
            XMLConfiguration xml, String xmlPath) {
        List<IReferenceFilter> refFilters = new ArrayList<>();
        List<HierarchicalConfiguration> filterNodes = 
                xml.configurationsAt(xmlPath);
        for (HierarchicalConfiguration filterNode : filterNodes) {
            IReferenceFilter refFilter = 
                    ConfigurationUtil.newInstance(filterNode);
            if (refFilter != null) {
                refFilters.add(refFilter);
                LOG.info("Reference filter loaded: " + refFilter);
            } else {
                LOG.error("Problem loading filter, "
                        + "please check for other log messages.");
            }
        }
        return refFilters.toArray(new IReferenceFilter[] {});
    }
    
    private IMetadataFilter[] loadMetadataFilters(XMLConfiguration xml,
            String xmlPath) {
        List<IMetadataFilter> filters = new ArrayList<>();
        List<HierarchicalConfiguration> filterNodes = xml
                .configurationsAt(xmlPath);
        for (HierarchicalConfiguration filterNode : filterNodes) {
            IMetadataFilter filter = ConfigurationUtil.newInstance(filterNode);
            filters.add(filter);
            LOG.info("Matadata filter loaded: " + filter);
        }
        return filters.toArray(new IMetadataFilter[] {});
    }
    
    private IDocumentFilter[] loadDocumentFilters(
            XMLConfiguration xml, String xmlPath) {
        List<IDocumentFilter> filters = new ArrayList<>();
        List<HierarchicalConfiguration> filterNodes = 
                xml.configurationsAt(xmlPath);
        for (HierarchicalConfiguration filterNode : filterNodes) {
            IDocumentFilter filter = ConfigurationUtil.newInstance(filterNode);
            filters.add(filter);
            LOG.info("Document filter loaded: " + filter);
        }
        return filters.toArray(new IDocumentFilter[] {});
    }
    
    protected void writeObject(
            Writer out, String tagName, Object object) throws IOException {
        writeObject(out, tagName, object, false);
    }
    
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
    
    protected <T> T[] defaultIfEmpty(T[] array, T[] defaultArray) {
        if (ArrayUtils.isEmpty(array)) {
            return defaultArray;
        }
        return array;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AbstractCrawlerConfig)) {
            return false;
        }
        AbstractCrawlerConfig castOther = (AbstractCrawlerConfig) other;
        return new EqualsBuilder()
                .append(id, castOther.id)
                .append(numThreads, castOther.numThreads)
                .append(workDir, castOther.workDir)
                .append(maxDocuments, castOther.maxDocuments)
                .append(orphansStrategy, castOther.orphansStrategy)
                .append(crawlDataStoreFactory, castOther.crawlDataStoreFactory)
                .append(referenceFilters, castOther.referenceFilters)
                .append(metadataFilters, castOther.metadataFilters)
                .append(documentFilters, castOther.documentFilters)
                .append(crawlerListeners, castOther.crawlerListeners)
                .append(importerConfig, castOther.importerConfig)
                .append(committer, castOther.committer)
                .append(documentChecksummer, castOther.documentChecksummer)
                .append(spoiledReferenceStrategizer, 
                        castOther.spoiledReferenceStrategizer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(numThreads)
                .append(workDir)
                .append(maxDocuments)
                .append(orphansStrategy)
                .append(crawlDataStoreFactory)
                .append(referenceFilters)
                .append(metadataFilters)
                .append(documentFilters)
                .append(crawlerListeners)
                .append(importerConfig)
                .append(committer)
                .append(documentChecksummer)
                .append(spoiledReferenceStrategizer)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("numThreads", numThreads)
                .append("workDir", workDir)
                .append("maxDocuments", maxDocuments)
                .append("orphansStrategy", orphansStrategy)
                .append("crawlDataStoreFactory", crawlDataStoreFactory)
                .append("referenceFilters", referenceFilters)
                .append("metadataFilters", metadataFilters)
                .append("documentFilters", documentFilters)
                .append("crawlerListeners", crawlerListeners)
                .append("importerConfig", importerConfig)
                .append("committer", committer)
                .append("documentChecksummer", documentChecksummer)
                .append("spoiledReferenceStrategizer", 
                        spoiledReferenceStrategizer)
                .toString();
    }
}
