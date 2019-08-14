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

import static com.norconex.collector.core.CollectorEvent.COLLECTOR_ENDED;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STARTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.VersionUtil;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.importer.Importer;
import com.norconex.jef5.job.IJob;
import com.norconex.jef5.job.group.AsyncJobGroup;
import com.norconex.jef5.shutdown.ShutdownException;
import com.norconex.jef5.status.JobState;
import com.norconex.jef5.status.JobStatus;
import com.norconex.jef5.status.JobSuiteStatus;
import com.norconex.jef5.suite.JobSuite;
import com.norconex.jef5.suite.JobSuiteConfig;

/**
 * Base implementation of a Collector.
 * Instances of this class can hold several crawler, running at once.
 * This is convenient when there are configuration setting to be shared amongst
 * crawlers.  When you have many crawler jobs defined that have nothing
 * in common, it may be best to configure and run them separately, to facilitate
 * troubleshooting.  There is no best rule for this, experimentation
 * will help you.
 * @author Pascal Essiembre
 */
public abstract class Collector {

    private static final Logger LOG =
            LoggerFactory.getLogger(Collector.class);

    private static final String NORCONEX =
            "  _   _  ___  ____   ____ ___  _   _ _______  __%n"
          + " | \\ | |/ _ \\|  _ \\ / ___/ _ \\| \\ | | ____\\ \\/ /%n"
          + " |  \\| | | | | |_) | |  | | | |  \\| |  _|  \\  / %n"
          + " | |\\  | |_| |  _ <| |__| |_| | |\\  | |___ /  \\ %n"
          + " |_| \\_|\\___/|_| \\_\\\\____\\___/|_| \\_|_____/_/\\_\\%n%n"
          //+ " -----------[ HTTP COLLECTOR 3.0.0 ]------------%n%n";
          + " %s%n%n";

    private final CollectorConfig collectorConfig;

    private final List<Crawler> crawlers = new ArrayList<>();
    private JobSuite jobSuite;

    private final EventManager eventManager;

    private static final InheritableThreadLocal<Collector> INSTANCE =
            new InheritableThreadLocal<>();

    private CachedStreamFactory streamFactory;
    private Path workDir;
    private Path tempDir;

    /**
     * Creates and configure a Collector with the provided
     * configuration.
     * @param collectorConfig Collector configuration
     */
    public Collector(CollectorConfig collectorConfig) {
        this(collectorConfig, null);
    }

	/**
	 * Creates and configure a Collector with the provided
	 * configuration.
	 * @param collectorConfig Collector configuration
	 * @param eventManager event manager
	 */
    public Collector(
            CollectorConfig collectorConfig, EventManager eventManager) {

        //TODO have an init method instead?  Or make it implement
        // IEvent listener and listen for start?

        //TODO clone config so modifications no longer apply.
        Objects.requireNonNull(
                collectorConfig, "'collectorConfig' must not be null.");
//        Objects.requireNonNull(
//                eventManager, "'eventManager' must not be null.");

        this.collectorConfig = collectorConfig;
        this.eventManager = new EventManager(eventManager);

        INSTANCE.set(this);
    }

    public static Collector get() {
        return INSTANCE.get();
    }

    //TODO Should we deprecate this since IJobSuiteFactory has createJobSuite
    //which can be overwritten if need be, instead of exposing it?
    /**
     * Gets the job suite or <code>null</code> if the the collector
     * was not yet started or is no longer running.
     * @return JobSuite
     * @deprecated Since 2.0.0
     */
    @Deprecated
    //TODO try to deprecate
    public JobSuite getJobSuite() {
        return jobSuite;
    }

    public Path getWorkDir() {
        if (workDir != null) {
            return workDir;
        }

        String fileSafeId = FileUtil.toSafeFileName(getId());
        Path dir = ObjectUtils.defaultIfNull(collectorConfig.getWorkDir(),
                CollectorConfig.DEFAULT_WORK_DIR).resolve(fileSafeId);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create collector working directory: " + dir, e);
        }
        workDir = dir;
        return workDir;
    }
    public Path getTempDir() {
        if (tempDir != null) {
            return tempDir;
        }

        Path dir;
        if (collectorConfig.getTempDir() == null) {
            dir = getWorkDir().resolve("temp");
        } else {
            String fileSafeId = FileUtil.toSafeFileName(getId());
            dir = collectorConfig.getTempDir().resolve(fileSafeId);
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create collector temp directory: " + dir, e);
        }
        tempDir = dir;
        return tempDir;
    }

    /**
     * Starts all crawlers defined in configuration.
     * @param resumeNonCompleted whether to resume where previous crawler
     *        aborted (if applicable)
     */
    public void start(boolean resumeNonCompleted) {
        try {
            initCollector();
            eventManager.fire(CollectorEvent.create(COLLECTOR_STARTED, this));
            jobSuite.execute(resumeNonCompleted);
        } finally {
            try {
                cleanupCollector();
            } finally {
                eventManager.fire(CollectorEvent.create(COLLECTOR_ENDED, this));
            }
            eventManager.clearListeners();
        }
    }

    protected void initCollector() {

        // Ensure clean state
        tempDir = null;
        workDir = null;
        jobSuite = null;
        //TODO listeners are removed after everything and not kept
        // because we support adding them after config is added.  Shall we???
//        eventManager.clearListeners();
        crawlers.clear();

        // recreate everything
        createCrawlers();

        //--- Register event listeners ---
        eventManager.addListenersFromScan(this.collectorConfig);

//        eventManager.getListeners().forEach(
//                l -> LOG.debug("Event listener: {}", l.getClass()));
//        System.out.println("DDDDDDDD listeners: ");
//        eventManager.getListeners().forEach(
//                l -> System.out.println("   class: " + l.getClass()));


        //TODO move this code to a config validator class?
        //--- Ensure good state/config ---
        if (StringUtils.isBlank(collectorConfig.getId())) {
            throw new CollectorException("Collector must be given "
                    + "a unique identifier (id).");
        }


//TODO **************************
//        // PREVENT RERUNNING A COLLECTOR INSTANCE MORE THAN ONCE
//        // HAVE A CLONE OR EQUVALENT METHOD THAT COULD BE CALLED
//        // ONLY WHEN A JOB IS DONE RUNNING.
//        // THEN WE DO NOT HAVE TO CHECK IF THIS jvm INTANCE IS ALREAY RUNNING
//        // OR NOT AND ENSURE ALL RESOURCES ARE DETROYED
//
//        if (jobSuite != null && jobSuite.getRootStatus().isRunning()) {
//            LOG.debug("Collector currently running. Waiting a bit in case it's "
//                    + "shutting down.");
//            Sleeper.sleepMillis(JobHeartbeatGenerator.HEARTBEAT_INTERVAL * 2);
//            if (jobSuite.getRootStatus().isRunning()) {
//                throw new CollectorException(
//                        "Collector is already running. Wait for it to complete "
//                      + "before starting the same instance again, or stop "
//                      + "the currently running instance first.");
//            }
//            System.out.println("DDDDDDD state: " + jobSuite.getRootStatus().getState());
//
//            System.out.println("DDDDDDD running? " + jobSuite.getRootStatus().isRunning());
//        }




//        if (jobSuite != null) {
//            throw new CollectorException(
//                    "Collector is already running. Wait for it to complete "
//                  + "before starting the same instance again, or stop "
//                  + "the currently running instance first.");
//        }



        // init order is important

//        //--- Directories ---
//        this.workDir = createWorkDir();
//        this.tempDir = createTempDir();
//        String fileSafeId = FileUtil.toSafeFileName(getId());
//        workDir = ObjectUtils.defaultIfNull(
//                collectorConfig.getWorkDir(),
//                CollectorConfig.DEFAULT_WORK_DIR).resolve(fileSafeId);
//        if (collectorConfig.getTempDir() == null) {
//            tempDir = workDir.resolve("temp");
//        } else {
//            tempDir = collectorConfig.getTempDir().resolve(fileSafeId);
//        }
//        try {
//            Files.createDirectories(workDir);
//            Files.createDirectories(tempDir);
//        } catch (IOException e) {
//            throw new CollectorException(
//                    "Could not create collector directory.", e);
//        }

        //--- Stream Cache Factory ---
        streamFactory = new CachedStreamFactory(
                collectorConfig.getMaxMemoryPool(),
                collectorConfig.getMaxMemoryInstance(),
                getTempDir());
//                collectorConfig.getTempDir().resolve(
//                        FileUtil.toSafeFileName(getId())));

        //--- JEF Job Suite ---
        jobSuite = createJobSuite();

//        //--- Register event listeners ---
//        eventManager.addListenersFromScan(this.collectorConfig);

        //--- Print release versions ---
//        printReleaseVersions();
    }

//    // will be called any time before startup. After, only called once
//    private Path createWorkDir() {
//        String fileSafeId = FileUtil.toSafeFileName(getId());
//        Path dir = ObjectUtils.defaultIfNull(
//                collectorConfig.getWorkDir(),
//                CollectorConfig.DEFAULT_WORK_DIR).resolve(fileSafeId);
//        try {
//            Files.createDirectories(dir);
//        } catch (IOException e) {
//            throw new CollectorException(
//                    "Could not create collector working directory: " + dir, e);
//        }
//        return dir;
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
//                    "Could not create collector temp directory: " + dir, e);
//        }
//        return dir;
//    }

    protected void cleanupCollector() {
        //TODO do not make jobSuite null so we can grab status?
        // Or store latest status?
        //jobSuite = null;
        //workDir = null;

        try {
            FileUtil.delete(getTempDir().toFile());
        } catch (IOException e) {
            throw new CollectorException("Could not delete temp directory", e);
        } finally {
            //tempDir = null;
        }
    }

    /**
     * Gets the state of this collector. If the collector is not running,
     * {@link JobState#UNKNOWN} is returned.
     * @return execution state
     * @since 1.10.0
     * @deprecated Since 2.0.0
     */
    //TODO try to deprecate (do not rely on JEF???)
    @Deprecated
    public JobState getState() {
        JobSuite suite = getJobSuite();
        if (suite != null) {
            JobStatus status = suite.getRootStatus();
            if (status != null) {
                return status.getState();
            }
        }
        return JobState.UNKNOWN;
    }

    /**
     * Stops a running instance of this Collector. The caller can be a
     * different JVM instance than the instance we want to stop.
     */
    public void stop() {
        // It is currently necessary to create the crawlers from config
        // so that the suite is created properly and jobs stopped properly.
        createCrawlers();

        if (jobSuite == null) {
            jobSuite = createJobSuite();
            //jobSuite = snap
        }
        JobSuiteStatus suiteStatus = null;
        try {
            suiteStatus = JobSuiteStatus.getInstance(jobSuite);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not obtain job status of: " + getId(), e);
        }

//        JobStatus status = jobSuite.getRootStatus();
        JobStatus status = suiteStatus.getRootStatus();

        if (status == null
                || !status.isState(JobState.RUNNING, JobState.UNKNOWN)) {
            String curState =
                    (status != null ? " State: " + status.getState() : "");
            throw new CollectorException(
                    "This collector cannot be stopped since it is NOT "
                  + "running." + curState);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Suite state: {}", status.getState());
        }

        try {
            LOG.info("Making a stop request...");
            jobSuite.stop();
            LOG.info("Stop request made.");
            LOG.info("PLEASE NOTE: To ensure a clean stop, "
                    + "crawlers may wait until they are done with documents "
                    + "currently being processed. If an urgent stop is "
                    + "required or you do not want to wait, manually kill "
                    + "the process.");
            //TODO wait for stop confirmation before setting to null?
            jobSuite = null;
        } catch (ShutdownException e) {
            throw new CollectorException(
                    "Could not stop collector: " + getId(), e);
        }
    }

//    @Override
    public JobSuite createJobSuite() {
        CollectorConfig collConfig = getCollectorConfig();

        List<Crawler> crawlerList = getCrawlers();

        IJob rootJob = null;
        if (crawlerList.size() > 1) {
            int maxCrawlers = crawlerList.size();
            if (collConfig.getMaxParallelCrawlers() > 0) {
                maxCrawlers = Math.min(
                        maxCrawlers, collConfig.getMaxParallelCrawlers());
            }
            rootJob = new AsyncJobGroup(getId(), maxCrawlers, crawlerList);
        } else if (crawlerList.size() == 1) {
            rootJob = crawlerList.get(0);
        }

        JobSuiteConfig suiteConfig = new JobSuiteConfig();

        //TODO have a base workdir, which is used to figure out where to put
        // everything (log, progress), and make log and progress overwritable.

//        suiteConfig.setLogManager(new FileLogManager(collConfig.getLogsDir()));
//        suiteConfig.setJobStatusStore(
//                new FileJobStatusStore(collConfig.getProgressDir()));
        suiteConfig.setWorkdir(collConfig.getWorkDir());

        // Add JEF listeners
//        if (collConfig.getJobLifeCycleListeners() != null) {
//            suiteConfig.setJobLifeCycleListeners(
//                    collConfig.getJobLifeCycleListeners());
//        }
//        if (collConfig.getJobErrorListeners() != null) {
//            suiteConfig.setJobErrorListeners(collConfig.getJobErrorListeners());
//        }
//        List<ISuiteLifeCycleListener> suiteListeners = new ArrayList<>();
//        suiteListeners.add(new AbstractSuiteLifeCycleListener() {
//            @Override
//            public void suiteStarted(JobSuite suite) {
//                printReleaseVersion();
//            }
//        });
//        if (collConfig.getSuiteLifeCycleListeners() != null) {
//            suiteListeners.addAll(Arrays.asList(
//                    collConfig.getSuiteLifeCycleListeners()));
//        }
//        suiteConfig.setSuiteLifeCycleListeners(
//                suiteListeners.toArray(new ISuiteLifeCycleListener[]{}));

        JobSuite suite = new JobSuite(rootJob, suiteConfig);
        LOG.info("Collector with {} crawler(s) created.", crawlerList.size());
        return suite;
    }

    /**
     * Gets the event manager.
     * @return event manager
     * @since 2.0.0
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Loads all crawlers from configuration.
     */
    private void createCrawlers() {
        if (getCrawlers().isEmpty()) {
            List<CrawlerConfig> crawlerConfigs =
                    this.collectorConfig.getCrawlerConfigs();
            if (crawlerConfigs != null) {
                for (CrawlerConfig crawlerConfig : crawlerConfigs) {
                    crawlers.add(createCrawler(crawlerConfig));
                }
            }
        } else {
            LOG.debug("Crawlers already created.");
        }
    }


    /**
     * Creates a new crawler instance.
     * @param config crawler configuration
     * @return new crawler
     */
    protected abstract Crawler createCrawler(CrawlerConfig config);

    //TODO Since 3.0.0
    public CachedStreamFactory getStreamFactory() {
        return streamFactory;
    }

    /**
     * Gets the collector configuration.
     * @return collector configuration
     */
    public CollectorConfig getCollectorConfig() {
        return collectorConfig;
    }

    /**
     * Gets the collector unique identifier.
     * @return collector unique identifier
     */
    public String getId() {
        return collectorConfig.getId();
    }

    //TODO remove for good? Always (re)create from config if we want to
    // restart cleanly.
//    /**
//     * Set the provided crawlers on this collector (overwriting any existing).
//     * @param crawlers crawlers to set
//     */
//    public void setCrawlers(List<Crawler> crawlers) {
//        CollectionUtil.setAll(this.crawlers, crawlers);
//        collectorConfig.setCrawlerConfigs(this.crawlers.stream().map(
//                crawler -> crawler.getCrawlerConfig()).collect(
//                        Collectors.toList()));
//    }
    /**
     * Gets all crawler instances in this collector.
     * @return crawlers
     */
    public List<Crawler> getCrawlers() {
        return Collections.unmodifiableList(crawlers);
    }

    public String getVersion() {
        return VersionUtil.getVersion(getClass(), "Undefined");
    }
    public List<String> getReleaseVersions() {
        List<String> versions = new ArrayList<>();
        versions.add(releaseVersion("Collector", getClass()));
        versions.add(releaseVersion("Collector Core", Collector.class));
        versions.add(releaseVersion("Importer", Importer.class));
        versions.add(releaseVersion("JEF", IJob.class));
        versions.add(releaseVersion("Committer Core", ICommitter.class));
        for (CrawlerConfig crawler : getCollectorConfig().getCrawlerConfigs()) {
            ICommitter c = crawler.getCommitter();
            if (c != null && !c.getClass().getPackage().getName().startsWith(
                    "com.norconex.committer.core")) {
                versions.add(releaseVersion("Committer "
                        + c.getClass().getSimpleName(), c.getClass()));
            }
        }
        return versions;
    }
    private String releaseVersion(String moduleName, Class<?> cls) {
        return StringUtils.rightPad(moduleName + ": ", 20, ' ')
                + VersionUtil.getDetailedVersion(cls, "undefined");
    }


//TODO delete below when replaced by above
//    private void printReleaseVersions() {
//        printReleaseVersion("Collector", getClass().getPackage());
//        printReleaseVersion("Collector Core",
//                Collector.class.getPackage());
//        printReleaseVersion("Importer", Importer.class.getPackage());
//        printReleaseVersion("JEF", IJob.class.getPackage());
//
//        //--- Committers ---
//        printReleaseVersion("Committer Core", ICommitter.class.getPackage());
//        Set<ICommitter> committers = new HashSet<>();
//        for (Crawler crawler : getCrawlers()) {
//            ICommitter committer = crawler.getCrawlerConfig().getCommitter();
//            if (committer != null) {
//                Package committerPackage = committer.getClass().getPackage();
//                if (committerPackage != null
//                        && !committerPackage.getName().startsWith(
//                                "com.norconex.committer.core")) {
//                    committers.add(committer);
//                }
//            }
//        }
//        for (ICommitter c : committers) {
//            printReleaseVersion(
//                    c.getClass().getSimpleName(), c.getClass().getPackage());
//        }
//    }
//    private void printReleaseVersion(String moduleName, Package p) {
//        //TODO grab from pom.xml if blank, in case running from unpackaged
//        String version = p.getImplementationVersion();
//        if (StringUtils.isBlank(version)) {
//            // No version is likely due to using an unpacked or modified
//            // jar, or the jar not being packaged with version
//            // information.
//            LOG.info("Version: \"" + moduleName
//                    + "\" version is undefined.");
//            return;
//        }
//        LOG.info("Version: " + p.getImplementationTitle() + " "
//                + p.getImplementationVersion()
//                + " (" + p.getImplementationVendor() + ")");
//    }

    @Override
    public String toString() {
        return getId();
    }
}
