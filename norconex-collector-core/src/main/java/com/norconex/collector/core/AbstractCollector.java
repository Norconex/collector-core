/* Copyright 2014 Norconex Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.committer.core.ICommitter;
import com.norconex.importer.Importer;
import com.norconex.jef4.JEFUtil;
import com.norconex.jef4.job.IJob;
import com.norconex.jef4.job.group.AsyncJobGroup;
import com.norconex.jef4.log.FileLogManager;
import com.norconex.jef4.status.FileJobStatusStore;
import com.norconex.jef4.status.IJobStatus;
import com.norconex.jef4.status.JobState;
import com.norconex.jef4.suite.JobSuite;
import com.norconex.jef4.suite.JobSuiteConfig;
 
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
@SuppressWarnings("nls")
public abstract class AbstractCollector implements ICollector {

    private static final Logger LOG = 
            LogManager.getLogger(AbstractCollector.class);
    
    private AbstractCollectorConfig collectorConfig;

    private ICrawler[] crawlers;
    private JobSuite jobSuite;
    
	/**
	 * Creates and configure a Collector with the provided
	 * configuration.
	 * @param collectorConfig Collector configuration
	 */
    public AbstractCollector(AbstractCollectorConfig collectorConfig) {
        //TODO clone config so modifications no longer apply.
        if (collectorConfig == null) {
            throw new IllegalArgumentException(
                    "Collector Configuration cannot be null.");
        }
        
        this.collectorConfig = collectorConfig;

        ICrawlerConfig[] crawlerConfigs = 
                this.collectorConfig.getCrawlerConfigs();
        if (crawlerConfigs != null) {
            ICrawler[] newCrawlers = new ICrawler[crawlerConfigs.length];
            for (int i = 0; i < crawlerConfigs.length; i++) {
                ICrawlerConfig crawlerConfig = crawlerConfigs[i];
                newCrawlers[i] = createCrawler(crawlerConfig);
            }
            this.crawlers = newCrawlers;
        } else {
            this.crawlers = new ICrawler[]{};
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

        printReleaseVersion();
        
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
        try {
            jobSuite.execute(resumeNonCompleted);
        } finally {
            jobSuite = null;
        }
    }

    /**
     * Stops a running instance of this Collector.
     */
    @Override
    public void stop() {
        if (jobSuite == null) {
            jobSuite = createJobSuite();
        }

        IJobStatus status = jobSuite.getStatus();
        if (status == null 
                || !status.isState(JobState.RUNNING, JobState.UNKNOWN)) {
            throw new CollectorException(
                    "This collector cannot be stopped since it is NOT "
                  + "running. Current state: " 
                  + jobSuite.getStatus().getState());
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Suite state: " + status.getState());        
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
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not stop collector: " + getId(), e);
        }
    }
    
    @Override
    public JobSuite createJobSuite() {
        ICrawler[] crawlers = getCrawlers();
        
        IJob rootJob = null;
        if (crawlers.length > 1) {
            rootJob = new AsyncJobGroup(
                    getId(), crawlers
            );
        } else if (crawlers.length == 1) {
            rootJob = crawlers[0];
        }
        
        JobSuiteConfig suiteConfig = new JobSuiteConfig();

        
        //TODO have a base workdir, which is used to figure out where to put
        // everything (log, progress), and make log and progress overwritable.

        ICollectorConfig collectorConfig = getCollectorConfig();
        suiteConfig.setLogManager(
                new FileLogManager(collectorConfig.getLogsDir()));
        suiteConfig.setJobStatusStore(
                new FileJobStatusStore(collectorConfig.getProgressDir()));
        suiteConfig.setWorkdir(collectorConfig.getProgressDir()); 
        JobSuite suite = new JobSuite(rootJob, suiteConfig);
        LOG.info("Suite of " + crawlers.length + " crawler jobs created.");
        return suite;
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
    public AbstractCollectorConfig getCollectorConfig() {
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
    public void setCrawlers(ICrawler[] crawlers) {
        this.crawlers = Arrays.copyOf(crawlers, crawlers.length);
    }
    /**
     * Gets all crawler instances in this collector.
     * @return crawlers
     */
    public ICrawler[] getCrawlers() {
        return Arrays.copyOf(crawlers, crawlers.length);
    }
    
    private void printReleaseVersion() {
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
            LOG.info("Version: \"" + moduleName
                    + "\" version cannot be established. "
                    + "This is likely due to using an unpacked or modified "
                    + "jar, or the jar not being packaged with version "
                    + "information.");
            return;
        }
        LOG.info("Version: " + p.getImplementationTitle() + " " 
                + p.getImplementationVersion()
                + " (" + p.getImplementationVendor() + ")");
    }

}
