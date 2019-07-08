/* Copyright 2014-2019 Norconex Inc.
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
import com.norconex.commons.lang.event.IEventListener;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.ImporterConfig;

/**
 * Base Crawler configuration. Crawlers usually read this configuration upon
 * starting up.  Once execution has started, it should not be changed
 * to avoid unexpected behaviors.
 * @author Pascal Essiembre
 */
public abstract class  implements IXMLConfigurable {

    public enum OrphansStrategy {
        /**
         * Deleting orphans sends them to the Committer for deletions and
         * they are removed from the internal reference cache.
         */
        DELETE,
        /**
         * Processing orphans tries to obtain and process them again,
         * normally.
         */
        PROCESS,
        /**
         * Ignoring orphans effectively does nothing with them
         * (not deleted, not processed).
         */
        IGNORE
    }

    private String id;
    private int numThreads = 2;
//    private final Path workDir = Paths.get("./work");
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

    private final List<IEventListener<?>> eventListeners = new ArrayList<>();

    /**
     * Creates a new crawler configuration.
     */
	public CrawlerConfig() {
        super();
    }

	/**
	 * Gets this crawler unique identifier.
	 * @return unique identifier
	 */
    public String getId() {
        return id;
    }
    /**
     * Sets this crawler unique identifier.
     * Using usual names is perfectly fine (non-alphanumeric characters are OK).
     * It is important for this crawler ID to be unique amongst your
     * collector crawlers.  This facilitates integration with different
     * systems and facilitates tracking.
     * @param id unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the maximum number of threads a crawler can use.
     * @return number of threads
     */
    public int getNumThreads() {
        return numThreads;
    }
    /**
     * Sets the maximum number of threads a crawler can use.
     * @param numThreads number of threads
     */
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

//    /**
//     * Gets the crawler base directory where many files created at
//     * execution time are stored. Default is <code>null</code>, which
//     * will use the collector working directory
//     * @return working directory
//     */
//    public Path getWorkDir() {
//        return workDir;
//    }
//    public void setWorkDir(Path workDir) {
//        this.workDir = workDir;
//    }

    /**
     * Gets the maximum number of documents that can be processed. It is
     * normal not all "processed" documents make it to your Committer
     * as some can be rejected.
     * @return maximum number of documents that can be processed
     */
    public int getMaxDocuments() {
        return maxDocuments;
    }
    /**
     * Sets the maximum number of documents that can be processed.
     * @param maxDocuments maximum number of documents that can be processed
     */
    public void setMaxDocuments(int maxDocuments) {
        this.maxDocuments = maxDocuments;
    }

    /**
     * <p>Gets the strategy to adopt when there are orphans.  Orphans are
     * references that were processed in a previous run, but were not in the
     * current run.  In other words, they are leftovers from a previous run
     * that were not re-encountered in the current.
     * </p><p>
     * Unless explicitly stated otherwise by an implementing class, the default
     * strategy is to <code>PROCESS</code> orphans.
     * Setting a <code>null</code> value is the same as setting
     * <code>IGNORE</code>.
     * </p><p>
     * Since 1.2.0, unless otherwise stated in implementing classes,
     * the default orphan strategy is now <code>PROCESS</code>.
     * </p><p>
     * <b>Be careful:</b> Setting the orphan strategy to <code>DELETE</code>
     * is NOT recommended in most cases. With some collectors, a temporary
     * failure such as a network outage or a web page timing out, may cause
     * some documents not to be crawled. When this happens, unreachable
     * documents would be considered "orphans" and be deleted while under
     * normal circumstances, they should be kept.  Re-processing them
     * (default), is usually the safest approach to confirm they still
     * exist before deleting or updating them.
     * </p>
     * @return orphans strategy
     */
    public OrphansStrategy getOrphansStrategy() {
        return orphansStrategy;
    }
    /**
     * <p>Sets the strategy to adopt when there are orphans.</p>
     * @param orphansStrategy orphans strategy
     * @see #getOrphansStrategy()
     */
    public void setOrphansStrategy(OrphansStrategy orphansStrategy) {
        this.orphansStrategy = orphansStrategy;
    }

    /**
     * Gets the exceptions we want to stop the crawler on.
     * By default the crawler will log exceptions from processing
     * a document and try to move on to the next without stopping.
     * Even if no exceptions are returned by this method,
     * the crawler can sometimes stop regardless if it cannot recover
     * safely from an exception.
     * To capture more exceptions, use a parent class (e.g., Exception
     * should catch them all).
     * @return exceptions that will stop the crawler when encountered
     * @since 1.9.0
     */
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
            List<Class<? extends Exception>> stopOnExceptions) {
        CollectionUtil.setAll(this.stopOnExceptions, stopOnExceptions);
    }

    /**
     * Gets the crawl data store factory.
     * @return crawl data store factory.
     */
    public ICrawlDataStoreFactory getCrawlDataStoreFactory() {
        return crawlDataStoreFactory;
    }
    /**
     * Sets the crawl data store factory.
     * @param crawlDataStoreFactory crawl data store factory.
     */
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


    /**
     * Gets the spoiled state strategy resolver.
     * @return spoiled state strategy resolver
     * @since 1.2.0
     */
    public ISpoiledReferenceStrategizer getSpoiledReferenceStrategizer() {
        return spoiledReferenceStrategizer;
    }
    /**
     * Sets the spoiled state strategy resolver.
     * @param spoiledReferenceStrategizer spoiled state strategy resolver
     * @since 1.2.0
     */
    public void setSpoiledReferenceStrategizer(
            ISpoiledReferenceStrategizer spoiledReferenceStrategizer) {
        this.spoiledReferenceStrategizer = spoiledReferenceStrategizer;
    }

    /**
     * Gets reference filters
     * @return reference filters
     */
    public List<IReferenceFilter> getReferenceFilters() {
        return Collections.unmodifiableList(referenceFilters);
    }
    /**
     * Sets reference filters.
     * @param referenceFilters reference filters to set
     */
    public void setReferenceFilters(IReferenceFilter... referenceFilters) {
        setReferenceFilters(Arrays.asList(referenceFilters));
    }
    /**
     * Sets reference filters.
     * @param referenceFilters the referenceFilters to set
     * @since 2.0.0
     */
    public void setReferenceFilters(List<IReferenceFilter> referenceFilters) {
        CollectionUtil.setAll(this.referenceFilters, referenceFilters);
    }

    /**
     * Gets the document filters.
     * @return document filters
     */
    public List<IDocumentFilter> getDocumentFilters() {
        return Collections.unmodifiableList(documentFilters);
    }
    /**
     * Sets document filters.
     * @param documentfilters document filters
     */
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

    /**
     * Gets metadata filters.
     * @return metadata filters
     */
    public List<IMetadataFilter> getMetadataFilters() {
        return Collections.unmodifiableList(metadataFilters);
    }
    /**
     * Sets metadata filters.
     * @param metadataFilters metadata filters
     */
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

    /**
     * Gets the document checksummer.
     * @return document checksummer
     */
    public IDocumentChecksummer getDocumentChecksummer() {
        return documentChecksummer;
    }
    /**
     * Sets the document checksummer.
     * @param documentChecksummer document checksummer
     */
    public void setDocumentChecksummer(
            IDocumentChecksummer documentChecksummer) {
        this.documentChecksummer = documentChecksummer;
    }

    /**
     * Gets the Importer module configuration.
     * @return Importer module configuration
     */
    public ImporterConfig getImporterConfig() {
        return importerConfig;
    }
    /**
     * Sets the Importer module configuration.
     * @param importerConfig Importer module configuration
     */
    public void setImporterConfig(ImporterConfig importerConfig) {
        this.importerConfig = importerConfig;
    }

    /**
     * Gets the Committer module configuration.
     * @return Committer module configuration
     */
    public ICommitter getCommitter() {
        return committer;
    }
    /**
     * Sets the Committer module configuration.
     * @param committer Committer module configuration
     */
    public void setCommitter(ICommitter committer) {
        this.committer = committer;
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
        xml.setAttribute("id", id);
        xml.addElement("numThreads", numThreads);
//        xml.addElement("workDir", workDir);
        xml.addElement("maxDocuments", maxDocuments);
        xml.addElementList("stopOnExceptions", "exception", stopOnExceptions);
        xml.addElement("orphansStrategy", orphansStrategy);
        xml.addElement("crawlDataStoreFactory", crawlDataStoreFactory);
        xml.addElementList("referenceFilters", "filter", referenceFilters);
        xml.addElementList("metadataFilters", "filter", metadataFilters);
        xml.addElementList("documentFilters", "filter", documentFilters);
//        xml.addElementList("crawlerListeners", "listener", crawlerListeners);
        if (importerConfig != null) {
            xml.addElement("importer", importerConfig);//.configure(importerConfig);

//            xml.addElement("importer").configure(importerConfig);
        }
//        xml.addElement("importer", importerConfig);
        if (committer != null) {
            xml.addElement("committer", committer);
        }
        xml.addElement("documentChecksummer", documentChecksummer);
        xml.addElement(
                "spoiledReferenceStrategizer", spoiledReferenceStrategizer);

        xml.addElementList("eventListeners", "listener", eventListeners);

        saveCrawlerConfigToXML(xml);
    }
    protected abstract void saveCrawlerConfigToXML(XML xml);

    @Override
    public final void loadFromXML(XML xml) {
        setId(xml.getString("@id", id));
        setNumThreads(xml.getInteger("numThreads", numThreads));
        setOrphansStrategy(xml.getEnum(
                "orphansStrategy", OrphansStrategy.class, orphansStrategy));
//        setWorkDir(xml.getPath("workDir", workDir));
        setMaxDocuments(xml.getInteger("maxDocuments", maxDocuments));
        setStopOnExceptions(xml.getClassList(
                "stopOnExceptions/exception", stopOnExceptions));
        setReferenceFilters(xml.getObjectList(
                "referenceFilters/filter", referenceFilters));
        setMetadataFilters(xml.getObjectList(
                "metadataFilters/filter", metadataFilters));
        setDocumentFilters(xml.getObjectList(
                "documentFilters/filter", documentFilters));


        //TODO Make it so importer can be null (importing is then skipped)
//        setImporterConfig(xml.getObject("importer", importerConfig));

        XML importerXML = xml.getXML("importer");
        if (importerXML != null) {
            //TODO new ImporterConfig()  .setCachedConfigParams as defaults... then call XML configure
            ImporterConfig cfg = new ImporterConfig();
            /*List<XMLValidationError> errors = */ importerXML.populate(cfg);
            setImporterConfig(cfg);
            //TODO handle ignore errors
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

        setEventListeners(xml.getObjectList(
                "eventListeners/listener", eventListeners));

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
