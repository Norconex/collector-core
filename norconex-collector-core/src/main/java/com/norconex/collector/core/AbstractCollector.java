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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.importer.Importer;
import com.norconex.jef5.job.IJob;
import com.norconex.jef5.job.group.AsyncJobGroup;
import com.norconex.jef5.shutdown.ShutdownException;
import com.norconex.jef5.status.JobState;
import com.norconex.jef5.status.JobStatus;
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
public abstract class AbstractCollector implements ICollector {

    private static final Logger LOG =
            LoggerFactory.getLogger(AbstractCollector.class);

    private final ICollectorConfig collectorConfig;

    private final List<ICrawler> crawlers = new ArrayList<>();
    private JobSuite jobSuite;

    private final EventManager eventManager;


    /**
     * Creates and configure a Collector with the provided
     * configuration.
     * @param collectorConfig Collector configuration
     */
    public AbstractCollector(ICollectorConfig collectorConfig) {
        this(collectorConfig, new EventManager());
    }

	/**
	 * Creates and configure a Collector with the provided
	 * configuration.
	 * @param collectorConfig Collector configuration
	 * @param eventManager event manager
	 */
    public AbstractCollector(
            ICollectorConfig collectorConfig, EventManager eventManager) {
        //TODO have an init method instead?  Or make it implement
        // IEvent listener and listen for start?

        //TODO clone config so modifications no longer apply.
        Objects.requireNonNull(
                collectorConfig, "collectorConfig cannot be null.");
        Objects.requireNonNull(eventManager, "eventManager cannot be null.");

        this.collectorConfig = collectorConfig;
        this.eventManager = eventManager;

        List<ICrawlerConfig> crawlerConfigs =
                this.collectorConfig.getCrawlerConfigs();
        if (crawlerConfigs != null) {
            for (ICrawlerConfig crawlerConfig : crawlerConfigs) {
                crawlers.add(createCrawler(crawlerConfig));
            }
        }
    }

    /**
     * Gets the job suite.
     * @return the jobSuite
     */
    @Override
    public JobSuite getJobSuite() {
        return jobSuite;
    }

    /**
     * Start all crawlers defined in configuration.
     * @param resumeNonCompleted whether to resume where previous crawler
     *        aborted (if applicable)
     */
    @Override
    public void start(boolean resumeNonCompleted) {

        //TODO move this code to a config validator class?
        if (StringUtils.isBlank(getCollectorConfig().getId())) {
            throw new CollectorException("Collector must be given "
                    + "a unique identifier (id).");
        }

        if (jobSuite != null) {
            throw new CollectorException(
                    "Collector is already running. Wait for it to complete "
                  + "before starting the same instance again, or stop "
                  + "the currently running instance first.");
        }
        jobSuite = createJobSuite();

        eventManager.addListenersFromScan(this.collectorConfig);

        printReleaseVersions();

//        List<ICollectorLifeCycleListener> listeners =
//                collectorConfig.getCollectorListeners();
        try {
            eventManager.fire(CollectorEvent.create(COLLECTOR_STARTED, this));
//            if (CollectionUtils.isNotEmpty(listeners)) {
//                for (ICollectorLifeCycleListener l : listeners) {
//                    l.onCollectorStart(this);
//                }
//            }
            jobSuite.execute(resumeNonCompleted);
        } finally {
//            if (CollectionUtils.isNotEmpty(listeners)) {
//                for (ICollectorLifeCycleListener l : listeners) {
//                    l.onCollectorFinish(this);
//                }
//            }
            eventManager.fire(CollectorEvent.create(COLLECTOR_ENDED, this));

            jobSuite = null;
        }
    }

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
     * Stops a running instance of this Collector.
     */
    @Override
    public void stop() {
        if (jobSuite == null) {
            jobSuite = createJobSuite();
        }

        JobStatus status = jobSuite.getRootStatus();
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
        List<ICrawler> crawlerList = getCrawlers();

        IJob rootJob = null;
        if (crawlerList.size() > 1) {
            rootJob = new AsyncJobGroup(getId(), crawlerList);
        } else if (crawlerList.size() == 1) {
            rootJob = crawlerList.get(0);
        }

        JobSuiteConfig suiteConfig = new JobSuiteConfig();

        //TODO have a base workdir, which is used to figure out where to put
        // everything (log, progress), and make log and progress overwritable.

        ICollectorConfig collConfig = getCollectorConfig();
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
     * Creates a new crawler instance.
     * @param config crawler configuration
     * @return new crawler
     */
    protected abstract ICrawler createCrawler(ICrawlerConfig config);

    /**
     * Gets the collector configuration
     * @return the collectorConfig
     */
    @Override
    public ICollectorConfig getCollectorConfig() {
        return collectorConfig;
    }

    @Override
    public String getId() {
        return collectorConfig.getId();
    }

    /**
     * Add the provided crawlers to this collector.
     * @param crawlers crawlers to add
     */
    public void setCrawlers(List<ICrawler> crawlers) {
        CollectionUtil.setAll(this.crawlers, crawlers);
    }
    /**
     * Gets all crawler instances in this collector.
     * @return crawlers
     */
    public List<ICrawler> getCrawlers() {
        return Collections.unmodifiableList(crawlers);
    }

    private void printReleaseVersions() {
        printReleaseVersion("Collector", getClass().getPackage());
        printReleaseVersion("Collector Core",
                AbstractCollector.class.getPackage());
        printReleaseVersion("Importer", Importer.class.getPackage());
        printReleaseVersion("JEF", IJob.class.getPackage());

        //--- Committers ---
        printReleaseVersion("Committer Core", ICommitter.class.getPackage());
        Set<ICommitter> committers = new HashSet<>();
        for (ICrawler crawler : getCrawlers()) {
            ICommitter committer = crawler.getCrawlerConfig().getCommitter();
            if (committer != null) {
                Package committerPackage = committer.getClass().getPackage();
                if (committerPackage != null
                        && !committerPackage.getName().startsWith(
                                "com.norconex.committer.core")) {
                    committers.add(committer);
                }
            }
        }
        for (ICommitter c : committers) {
            printReleaseVersion(
                    c.getClass().getSimpleName(), c.getClass().getPackage());
        }
    }
    private void printReleaseVersion(String moduleName, Package p) {
        String version = p.getImplementationVersion();
        if (StringUtils.isBlank(version)) {
            // No version is likely due to using an unpacked or modified
            // jar, or the jar not being packaged with version
            // information.
            LOG.info("Version: \"" + moduleName
                    + "\" version is undefined.");
            return;
        }
        LOG.info("Version: " + p.getImplementationTitle() + " "
                + p.getImplementationVersion()
                + " (" + p.getImplementationVendor() + ")");
    }

    @Override
    public String toString() {
        return getId();
    }
}
