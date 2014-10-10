/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.jef4.job.IJob;
import com.norconex.jef4.job.group.AsyncJobGroup;
import com.norconex.jef4.log.FileLogManager;
import com.norconex.jef4.status.FileJobStatusStore;
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
                    "Collector Configugation cannot be null.");
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
        
        //TODO move this code to a config validator class?
        //TODO move this code to base class?
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
            throw new CollectorException(
                    "This collector cannot be stopped since it is NOT "
                  + "running.");
        }
        try {
            jobSuite.stop();
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
}
