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

import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_FINISHED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_RESUMED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STARTED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STOPPED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STOPPING;
import static com.norconex.collector.core.crawler.CrawlerEvent.DOCUMENT_COMMITTED_REMOVE;
import static com.norconex.collector.core.crawler.CrawlerEvent.DOCUMENT_IMPORTED;
import static com.norconex.collector.core.crawler.CrawlerEvent.REJECTED_ERROR;
import static com.norconex.collector.core.crawler.CrawlerEvent.REJECTED_IMPORT;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.Collector;
import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.CrawlerConfig.OrphansStrategy;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.collector.core.jmx.Monitoring;
import com.norconex.collector.core.pipeline.importer.ImporterPipelineContext;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.collector.core.spoil.SpoiledReferenceStrategy;
import com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.Sleeper;
import com.norconex.commons.lang.bean.BeanUtil;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.time.DurationFormatter;
import com.norconex.importer.Importer;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.doc.ImporterMetadata;
import com.norconex.importer.response.ImporterResponse;
import com.norconex.jef5.job.AbstractResumableJob;
import com.norconex.jef5.status.JobStatus;
import com.norconex.jef5.status.JobStatusUpdater;
import com.norconex.jef5.suite.JobSuite;

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
 */
//TODO document that logger should have thread name instead of explicitely
// adding id everywhere when logging?
public abstract class Crawler extends AbstractResumableJob {

    private static final Logger LOG =
            LoggerFactory.getLogger(Crawler.class);

    private static final int DOUBLE_PROGRESS_SCALE = 4;
    private static final int DOUBLE_PERCENT_SCALE = -2;
    private static final int MINIMUM_DELAY = 1;
    private static final long STATUS_LOGGING_INTERVAL =
            TimeUnit.SECONDS.toMillis(5);
    private static final InheritableThreadLocal<Crawler> INSTANCE =
            new InheritableThreadLocal<>();

    private final CrawlerConfig config;
    private final Collector collector;
//    private final EventManager eventManager;
    private Importer importer;

    private Path workDir;
    private Path tempDir;
    private Path downloadDir;

//    private CachedStreamFactory streamFactory;

    private boolean stopped;
    // This processedCount does not take into account alternate references such
    // as redirects. It is a cleaner representation for end-users and speed
    // things a bit bit not having to obtain that value from the database at
    // every progress change.,
    private int processedCount;
    private long lastStatusLoggingTime;

//    private final EventManager eventManager;

//    /**
//     * Constructor.
//     * @param config crawler configuration
//     * @param eventManager event manager
//     */
//    public Crawler(CrawlerConfig config, EventManager eventManager) {
//        //TODO pass Collector instead and grab eventManager from it?
//        // That could give is other useful init info
//        this.config = config;
//        this.eventManager = eventManager;
//        INSTANCE.set(this);
//    }
    /**
     * Constructor.
     * @param config crawler configuration
     * @param collector the collector this crawler is attached to
     */
    public Crawler(CrawlerConfig config, Collector collector) {
        Objects.requireNonNull(config, "'config' must not be null");
        Objects.requireNonNull(config, "'collector' must not be null");

        //TODO pass Collector instead and grab eventManager from it?
        // That could give is other useful init info
        this.config = config;
        this.collector = collector;
//        this.eventManager = new EventManager(collector.getEventManager());
        INSTANCE.set(this);
    }

    public static Crawler get() {
        return INSTANCE.get();
    }

    /**
     * Gets the event manager.
     * @return event manager
     * @since 2.0.0
     */
    public EventManager getEventManager() {
        return collector.getEventManager();
//        return eventManager;
    }

    @Override
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

    @Override
    public void stop(JobStatus jobStatus, JobSuite suite) {
        getEventManager().fire(CrawlerEvent.create(CRAWLER_STOPPING, this));
        stopped = true;
        LOG.info("{}: Stopping the crawler.", getId());
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

//    public void fireCrawlerEvent(
//            String eventType, ICrawlData crawlData, Object subject) {
//        crawlerEventManager.fireCrawlerEvent(
//                new XMLValidationEvent(eventType, crawlData, subject));
//    }

    protected Collector getCollector() {
        return collector;
    }

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


//    public Path getBaseDownloadDir() {
//        return getCrawlerConfig().getWorkDir()
//                .toAbsolutePath().resolve("downloads");
////        return new File(
////                getCrawlerConfig().getWorkDir().getAbsolutePath(), "downloads");
//    }
    public Path getDownloadDir() {
        return downloadDir;
//        return getBaseDownloadDir().resolve(FileUtil.toSafeFileName(
//                getCrawlerConfig().getId()));
    }



//    @Override
//    public CrawlerEventManager getCrawlerEventManager() {
//        return crawlerEventManager;
//    }

    @Override
    protected void startExecution(
            JobStatusUpdater statusUpdater, JobSuite suite) {
        getEventManager().fire(CrawlerEvent.create(CRAWLER_STARTED, this));
        doExecute(statusUpdater, suite, false);
    }

    @Override
    protected void resumeExecution(
            JobStatusUpdater statusUpdater, JobSuite suite) {
        getEventManager().fire(CrawlerEvent.create(CRAWLER_RESUMED, this));
        doExecute(statusUpdater, suite, true);
    }

    private void doExecute(JobStatusUpdater statusUpdater,
            JobSuite suite, boolean resume) {

//        try {
//            Files.createDirectories(config.getWorkDir());
//        } catch (IOException e) {
//            throw new CollectorException("Cannot create working directory: "
//                    + config.getWorkDir(), e);
//        }

        initCrawler();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ICrawlDataStore crawlDataStore = createCrawlDataStore(resume);

//        this.crawlerEventManager = new CrawlerEventManager(
//                this, getCrawlerConfig().getCrawlerListeners());
        importer = new Importer(getCrawlerConfig().getImporterConfig());
//        streamFactory = importer.getStreamFactory();
        processedCount = crawlDataStore.getProcessedCount();
        if (Boolean.getBoolean("enableJMX")) {
            registerMonitoringMbean(crawlDataStore);
        }

        try {
            //TODO rename "beforeExecution and afterExecution"?
            prepareExecution(statusUpdater, suite, crawlDataStore, resume);

            //TODO move this code to a config validator class?
            if (StringUtils.isBlank(getCrawlerConfig().getId())) {
                throw new CollectorException("Crawler must be given "
                        + "a unique identifier (id).");
            }

            lastStatusLoggingTime = System.currentTimeMillis();
            execute(statusUpdater, suite, crawlDataStore);
        } finally {
            try {
                stopWatch.stop();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Crawler executed in {}.",
                            DurationFormatter.FULL.withLocale(
                                   Locale.ENGLISH).format(stopWatch.getTime()));
                }
                cleanupExecution(statusUpdater, suite, crawlDataStore);
                cleanupCrawler();
            } finally {
                crawlDataStore.close();
            }
        }

    }

    protected void initCrawler() {
        //--- Ensure good state/config ---
        if (StringUtils.isBlank(config.getId())) {
            throw new CollectorException("Crawler must be given "
                    + "a unique identifier (id).");
        }

        //--- Directories ---
//        String fileSafeId = FileUtil.toSafeFileName(getId());
//        workDir = collector.getWorkDir().resolve(fileSafeId);
//        tempDir = collector.getTempDir().resolve(fileSafeId);
        downloadDir = getWorkDir().resolve("downloads");
//        try {
//            Files.createDirectories(workDir);
//            Files.createDirectories(tempDir);
//        } catch (IOException e) {
//            throw new CollectorException(
//                    "Could not create crawler directory.", e);
//        }
    }

//    private Path createWorkDir() {
//        String fileSafeId = FileUtil.toSafeFileName(getId());
//        Path dir = collector.getWorkDir().resolve(fileSafeId);
//        try {
//            Files.createDirectories(workDir);
//            Files.createDirectories(tempDir);
//        } catch (IOException e) {
//            throw new CollectorException(
//                    "Could not create crawler working directory.", e);
//        }
//
//    }
//    private Path createTempDir() {
//        Path dir;
//        if (collectorConfig.getTempDir() == null) {
//            dir = workDir.resolve("temp");
//        } else {
//            String fileSafeId = FileUtil.toSafeFileName(getId());
//            dir = collectorConfig.getTempDir().resolve(fileSafeId);
//        }
//        try {
//            Files.createDirectories(dir);
//        } catch (IOException e) {
//            throw new CollectorException(
//                    "Could not create temp directory: " + dir, e);
//        }
//        return dir;
//    }

    protected void cleanupCrawler() {
        //TODO shall we, or leave to collector to clean all?
        // eventManager.clearListeners();
    }

    protected ICrawlDataStore createCrawlDataStore(boolean resume) {
        return config.getCrawlDataStoreFactory().createCrawlDataStore(
                config, resume);
    }

    protected abstract void prepareExecution(
            JobStatusUpdater statusUpdater, JobSuite suite,
            ICrawlDataStore refStore, boolean resume);

    protected abstract void cleanupExecution(
            JobStatusUpdater statusUpdater, JobSuite suite,
            ICrawlDataStore refStore);


    protected void execute(JobStatusUpdater statusUpdater,
            JobSuite suite, ICrawlDataStore crawlDataStore) {

        //--- Process start/queued references ----------------------------------
        LOG.info("{}: Crawling references...", getId());
        ImporterPipelineContext contextPrototype =
                new ImporterPipelineContext(this, crawlDataStore);
        processReferences(statusUpdater, suite, contextPrototype);

        if (!isStopped()) {
            handleOrphans(crawlDataStore, statusUpdater, suite);
        }

        ICommitter committer = getCrawlerConfig().getCommitter();
        if (committer != null) {
            LOG.info("{}: Crawler {}: committing documents.",
                    getId(), (isStopped() ? "stopping" : "finishing"));
            committer.commit();
        }

        LOG.info("{}: {} reference(s) processed.", getId(), processedCount);

        LOG.debug("{}: Removing empty directories", getId());
        FileUtil.deleteEmptyDirs(getDownloadDir().toFile());

        if (!isStopped()) {
            getEventManager().fire(CrawlerEvent.create(CRAWLER_FINISHED, this));
        } else {
            getEventManager().fire(CrawlerEvent.create(CRAWLER_STOPPED, this));
        }
        LOG.info(getId() + ": Crawler "
                + (isStopped() ? "stopped." : "completed."));
    }

    protected void handleOrphans(ICrawlDataStore crawlStore,
            JobStatusUpdater statusUpdater, JobSuite suite) {

        OrphansStrategy strategy = config.getOrphansStrategy();
        if (strategy == null) {
            // null is same as ignore
            strategy = OrphansStrategy.IGNORE;
        }

        // If PROCESS, we do not care to validate if really orphan since
        // all cache items will be reprocessed regardless
        if (strategy == OrphansStrategy.PROCESS) {
            reprocessCacheOrphans(crawlStore, statusUpdater, suite);
            return;
        }

        if (strategy == OrphansStrategy.DELETE) {
            deleteCacheOrphans(crawlStore, statusUpdater, suite);
        }
        // else, ignore (i.e. don't do anything)
        //TODO log how many where ignored (cache count)
    }

    protected boolean isMaxDocuments() {
        return getCrawlerConfig().getMaxDocuments() > -1
                && processedCount >= getCrawlerConfig().getMaxDocuments();
    }

    protected void reprocessCacheOrphans(
            ICrawlDataStore crawlDataStore,
            JobStatusUpdater statusUpdater, JobSuite suite) {
        if (isMaxDocuments()) {
            LOG.info(getId() + ": Max documents reached. "
                    + "Not reprocessing orphans (if any).");
            return;
        }
        LOG.info("{}: Reprocessing any cached/orphan references...", getId());

        long count = 0;
        Iterator<ICrawlData> it = crawlDataStore.getCacheIterator();
        if (it != null) {
            while (it.hasNext()) {
                ICrawlData crawlData = it.next();
                executeQueuePipeline(crawlData, crawlDataStore);
                count++;
            }
            ImporterPipelineContext contextPrototype =
                    new ImporterPipelineContext(this, crawlDataStore);
            contextPrototype.setOrphan(true);
            processReferences(statusUpdater, suite, contextPrototype);
        }
        LOG.debug("{}: Reprocessed {} cached/orphan references.",
                getId(), count);
    }

    protected abstract void executeQueuePipeline(
            ICrawlData crawlData, ICrawlDataStore crawlDataStore);

    protected void deleteCacheOrphans(ICrawlDataStore crawlDataStore,
            JobStatusUpdater statusUpdater, JobSuite suite) {
        LOG.info("{}: Deleting orphan references (if any)...", getId());
        long count = 0;
        Iterator<ICrawlData> it = crawlDataStore.getCacheIterator();
        if (it != null && it.hasNext()) {
            while (it.hasNext()) {
                crawlDataStore.queue(it.next());
                count++;
            }
            ImporterPipelineContext contextPrototype =
                    new ImporterPipelineContext(this, crawlDataStore);
            contextPrototype.setDelete(true);
            processReferences(statusUpdater, suite, contextPrototype);
        }
        LOG.info("{}: Deleted {} orphan references.", getId(), count);
    }


    protected void processReferences(
            final JobStatusUpdater statusUpdater,
            final JobSuite suite,
            final ImporterPipelineContext contextPrototype) {


        int numThreads = getCrawlerConfig().getNumThreads();
        final CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i + 1;
            LOG.debug("{}: Crawler thread #{} started.", getId(), threadIndex);
            pool.execute(new ProcessReferencesRunnable(
                    suite, statusUpdater, latch, contextPrototype));
        }

        try {
            latch.await();
            pool.shutdown();
        } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             throw new CollectorException(e);
        }
    }

    // return <code>true</code> if more references to process
    protected boolean processNextReference(
            final JobStatusUpdater statusUpdater,
            final ImporterPipelineContext context) {
        if (!context.isDelete() && isMaxDocuments()) {
            LOG.info("{}: Maximum documents reached: {}",
                    getId(), getCrawlerConfig().getMaxDocuments());
            return false;
        }
        ICrawlDataStore crawlStore = context.getCrawlDataStore();

        BaseCrawlData queuedCrawlData = (BaseCrawlData) crawlStore.nextQueued();
        context.setCrawlData(queuedCrawlData);

        if (LOG.isTraceEnabled()) {
            LOG.trace(getId() + " Processing next reference from Queue: "
                    + queuedCrawlData);
        }
        if (queuedCrawlData != null) {
            StopWatch watch = null;
            if (LOG.isDebugEnabled()) {
                watch = new StopWatch();
                watch.start();
            }
            processNextQueuedCrawlData(context);
            setProgress(statusUpdater, crawlStore);
            if (LOG.isDebugEnabled()) {
                watch.stop();
                LOG.debug(getId() + ": " + watch.toString()
                        + " to process: " + queuedCrawlData.getReference());
            }
        } else {
            int activeCount = crawlStore.getActiveCount();
            boolean queueEmpty = crawlStore.isQueueEmpty();
            if (LOG.isTraceEnabled()) {
                LOG.trace("{} Number of references currently being "
                        + "processed: {}", getId(), activeCount);
                LOG.trace("{} Is reference queue empty? {}",
                        getId(), queueEmpty);
            }
            if (activeCount == 0 && queueEmpty) {
                return false;
            }
            Sleeper.sleepMillis(MINIMUM_DELAY);
        }
        return true;
    }

    private void registerMonitoringMbean(ICrawlDataStore crawlDataStore) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            String objName = "com.norconex.collector.crawler:type=" +
                    getCrawlerConfig().getId();
            LOG.info("Adding MBean for JMX monitoring: {}", objName);
            ObjectName name = new ObjectName(objName);
            Monitoring mbean = new Monitoring(crawlDataStore);
            mbs.registerMBean(mbean, name);
        } catch (MalformedObjectNameException |
                 InstanceAlreadyExistsException |
                 MBeanRegistrationException |
                 NotCompliantMBeanException e) {
            throw new CollectorException(e);
        }
    }

    private void setProgress(
            JobStatusUpdater statusUpdater, ICrawlDataStore db) {
        int queued = db.getQueueSize();
        int processed = processedCount;
        int total = queued + processed;

        double progress = 0;

        if (total != 0) {
            progress = BigDecimal.valueOf(processed)
                    .divide(BigDecimal.valueOf(total),
                            DOUBLE_PROGRESS_SCALE, RoundingMode.DOWN)
                    .doubleValue();
        }
        statusUpdater.setProgress(progress);

        statusUpdater.setNote(
                NumberFormat.getIntegerInstance().format(processed)
                + " references processed out of "
                + NumberFormat.getIntegerInstance().format(total));

        if (LOG.isInfoEnabled()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastStatusLoggingTime > STATUS_LOGGING_INTERVAL) {
                lastStatusLoggingTime = currentTime;
                int percent = BigDecimal.valueOf(progress).movePointLeft(
                        DOUBLE_PERCENT_SCALE).intValue();
                LOG.info(getId() + ": " + percent + "% completed ("
                        + processed + " processed/" + total + " total)");
            }
        }
    }

    //TODO given latest changes in implementing methods, shall we only consider
    //using generics instead of having this wrapping method?
    protected abstract ImporterDocument wrapDocument(
            ICrawlData crawlData, ImporterDocument document);
    protected void initCrawlData(
            ICrawlData crawlData,
            ICrawlData cachedCrawlData,
            ImporterDocument document) {
        // default does nothing
    }

    private void processNextQueuedCrawlData(ImporterPipelineContext context) {

        BaseCrawlData crawlData = context.getCrawlData();
        ICrawlDataStore crawlDataStore = context.getCrawlDataStore();

        String reference = crawlData.getReference();
        ImporterDocument doc = wrapDocument(crawlData, new ImporterDocument(
                crawlData.getReference(), getStreamFactory().newInputStream()));
        context.setDocument(doc);

        //TODO create a composite object that has crawler, crawlData,
        // cachedCrawlData, ... To reduce the number of arguments passed around.
        // It could potentially be a base class for pipeline contexts too.
        BaseCrawlData cachedCrawlData =
                (BaseCrawlData) crawlDataStore.getCached(reference);
        context.setCachedCrawlData(cachedCrawlData);

        doc.getMetadata().set(
                CollectorMetadata.COLLECTOR_IS_CRAWL_NEW,
                cachedCrawlData == null);

        initCrawlData(crawlData, cachedCrawlData, doc);

        try {
            if (context.isDelete()) {
                deleteReference(crawlData, doc);
                finalizeDocumentProcessing(
                        crawlData, crawlDataStore, doc, cachedCrawlData);
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("{}: Processing reference: {}", getId(), reference);
            }

            ImporterResponse response = executeImporterPipeline(context);

            if (response != null) {
                processImportResponse(
                        response, crawlDataStore, crawlData, cachedCrawlData);
            } else {
                if (crawlData.getState().isNewOrModified()) {
                    crawlData.setState(CrawlState.REJECTED);
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
                finalizeDocumentProcessing(
                        crawlData, crawlDataStore, doc, cachedCrawlData);
            }
        } catch (Throwable e) {
            //TODO do we really want to catch anything other than
            // HTTPFetchException?  In case we want special treatment to the
            // class?
            crawlData.setState(CrawlState.ERROR);
            getEventManager().fire(
                    CrawlerEvent.create(REJECTED_ERROR, this, crawlData, e));
            if (LOG.isDebugEnabled()) {
                LOG.info("{}: Could not process document: {} ({})",
                        getId(), reference, e.getMessage(), e);
            } else {
                LOG.info("{}: Could not process document: {} ({})",
                        getId(), reference, e.getMessage());
            }
            finalizeDocumentProcessing(
                    crawlData, crawlDataStore, doc, cachedCrawlData);

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
            ImporterResponse response,
            ICrawlDataStore crawlDataStore,
            BaseCrawlData crawlData,
            BaseCrawlData cachedCrawlData) {

        ImporterDocument doc = response.getDocument();
        if (response.isSuccess()) {
            getEventManager().fire(CrawlerEvent.create(
                    DOCUMENT_IMPORTED, this, crawlData, response));
            ImporterDocument wrappedDoc = wrapDocument(crawlData, doc);
            executeCommitterPipeline(this, wrappedDoc,
                    crawlDataStore, crawlData, cachedCrawlData);
        } else {
            crawlData.setState(CrawlState.REJECTED);
            getEventManager().fire(CrawlerEvent.create(
                    REJECTED_IMPORT, this, crawlData, response));
            LOG.debug("{}: Importing unsuccessful for \"{}\": {}",
                    getId(), crawlData.getReference(),
                    response.getImporterStatus().getDescription());
        }
        finalizeDocumentProcessing(
                crawlData, crawlDataStore, doc, cachedCrawlData);
        ImporterResponse[] children = response.getNestedResponses();
        for (ImporterResponse child : children) {
            BaseCrawlData embeddedCrawlData = createEmbeddedCrawlData(
                    child.getReference(), crawlData);
            BaseCrawlData embeddedCachedCrawlData =
                    (BaseCrawlData) crawlDataStore.getCached(
                            child.getReference());
            processImportResponse(child, crawlDataStore,
                    embeddedCrawlData, embeddedCachedCrawlData);
        }
    }


    private void finalizeDocumentProcessing(BaseCrawlData crawlData,
            ICrawlDataStore store, ImporterDocument doc,
            ICrawlData cached) {

        //--- Ensure we have a state -------------------------------------------
        if (crawlData.getState() == null) {
            LOG.warn("{}: reference status is unknown for \"{}\". "
                    + "This should not happen. Assuming bad status.",
                    getId(), crawlData.getReference());
            crawlData.setState(CrawlState.BAD_STATUS);
        }

        try {

            // important to call this before copying properties further down
            beforeFinalizeDocumentProcessing(crawlData, store, doc, cached);

            //--- If doc crawl was incomplete, set missing info from cache -----
            // If document is not new or modified, it did not go through
            // the entire crawl life cycle for a document so maybe not all info
            // could be gathered for a reference.  Since we do not want to lose
            // previous information when the crawl was effective/good
            // we copy it all that is non-null from cache.
            if (!crawlData.getState().isNewOrModified() && cached != null) {
                //TODO maybe new CrawlData instances should be initialized with
                // some of cache data available instead?
                BeanUtil.copyPropertiesOverNulls(crawlData, cached);
            }

            //--- Deal with bad states (if not already deleted) ----------------
            if (!crawlData.getState().isGoodState()
                    && !crawlData.getState().isOneOf(CrawlState.DELETED)) {

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
                        getSpoiledStateStrategy(crawlData);

                if (strategy == SpoiledReferenceStrategy.IGNORE) {
                    LOG.debug("{}: ignoring spoiled reference: {}",
                            getId(), crawlData.getReference());
                } else if (strategy == SpoiledReferenceStrategy.DELETE) {
                    // Delete if previous state exists and is not already
                    // marked as deleted.
                    if (cached != null
                            && !cached.getState().isOneOf(CrawlState.DELETED)) {
                        deleteReference(crawlData, doc);
                    }
                } else {
                    // GRACE_ONCE:
                    // Delete if previous state exists and is a bad state,
                    // but not already marked as deleted.
                    if (cached != null
                            && !cached.getState().isOneOf(CrawlState.DELETED)) {
                        if (!cached.getState().isGoodState()) {
                            deleteReference(crawlData, doc);
                        } else {
                            LOG.debug("{}: this spoiled reference is "
                                    + "being graced once (will be deleted "
                                    + "next time if still spoiled): {}",
                                    getId(), crawlData.getReference());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("{}: Could not finalize processing of: {} ({})",
                    getId(), crawlData.getReference(), e.getMessage(), e);
        }

        //--- Mark reference as Processed --------------------------------------
        try {
            processedCount++;
            store.processed(crawlData);
            markReferenceVariationsAsProcessed(crawlData, store);
        } catch (Exception e) {
            LOG.error("{}: Could not mark reference as processed: {} ({})",
                    getId(), crawlData.getReference(), e.getMessage(), e);
        }

        try {
            if (doc != null) {
                doc.getInputStream().dispose();
            }
        } catch (Exception e) {
            LOG.error("{}: Could not dispose of resources.", getId(), e);
        }
    }

    /**
     * Gives implementors a change to take action on a document before
     * its processing is being finalized (cycle end-of-life for a crawled
     * reference). Default implementation does nothing.
     * @param crawlData crawl data with data the crawler was able to obtain,
     *                  guaranteed to have a non-null state
     * @param store crawl store
     * @param doc the document
     * @param cachedCrawlData cached crawl data
     *        (<code>null</code> if document was not crawled before)
     */
    protected void beforeFinalizeDocumentProcessing(BaseCrawlData crawlData,
            ICrawlDataStore store, ImporterDocument doc,
            ICrawlData cachedCrawlData) {
        //NOOP
    }

    protected abstract void markReferenceVariationsAsProcessed(
            BaseCrawlData crawlData, ICrawlDataStore refStore);


    protected abstract BaseCrawlData createEmbeddedCrawlData(
            String embeddedReference, ICrawlData parentCrawlData);

    protected abstract ImporterResponse executeImporterPipeline(
            ImporterPipelineContext context);

    //TODO, replace with DocumentPipelineContext?
    protected abstract void executeCommitterPipeline(
            Crawler crawler,
            ImporterDocument doc,
            ICrawlDataStore crawlDataStore,
            BaseCrawlData crawlData,
            BaseCrawlData cachedCrawlData);

    private ImporterMetadata getNullSafeMetadata(ImporterDocument doc) {
        if (doc == null) {
            return new ImporterMetadata();
        }
        return doc.getMetadata();
    }

    private SpoiledReferenceStrategy getSpoiledStateStrategy(
            BaseCrawlData crawlData) {
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

    private void deleteReference(
            BaseCrawlData crawlData, ImporterDocument doc) {
        LOG.debug("{}: Deleting reference: {}",
                getId(), crawlData.getReference());
        ICommitter committer = getCrawlerConfig().getCommitter();
        crawlData.setState(CrawlState.DELETED);
        if (committer != null) {
            committer.remove(
                    crawlData.getReference(), getNullSafeMetadata(doc));
        }
        getEventManager().fire(CrawlerEvent.create(
                DOCUMENT_COMMITTED_REMOVE, this, crawlData, doc));
    }

    private final class ProcessReferencesRunnable implements Runnable {
        private final ImporterPipelineContext importerContextPrototype;
        private final JobSuite suite;
        private final JobStatusUpdater statusUpdater;
        private final CountDownLatch latch;

        private ProcessReferencesRunnable(
                JobSuite suite,
                JobStatusUpdater statusUpdater,
                CountDownLatch latch,
                ImporterPipelineContext importerContextPrototype) {
            this.suite = suite;
            this.statusUpdater = statusUpdater;
            this.latch = latch;
            this.importerContextPrototype = importerContextPrototype;
        }

        @Override
        public void run() {
            JobSuite.setCurrentJobId(statusUpdater.getJobId());
            try {
                while (!isStopped()) {
                    try {
                        if (!processNextReference(statusUpdater,
                                new ImporterPipelineContext(
                                        importerContextPrototype))) {
                            break;
                        }
                    } catch (Exception e) {
                        LOG.error("{}: "
                            + "An error occured that could compromise "
                            + "the stability of the crawler. Stopping "
                            + "excution to avoid further issues...",
                            getId(), e);
                        stop(suite.getJobStatus(suite.getRootJob()), suite);
                    }
                }
            } catch (Exception e) {
                LOG.error("{}: Problem in thread execution.", getId(), e);
            } finally {
                latch.countDown();
            }
        }
    }

    @Override
    public String toString() {
        return getId();
    }
}
