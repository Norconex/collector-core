/* Copyright 2014-2022 Norconex Inc.
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

import static com.norconex.collector.core.crawler.Crawler.ReferenceProcessStatus.MAX_REACHED;
import static com.norconex.collector.core.crawler.Crawler.ReferenceProcessStatus.QUEUE_EMPTY;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_CLEAN_BEGIN;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_CLEAN_END;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_INIT_BEGIN;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_INIT_END;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_RUN_BEGIN;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_RUN_END;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STOP_BEGIN;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STOP_END;
import static com.norconex.collector.core.crawler.CrawlerEvent.DOCUMENT_IMPORTED;
import static com.norconex.collector.core.crawler.CrawlerEvent.REJECTED_ERROR;
import static com.norconex.collector.core.crawler.CrawlerEvent.REJECTED_IMPORT;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.Collector;
import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.CrawlerConfig.OrphansStrategy;
import com.norconex.collector.core.doc.CrawlDoc;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.doc.CrawlDocInfoService;
import com.norconex.collector.core.doc.CrawlDocMetadata;
import com.norconex.collector.core.doc.CrawlState;
import com.norconex.collector.core.monitor.CrawlerMonitor;
import com.norconex.collector.core.monitor.CrawlerMonitorJMX;
import com.norconex.collector.core.monitor.MdcUtil;
import com.norconex.collector.core.pipeline.importer.ImporterPipelineContext;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.collector.core.spoil.SpoiledReferenceStrategy;
import com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer;
import com.norconex.collector.core.store.DataStoreExporter;
import com.norconex.collector.core.store.DataStoreImporter;
import com.norconex.collector.core.store.IDataStoreEngine;
import com.norconex.committer.core3.CommitterContext;
import com.norconex.commons.lang.Sleeper;
import com.norconex.commons.lang.bean.BeanUtil;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.importer.Importer;
import com.norconex.importer.doc.Doc;
import com.norconex.importer.response.ImporterResponse;

/**
 * <p>Abstract crawler implementation providing a common base to building
 * crawlers.</p>
 *
 * <p>As of 1.6.1, JMX support is disabled by default.  To enable it,
 * set the system property "enableJMX" to <code>true</code>.  You can do so
 * by adding this to your Java launch command:
 * </p>
 * <pre>
 *     -DenableJMX=true
 * </pre>
 *
 * @author Pascal Essiembre
 * @see CrawlerConfig
 */
public abstract class Crawler {

    private static final Logger LOG =
            LoggerFactory.getLogger(Crawler.class);

    private static final int MINIMUM_DELAY = 1;

    private final CrawlerConfig config;
    private final Collector collector;
    private Importer importer;
    private final CrawlerCommitterService committers;

    private Path workDir;
    private Path tempDir;
    private Path downloadDir;

    private boolean stopped;

    private CrawlerMonitor monitor;
    private CrawlProgressLogger progressLogger;
    private IDataStoreEngine dataStoreEngine;
    private CrawlDocInfoService crawlDocInfoService;

    /**
     * Constructor.
     * @param config crawler configuration
     * @param collector the collector this crawler is attached to
     */
    public Crawler(CrawlerConfig config, Collector collector) {
        Objects.requireNonNull(config, "'config' must not be null");
        Objects.requireNonNull(config, "'collector' must not be null");
        this.config = config;
        this.collector = collector;
        committers = new CrawlerCommitterService(this);
    }

    /**
     * Gets the event manager.
     * @return event manager
     * @since 2.0.0
     */
    public EventManager getEventManager() {
        return collector.getEventManager();
    }

    public CrawlerMonitor getMonitor() {
        return monitor;
    }

    public CrawlerCommitterService getCommitterService() {
        return committers;
    }

    public String getId() {
        return config.getId();
    }

    /**
     * Whether the crawler job was stopped.
     * @return <code>true</code> if stopped
     */
    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        getEventManager().fire(
                new CrawlerEvent.Builder(CRAWLER_STOP_BEGIN, this).build());
        stopped = true;
        LOG.info("Stopping the crawler.");
    }

    /**
     * Gets the crawler Importer module.
     * @return the Importer
     */
    public Importer getImporter() {
        return importer;
    }

    public CachedStreamFactory getStreamFactory() {
        return collector.getStreamFactory();
    }

    /**
     * Gets the crawler configuration.
     * @return the crawler configuration
     */
    public CrawlerConfig getCrawlerConfig() {
        return config;
    }

    // really make public? Or have a getCollectorId() method instead?
    public Collector getCollector() {
        return collector;
    }

    /**
     * Gets the directory where files needing to be persisted between
     * crawling sessions are kept.
     * @return working directory, never <code>null</code>
     */
    public Path getWorkDir() {
        if (workDir != null) {
            return workDir;
        }

        String fileSafeId = FileUtil.toSafeFileName(getId());
        Path dir = collector.getWorkDir().resolve(fileSafeId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create crawler working directory.", e);
        }
        workDir = dir;
        return workDir;
    }

    /**
     * Gets the directory where most temporary files are created for the
     * duration of a crawling session. Those files are typically deleted
     * after a crawling session.
     * @return temporary directory, never <code>null</code>
     */
    public Path getTempDir() {
        if (tempDir != null) {
            return tempDir;
        }

        String fileSafeId = FileUtil.toSafeFileName(getId());
        Path dir = collector.getTempDir().resolve(fileSafeId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create crawler temp directory.", e);
        }
        tempDir = dir;
        return tempDir;
    }


    public Path getDownloadDir() {
        return downloadDir;
    }

    /**
     * Starts crawling.
     */
    public void start() {
        boolean resume = initCrawler();
        importer = new Importer(
                getCrawlerConfig().getImporterConfig(),
                getEventManager());
        monitor = new CrawlerMonitor(this);
        //TODO make interval configurable
        //TODO make general logging messages verbosity configurable
        progressLogger = new CrawlProgressLogger(
                monitor, 30 * 1000);
        progressLogger.startTracking();

        if (Boolean.getBoolean("enableJMX")) {
            CrawlerMonitorJMX.register(this);
        }

        try {
            getEventManager().fire(
                    new CrawlerEvent.Builder(CRAWLER_RUN_BEGIN, this).build());
            logUsefulInfo();
            beforeCrawlerExecution(resume);

            //TODO move this code to a config validator class?
            if (StringUtils.isBlank(getCrawlerConfig().getId())) {
                throw new CollectorException("Crawler must be given "
                        + "a unique identifier (id).");
            }
            doExecute();
        } finally {
            try {
                afterCrawlerExecution();
            } finally {
                progressLogger.stopTracking();
                try {
                    LOG.info("Execution Summary:{}",
                            progressLogger.getExecutionSummary());
                } finally {
                    destroyCrawler();
                }
            }
            if (Boolean.getBoolean("enableJMX")) {
                CrawlerMonitorJMX.unregister(this);
            }
        }
    }

    private void logUsefulInfo() {
        if (Boolean.getBoolean("enableJMX")) {
            LOG.info("JMX support enabled.");
        } else {
            LOG.info("JMX support disabled. To enable, set -DenableJMX=true "
                    + "system property as JVM argument.");
        }
    }

    protected boolean initCrawler() {
        Thread.currentThread().setName(getId());
        MdcUtil.setCrawlerId(getId());

        getEventManager().fire(new CrawlerEvent.Builder(
                CRAWLER_INIT_BEGIN, this).message(
                        "Initializing crawler \"" + getId() + "\"...").build());

        //--- Ensure good state/config ---
        if (StringUtils.isBlank(config.getId())) {
            throw new CollectorException("Crawler must be given "
                    + "a unique identifier (id).");
        }

        //--- Directories ---
        downloadDir = getWorkDir().resolve("downloads");
        dataStoreEngine = config.getDataStoreEngine();
        dataStoreEngine.init(this);
        crawlDocInfoService = new CrawlDocInfoService(
                this, getCrawlDocInfoType());

        //--- Committers ---
        // index will be appended to committer workdir for each one
        CommitterContext committerContext = CommitterContext.builder()
                .setEventManager(getEventManager())
                .setWorkDir(getWorkDir().resolve("committer"))
                .setStreamFactory(getStreamFactory())
                .build();
        committers.init(committerContext);

        boolean resuming = crawlDocInfoService.open();
        getEventManager().fire(new CrawlerEvent.Builder(CRAWLER_INIT_END, this)
                .message("Crawler \"" + getId()
                        + "\" initialized successfully.").build());
        return resuming;
    }

    protected Class<? extends CrawlDocInfo> getCrawlDocInfoType() {
        return CrawlDocInfo.class;
    }

    public IDataStoreEngine getDataStoreEngine() {
        return dataStoreEngine;
    }

    public CrawlDocInfoService getDocInfoService() {
        return crawlDocInfoService;
    }

    public void clean() {
        initCrawler();
        getEventManager().fire(
                new CrawlerEvent.Builder(CRAWLER_CLEAN_BEGIN, this)
                    .message("Cleaning cached crawler \""
                            + getId() + "\" data...")
                    .build());
        try {
            committers.clean();
            dataStoreEngine.clean();
            destroyCrawler();
            FileUtils.deleteDirectory(getTempDir().toFile());
            FileUtils.deleteDirectory(getWorkDir().toFile());
            getEventManager().fire(
                    new CrawlerEvent.Builder(CRAWLER_CLEAN_END, this)
                        .message("Done cleaning crawler \""
                                + getId() + "\".")
                        .build());
        } catch (IOException e) {
            throw new CollectorException("Could not clean \"" + getId()
                    + "\" crawler directory.", e);
        }
    }


    public void importDataStore(Path inFile) {
        initCrawler();
        try {
            DataStoreImporter.importDataStore(this, inFile);
        } catch (IOException e) {
            throw new CollectorException("Could not import data store.", e);
        } finally {
            destroyCrawler();
        }

    }
    public Path exportDataStore(Path dir) {
        initCrawler();
        try {
            return DataStoreExporter.exportDataStore(this, dir);
        } catch (IOException e) {
            throw new CollectorException("Could not export data store.", e);
        } finally {
            destroyCrawler();
        }
    }

    protected void destroyCrawler() {
        ofNullable(crawlDocInfoService).ifPresent(CrawlDocInfoService::close);
        ofNullable(dataStoreEngine).ifPresent(IDataStoreEngine::close);

        //TODO shall we clear crawler listeners, or leave to collector impl
        // to clean all?
        // eventManager.clearListeners();
        ofNullable(committers).ifPresent(CrawlerCommitterService::close);
    }

    // Really needed since we have events for that now?
    /**
     * Gives crawler implementations a chance to prepare before execution starts
     * Invoked right after the {@link CrawlerEvent#CRAWLER_RUN_BEGIN} is fired.
     * This method is different than the {@link #initCrawler()} method, which
     * is invoked for any type of actions where as this one is only invoked
     * before an effective request for crawling.
     * @param resume whether the crawl is resuming from an unfinished session.
     */
    protected abstract void beforeCrawlerExecution(boolean resume);
    /**
     * Gives crawler implementations a chance to do something right after
     * the crawler is done processing its last reference, before all resources
     * are shut down.
     * Invoked right after {@link CrawlerEvent#CRAWLER_STOP_END} or
     * {@link CrawlerEvent#CRAWLER_RUN_END} (depending which of the two is
     * triggered).
     */
    protected abstract void afterCrawlerExecution();

    protected void doExecute() {

        //--- Process start/queued references ----------------------------------
        LOG.info("Crawling references...");
        processReferences(new ProcessFlags());

        if (!isStopped()) {
            handleOrphans();
        }

        LOG.debug("Removing empty directories");
        FileUtil.deleteEmptyDirs(getDownloadDir().toFile());
        getEventManager().fire(new CrawlerEvent.Builder(
                (isStopped() ? CRAWLER_STOP_END : CRAWLER_RUN_END),
                this).build());
        LOG.info("Crawler {}", (isStopped() ? "stopped." : "completed."));
    }

    protected void handleOrphans() {

        OrphansStrategy strategy = config.getOrphansStrategy();
        if (strategy == null) {
            // null is same as ignore
            strategy = OrphansStrategy.IGNORE;
        }

        // If PROCESS, we do not care to validate if really orphan since
        // all cache items will be reprocessed regardless
        if (strategy == OrphansStrategy.PROCESS) {
            reprocessCacheOrphans();
            return;
        }

        if (strategy == OrphansStrategy.DELETE) {
            deleteCacheOrphans();
        }
        // else, ignore (i.e. don't do anything)
        //TODO log how many where ignored (cache count)
    }

    protected boolean isMaxDocuments() {
        //TODO replace check for "processedCount" vs "maxDocuments"
        // with event counts vs max committed, max processed, max etc...
        return getCrawlerConfig().getMaxDocuments() > -1
                && monitor.getProcessedCount()
                        >= getCrawlerConfig().getMaxDocuments();
    }

    protected void reprocessCacheOrphans() {
        if (isMaxDocuments()) {
            LOG.info("Max documents reached. "
                    + "Not reprocessing orphans (if any).");
            return;
        }
        LOG.info("Reprocessing any cached/orphan references...");

        MutableLong count = new MutableLong();
        crawlDocInfoService.forEachCached((k, v) -> {
            executeQueuePipeline(v);
            count.increment();
            return true;
        });

        if (count.longValue() > 0) {
            processReferences(new ProcessFlags().orphan());
        }
        LOG.info("Reprocessed {} cached/orphan references.", count);
    }

    protected abstract void executeQueuePipeline(CrawlDocInfo ref);

    protected void deleteCacheOrphans() {
        LOG.info("Deleting orphan references (if any)...");

        MutableLong count = new MutableLong();
        crawlDocInfoService.forEachCached((k, v) -> {
            crawlDocInfoService.queue(v);
            count.increment();
            return true;
        });
        if (count.longValue() > 0) {
            processReferences(new ProcessFlags().delete());
        }
        LOG.info("Deleted {} orphan references.", count);
    }

    protected void processReferences(final ProcessFlags flags) {
        int numThreads = getCrawlerConfig().getNumThreads();
        final CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService execService = Executors.newFixedThreadPool(numThreads);
        try {
            for (int i = 0; i < numThreads; i++) {
                final int threadIndex = i + 1;
                LOG.debug("Crawler thread #{} starting...", threadIndex);
                execService.execute(new ProcessReferencesRunnable(
                        latch, flags, threadIndex));
            }
            latch.await();
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             throw new CollectorException(e);
        } finally {
            execService.shutdown();
        }
    }

    protected enum ReferenceProcessStatus {
        MAX_REACHED,
        QUEUE_EMPTY,
        OK;
    }

    // return <code>true</code> if more references to process
    protected ReferenceProcessStatus processNextReference(
            final ProcessFlags flags) {

        if (!flags.delete && isMaxDocuments()) {
            LOG.info("Maximum documents reached: {}",
                    getCrawlerConfig().getMaxDocuments());
            return ReferenceProcessStatus.MAX_REACHED;
        }
        Optional<CrawlDocInfo> queuedDocInfo =
                crawlDocInfoService.pollQueue();

        LOG.trace("Processing next reference from Queue: {}",
                queuedDocInfo);
        if (queuedDocInfo.isPresent()) {
            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                watch = new StopWatch();
                watch.start();
            }
            processNextQueuedCrawlData(queuedDocInfo.get(), flags);
            if (LOG.isDebugEnabled()) {
                watch.stop();
                LOG.debug("{} to process: {}", watch,
                        queuedDocInfo.get().getReference());
            }
        } else {
            long activeCount = crawlDocInfoService.getActiveCount();
            boolean queueEmpty = crawlDocInfoService.isQueueEmpty();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Number of references currently being "
                        + "processed: {}", activeCount);
                LOG.trace("Is reference queue empty? {}", queueEmpty);
            }
            if (activeCount == 0 && queueEmpty) {
                return ReferenceProcessStatus.QUEUE_EMPTY;
            }
            Sleeper.sleepMillis(MINIMUM_DELAY);
        }
        return ReferenceProcessStatus.OK;
    }

//TODO rely on events?
    protected void initCrawlDoc(CrawlDoc document) {
        // default does nothing
    }

    private void processNextQueuedCrawlData(
            CrawlDocInfo docInfo, ProcessFlags flags) {
        String reference = docInfo.getReference();

        CrawlDocInfo cachedDocInfo =
                crawlDocInfoService.getCached(reference).orElse(null);

        CrawlDoc doc = new CrawlDoc(
                docInfo, cachedDocInfo, getStreamFactory().newInputStream(),
                flags.orphan);

        ImporterPipelineContext context =
                new ImporterPipelineContext(Crawler.this, doc);

        doc.getMetadata().set(
                CrawlDocMetadata.IS_CRAWL_NEW,
                cachedDocInfo == null);

        initCrawlDoc(doc);

        try {
            if (flags.delete) {
                deleteReference(doc);
                finalizeDocumentProcessing(doc);
                return;
            }
            LOG.debug("Processing reference: {}", reference);

            ImporterResponse response = executeImporterPipeline(context);

            if (response != null) {
                processImportResponse(response, doc);//docInfo, cachedDocInfo);
            } else {
                if (docInfo.getState().isNewOrModified()) {
                    docInfo.setState(CrawlState.REJECTED);
                }
                //TODO Fire an event here? If we get here, the importer did
                //not kick in,
                //so do not fire REJECTED_IMPORT (like it used to).
                //Errors should have fired
                //something already so do not fire two REJECTED... but
                //what if a previous issue did not fire a REJECTED_*?
                //This should not happen, but keep an eye on that.
                //OR do we want to always fire REJECTED_IMPORT on import failure
                //(in addition to whatever) and maybe a new REJECTED_COLLECTOR
                //when it did not reach the importer module?
                finalizeDocumentProcessing(doc);
            }
        } catch (Throwable e) {
            //TODO do we really want to catch anything other than
            // HTTPFetchException?  In case we want special treatment to the
            // class?
            docInfo.setState(CrawlState.ERROR);
            getEventManager().fire(
                    new CrawlerEvent.Builder(REJECTED_ERROR, this)
                            .crawlDocInfo(docInfo)
                            .exception(e)
                            .build());
            if (LOG.isDebugEnabled()) {
                LOG.info("Could not process document: {} ({})",
                        reference, e.getMessage(), e);
            } else {
                LOG.info("Could not process document: {} ({})",
                        reference, e.getMessage());
            }
            finalizeDocumentProcessing(doc);

            // Rethrow exception is we want the crawler to stop
            List<Class<? extends Exception>> exceptionClasses =
                    config.getStopOnExceptions();
            if (CollectionUtils.isNotEmpty(exceptionClasses)) {
                for (Class<? extends Exception> c : exceptionClasses) {
                    if (c.isAssignableFrom(e.getClass())) {
                        throw e;
                    }
                }
            }
        }
    }

    private void processImportResponse(
            ImporterResponse response, CrawlDoc doc) {

        CrawlDocInfo docInfo = doc.getDocInfo();

        String msg = response.getImporterStatus().toString();
        if (response.getNestedResponses().length > 0) {
            msg += "(" + response.getNestedResponses().length
                    + " nested responses.)";
        }

        if (response.isSuccess()) {
            getEventManager().fire(
                    new CrawlerEvent.Builder(DOCUMENT_IMPORTED, this)
                        .crawlDocInfo(docInfo)
                        .subject(response)
                        .message(msg)
                        .build());
            executeCommitterPipeline(this, doc);
        } else {
            docInfo.setState(CrawlState.REJECTED);
            getEventManager().fire(
                    new CrawlerEvent.Builder(REJECTED_IMPORT, this)
                        .crawlDocInfo(docInfo)
                        .subject(response)
                        .message(msg)
                        .build());
            LOG.debug("Importing unsuccessful for \"{}\": {}",
                    docInfo.getReference(),
                    response.getImporterStatus().getDescription());
        }
        finalizeDocumentProcessing(doc);
        ImporterResponse[] children = response.getNestedResponses();
        for (ImporterResponse childResponse : children) {
            //TODO have a createEmbeddedDoc method instead?
            CrawlDocInfo childDocInfo = createChildDocInfo(
                    childResponse.getReference(), docInfo);
            CrawlDocInfo childCachedDocInfo =
                    crawlDocInfoService.getCached(
                            childResponse.getReference()).orElse(null);

            // Here we create a CrawlDoc since the document from the response
            // is (or can be) just a Doc, which does not hold all required
            // properties for crawling.
            //TODO refactor Doc vs CrawlDoc to have only one instance
            // so we do not have to create such copy?
            Doc childResponseDoc = childResponse.getDocument();
            CrawlDoc childCrawlDoc = new CrawlDoc(
                    childDocInfo, childCachedDocInfo,
                    childResponseDoc == null
                            ? CachedInputStream.cache(new NullInputStream(0))
                            : childResponseDoc.getInputStream());
            if (childResponseDoc != null) {
                childCrawlDoc.getMetadata().putAll(
                        childResponseDoc.getMetadata());
            }

            processImportResponse(childResponse, childCrawlDoc);
        }
    }


    private void finalizeDocumentProcessing(CrawlDoc doc) {

        CrawlDocInfo docInfo = doc.getDocInfo();
        CrawlDocInfo cachedDocInfo = doc.getCachedDocInfo();

        //--- Ensure we have a state -------------------------------------------
        if (docInfo.getState() == null) {
            LOG.warn("Reference status is unknown for \"{}\". "
                    + "This should not happen. Assuming bad status.",
                    docInfo.getReference());
            docInfo.setState(CrawlState.BAD_STATUS);
        }

        try {

            // important to call this before copying properties further down
            beforeFinalizeDocumentProcessing(doc);

            //--- If doc crawl was incomplete, set missing info from cache -----
            // If document is not new or modified, it did not go through
            // the entire crawl life cycle for a document so maybe not all info
            // could be gathered for a reference.  Since we do not want to lose
            // previous information when the crawl was effective/good
            // we copy it all that is non-null from cache.
            if (!docInfo.getState().isNewOrModified() && cachedDocInfo != null) {
                //TODO maybe new CrawlData instances should be initialized with
                // some of cache data available instead?
                BeanUtil.copyPropertiesOverNulls(docInfo, cachedDocInfo);
            }

            //--- Deal with bad states (if not already deleted) ----------------
            if (!docInfo.getState().isGoodState()
                    && !docInfo.getState().isOneOf(CrawlState.DELETED)) {

                //TODO If duplicate, consider it as spoiled if a cache version
                // exists in a good state.
                // This involves elaborating the concept of duplicate
                // or "reference change" in this core project. Otherwise there
                // is the slim possibility right now that a Collector
                // implementation marking references as duplicate may
                // generate orphans (which may be caught later based
                // on how orphans are handled, but they should not be ever
                // considered orphans in the first place).
                // This could remove the need for the
                // markReferenceVariationsAsProcessed(...) method

                SpoiledReferenceStrategy strategy =
                        getSpoiledStateStrategy(docInfo);

                if (strategy == SpoiledReferenceStrategy.IGNORE) {
                    LOG.debug("Ignoring spoiled reference: {}",
                            docInfo.getReference());
                } else if (strategy == SpoiledReferenceStrategy.DELETE) {
                    // Delete if previous state exists and is not already
                    // marked as deleted.
                    if (cachedDocInfo != null
                            && !cachedDocInfo.getState().isOneOf(
                                    CrawlState.DELETED)) {
                        deleteReference(doc);
                    }
                } else // GRACE_ONCE:
                // Delete if previous state exists and is a bad state,
                // but not already marked as deleted.
                if (cachedDocInfo != null
                        && !cachedDocInfo.getState().isOneOf(
                                CrawlState.DELETED)) {
                    if (!cachedDocInfo.getState().isGoodState()) {
                        deleteReference(doc);
                    } else {
                        LOG.debug("This spoiled reference is "
                                + "being graced once (will be deleted "
                                + "next time if still spoiled): {}",
                                docInfo.getReference());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not finalize processing of: {} ({})",
                    docInfo.getReference(), e.getMessage(), e);
        }

        //--- Mark reference as Processed --------------------------------------
        try {
            crawlDocInfoService.processed(docInfo);
            markReferenceVariationsAsProcessed(docInfo);

            progressLogger.logProgress();


        } catch (Exception e) {
            LOG.error("Could not mark reference as processed: {} ({})",
                    docInfo.getReference(), e.getMessage(), e);
        }

        try {
            doc.getInputStream().dispose();
        } catch (Exception e) {
            LOG.error("Could not dispose of resources.", e);
        }
    }

    /**
     * Gives implementors a change to take action on a document before
     * its processing is being finalized (cycle end-of-life for a crawled
     * reference). Default implementation does nothing.
     * @param doc the document
     */
    protected void beforeFinalizeDocumentProcessing(CrawlDoc doc) {
        //NOOP
        //TODO rely on event instead???
    }

    protected abstract void markReferenceVariationsAsProcessed(
            CrawlDocInfo crawlRef);


    protected abstract CrawlDocInfo createChildDocInfo(
            String embeddedReference, CrawlDocInfo parentCrawlRef);

    protected abstract ImporterResponse executeImporterPipeline(
            ImporterPipelineContext context);

    //TODO, replace with DocumentPipelineContext?
    protected abstract void executeCommitterPipeline(
            Crawler crawler, CrawlDoc doc);

    private SpoiledReferenceStrategy getSpoiledStateStrategy(
            CrawlDocInfo crawlData) {
        ISpoiledReferenceStrategizer strategyResolver =
                config.getSpoiledReferenceStrategizer();
        SpoiledReferenceStrategy strategy =
                strategyResolver.resolveSpoiledReferenceStrategy(
                        crawlData.getReference(), crawlData.getState());
        if (strategy == null) {
            // Assume the generic default (DELETE)
            strategy =  GenericSpoiledReferenceStrategizer
                    .DEFAULT_FALLBACK_STRATEGY;
        }
        return strategy;
    }

    private void deleteReference(CrawlDoc doc) {
        LOG.debug("Deleting reference: {}", doc.getReference());

        doc.getDocInfo().setState(CrawlState.DELETED);

        // Event triggered by service
        committers.delete(doc);
    }

    //TODO make enum if never mixed, and add "default"
    private static final class ProcessFlags {
        private boolean delete;
        private boolean orphan;
        private ProcessFlags delete() {
            delete = true;
            return this;
        }
        private ProcessFlags orphan() {
            orphan = true;
            return this;
        }
    }

    protected boolean isQueueInitialized() {
        return true;
    }

    private final class ProcessReferencesRunnable implements Runnable {
        private final ProcessFlags flags;
        private final CountDownLatch latch;
        private final int threadIndex;

        private ProcessReferencesRunnable(
                CountDownLatch latch,
                ProcessFlags flags,
                int threadIndex) {
            this.latch = latch;
            this.flags = flags;
            this.threadIndex = threadIndex;
        }

        @Override
        public void run() {
            MdcUtil.setCrawlerId(getId());
            Thread.currentThread().setName(getId() + "#" + threadIndex);

            LOG.debug("Crawler thread #{} started.", threadIndex);

            try {
                getEventManager().fire(new CrawlerEvent.Builder(
                        CrawlerEvent.CRAWLER_RUN_THREAD_BEGIN, Crawler.this)
                            .subject(Thread.currentThread())
                            .build());
                while (!isStopped()) {
                    try {
                        ReferenceProcessStatus status =
                                processNextReference(flags);
                        if (status == MAX_REACHED) {
                            stop();
                            break;
                        }
                        if (status == QUEUE_EMPTY) {
                            if (isQueueInitialized()) {
                                break;
                            }
                            LOG.info("References are still being queued. "
                                    + "Waiting for new references...");
                            Sleeper.sleepSeconds(5);
                        }
                    } catch (Exception e) {
                        LOG.error(
                              "An error occured that could compromise "
                            + "the stability of the crawler. Stopping "
                            + "excution to avoid further issues...", e);
                        stop();
                    }
                }
            } catch (Exception e) {
                LOG.error("Problem in thread execution.", e);
            } finally {
                latch.countDown();
                getEventManager().fire(new CrawlerEvent.Builder(
                        CrawlerEvent.CRAWLER_RUN_THREAD_END, Crawler.this)
                            .subject(Thread.currentThread())
                            .build());
            }
        }
    }

    @Override
    public String toString() {
        return getId();
    }
}
