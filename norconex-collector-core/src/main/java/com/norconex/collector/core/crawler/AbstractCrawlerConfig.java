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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStoreFactory;
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.ImporterConfig;
import com.norconex.importer.ImporterConfigLoader;

/**
 * Base Collector configuration.
 * @author Pascal Essiembre
 */
public abstract class AbstractCrawlerConfig implements ICrawlerConfig {

    private String id;
    private int numThreads = 2;
    private Path workDir = Paths.get("./work");
    private int maxDocuments = -1;
    private OrphansStrategy orphansStrategy = OrphansStrategy.PROCESS;
    private final List<Class<? extends Exception>> stopOnExceptions =
            new ArrayList<>();

    private ICrawlDataStoreFactory crawlDataStoreFactory =
            new MVStoreCrawlDataStoreFactory();

    private final List<IReferenceFilter> referenceFilters = new ArrayList<>();
    private final List<IMetadataFilter> metadataFilters = new ArrayList<>();
    private final List<IDocumentFilter> documentFilters = new ArrayList<>();

//    private final List<ICrawlerEventListener> crawlerListeners =
//            new ArrayList<>();
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
    public Path getWorkDir() {
        return workDir;
    }
    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    @Override
    public int getMaxDocuments() {
        return maxDocuments;
    }
    public void setMaxDocuments(int maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    @Override
    public OrphansStrategy getOrphansStrategy() {
        return orphansStrategy;
    }
    public void setOrphansStrategy(OrphansStrategy orphansStrategy) {
        this.orphansStrategy = orphansStrategy;
    }

    /**
     * @since 1.9.0
     */
    @Override
    public List<Class<? extends Exception>> getStopOnExceptions() {
        return Collections.unmodifiableList(stopOnExceptions);
    }
    /**
     * Sets the exceptions we want to stop the crawler on.
     * By default the crawler will log exceptions from processing
     * a document and try to move on to the next without stopping.
     * Even if no exceptions are returned by this method,
     * the crawler can sometimes stop regardless if it cannot recover
     * safely from an exception.
     * To capture more exceptions, use a parent class (e.g., Exception
     * should catch them all).
     * @param stopOnExceptions exceptions that will stop the crawler when
     *         encountered
     * @since 1.9.0
     */
    public void setStopOnExceptions(
            @SuppressWarnings("unchecked")
            Class<? extends Exception>... stopOnExceptions) {
        setStopOnExceptions(Arrays.asList(stopOnExceptions));
    }
    public void setStopOnExceptions(
            List<Class<? extends Exception>> stopOnExceptions) {
        CollectionUtil.setAll(this.stopOnExceptions, stopOnExceptions);
    }

    @Override
    public ICrawlDataStoreFactory getCrawlDataStoreFactory() {
        return crawlDataStoreFactory;
    }
    public void setCrawlDataStoreFactory(
            ICrawlDataStoreFactory crawlDataStoreFactory) {
        this.crawlDataStoreFactory = crawlDataStoreFactory;
    }

//    @Override
//    public List<ICrawlerEventListener> getCrawlerListeners() {
//        return Collections.unmodifiableList(crawlerListeners);
//    }
//    public void setCrawlerListeners(ICrawlerEventListener... crawlerListeners) {
//        setCrawlerListeners(Arrays.asList(crawlerListeners));
//    }
//    /**
//     * Sets crawler listeners.
//     * @param crawlerListeners
//     * @since 2.0.0
//     */
//    // Keep this or replace with JEF events??
//    public void setCrawlerListeners(
//            List<ICrawlerEventListener> crawlerListeners) {
//        CollectionUtil.setAll(this.crawlerListeners, crawlerListeners);
//    }


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
    public List<IReferenceFilter> getReferenceFilters() {
        return Collections.unmodifiableList(referenceFilters);
    }
    /**
     * Sets the reference filters.
     * @param referenceFilters the referenceFilters to set
     */
    public void setReferenceFilters(IReferenceFilter... referenceFilters) {
        setReferenceFilters(Arrays.asList(referenceFilters));
    }
    /**
     * Sets the reference filters.
     * @param referenceFilters the referenceFilters to set
     * @since 2.0.0
     */
    public void setReferenceFilters(List<IReferenceFilter> referenceFilters) {
        CollectionUtil.setAll(this.referenceFilters, referenceFilters);
    }
    @Override
    public List<IDocumentFilter> getDocumentFilters() {
        return Collections.unmodifiableList(documentFilters);
    }
    public void setDocumentFilters(IDocumentFilter... documentfilters) {
        setDocumentFilters(Arrays.asList(documentfilters));
    }
    /**
     * Sets document filters.
     * @param documentfilters document filters
     * @since 2.0.0
     */
    public void setDocumentFilters(List<IDocumentFilter> documentfilters) {
        CollectionUtil.setAll(this.documentFilters, documentFilters);
    }

    @Override
    public List<IMetadataFilter> getMetadataFilters() {
        return Collections.unmodifiableList(metadataFilters);
    }
    public void setMetadataFilters(IMetadataFilter... metadataFilters) {
        setMetadataFilters(Arrays.asList(metadataFilters));
    }
    /**
     * Sets metadata filters.
     * @param metadataFilters metadata filters
     * @since 2.0.0
     */
    public void setMetadataFilters(List<IMetadataFilter> metadataFilters) {
        CollectionUtil.setAll(this.metadataFilters, metadataFilters);
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
    public void saveToXML(XML xml) {
        xml.setAttribute("id", id);
        xml.addElement("numThreads", numThreads);
        xml.addElement("workDir", workDir);
        xml.addElement("maxDocuments", maxDocuments);
        xml.addElementList("stopOnExceptions", "exception", stopOnExceptions);
        xml.addElement("orphansStrategy", orphansStrategy);
        xml.addElement("crawlDataStoreFactory", crawlDataStoreFactory);
        xml.addElementList("referenceFilters", "filter", referenceFilters);
        xml.addElementList("metadataFilters", "filter", metadataFilters);
        xml.addElementList("documentFilters", "filter", documentFilters);
//        xml.addElementList("crawlerListeners", "listener", crawlerListeners);
        if (importerConfig != null) {
            xml.addElement("importer").configure(importerConfig);
        }
//        xml.addElement("importer", importerConfig);
        if (committer != null) {
            xml.addElement("committer", committer);
        }
        xml.addElement("documentChecksummer", documentChecksummer);
        xml.addElement(
                "spoiledReferenceStrategizer", spoiledReferenceStrategizer);

        saveCrawlerConfigToXML(xml);
    }
    protected abstract void saveCrawlerConfigToXML(XML xml);

    @Override
    public final void loadFromXML(XML xml) {
        setId(xml.getString("@id", id));
        setNumThreads(xml.getInteger("numThreads", numThreads));
        setOrphansStrategy(xml.getEnum(
                "orphansStrategy", OrphansStrategy.class, orphansStrategy));
        setWorkDir(xml.getPath("workDir", workDir));
        setMaxDocuments(xml.getInteger("maxDocuments", maxDocuments));
        setStopOnExceptions(xml.getClassList(
                "stopOnExceptions/exception", stopOnExceptions));
        setReferenceFilters(xml.getObjectList(
                "referenceFilters/filter", referenceFilters));
        setMetadataFilters(xml.getObjectList(
                "metadataFilters/filter", metadataFilters));
        setDocumentFilters(xml.getObjectList(
                "documentFilters/filter", documentFilters));
//        setCrawlerListeners(xml.getObjectList(
//                "crawlerListeners/listener", crawlerListeners));


        //TODO Make it so importer can be null (importing is then skipped)
//        setImporterConfig(xml.getObject("importer", importerConfig));

        XML importerXML = xml.getXML("importer");
        if (importerXML != null) {
            setImporterConfig(ImporterConfigLoader.loadImporterConfig(
                    importerXML, false)); //TODO handle ignore errors
        } else if (getImporterConfig() == null) {
            setImporterConfig(new ImporterConfig());
        }


        setCrawlDataStoreFactory(xml.getObject(
                "crawlDataStoreFactory", crawlDataStoreFactory));
        setCommitter(xml.getObject("committer", committer));
        setDocumentChecksummer(xml.getObject(
                "documentChecksummer", documentChecksummer));
        setSpoiledReferenceStrategizer(xml.getObject(
                "spoiledReferenceStrategizer", spoiledReferenceStrategizer));

        loadCrawlerConfigFromXML(xml);
    }
    protected abstract void loadCrawlerConfigFromXML(XML xml);


//    @SuppressWarnings("unchecked")
//    private Class<? extends Exception>[] loadStopOnExceptions(
//            XMLConfiguration xml, String xmlPath) {
//        List<Class<? extends Exception>> exceptions = new ArrayList<>();
//        List<HierarchicalConfiguration> exceptionNodes =
//                xml.configurationsAt(xmlPath);
//        for (HierarchicalConfiguration exNode : exceptionNodes) {
//            Class<? extends Exception> exception = (Class<? extends Exception>)
//                    XMLConfigurationUtil.getNullableClass(exNode, "", null);
//            if (exception != null) {
//                exceptions.add(exception);
//                LOG.info("Stop on exception class loaded: " + exception);
//            }
//        }
//        return exceptions.toArray(new Class[] {});
//    }
//
//    private ICrawlerEventListener[] loadListeners(XMLConfiguration xml,
//            String xmlPath) {
//        List<ICrawlerEventListener> listeners = new ArrayList<>();
//        List<HierarchicalConfiguration> listenerNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration listenerNode : listenerNodes) {
//            ICrawlerEventListener listener =
//                    XMLConfigurationUtil.newInstance(listenerNode);
//            listeners.add(listener);
//            LOG.info("Crawler event listener loaded: " + listener);
//        }
//        return listeners.toArray(new ICrawlerEventListener[] {});
//    }
//
//    private IReferenceFilter[] loadReferenceFilters(
//            XMLConfiguration xml, String xmlPath) {
//        List<IReferenceFilter> refFilters = new ArrayList<>();
//        List<HierarchicalConfiguration> filterNodes =
//                xml.configurationsAt(xmlPath);
//        for (HierarchicalConfiguration filterNode : filterNodes) {
//            IReferenceFilter refFilter =
//                    XMLConfigurationUtil.newInstance(filterNode);
//            if (refFilter != null) {
//                refFilters.add(refFilter);
//                LOG.info("Reference filter loaded: " + refFilter);
//            } else {
//                LOG.error("Problem loading filter, "
//                        + "please check for other log messages.");
//            }
//        }
//        return refFilters.toArray(new IReferenceFilter[] {});
//    }
//
//    private IMetadataFilter[] loadMetadataFilters(XMLConfiguration xml,
//            String xmlPath) {
//        List<IMetadataFilter> filters = new ArrayList<>();
//        List<HierarchicalConfiguration> filterNodes = xml
//                .configurationsAt(xmlPath);
//        for (HierarchicalConfiguration filterNode : filterNodes) {
//            IMetadataFilter filter =
//                    XMLConfigurationUtil.newInstance(filterNode);
//            filters.add(filter);
//            LOG.info("Matadata filter loaded: " + filter);
//        }
//        return filters.toArray(new IMetadataFilter[] {});
//    }
//
//    private IDocumentFilter[] loadDocumentFilters(
//            XMLConfiguration xml, String xmlPath) {
//        List<IDocumentFilter> filters = new ArrayList<>();
//        List<HierarchicalConfiguration> filterNodes =
//                xml.configurationsAt(xmlPath);
//        for (HierarchicalConfiguration filterNode : filterNodes) {
//            IDocumentFilter filter =
//                    XMLConfigurationUtil.newInstance(filterNode);
//            filters.add(filter);
//            LOG.info("Document filter loaded: " + filter);
//        }
//        return filters.toArray(new IDocumentFilter[] {});
//    }
//
//    protected void writeObject(
//            Writer out, String tagName, Object object) throws IOException {
//        writeObject(out, tagName, object, false);
//    }
//
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
//
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
