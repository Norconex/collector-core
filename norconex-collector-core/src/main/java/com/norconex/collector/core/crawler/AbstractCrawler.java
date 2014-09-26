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
package com.norconex.collector.core.crawler;

import java.util.Locale;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.commons.lang.time.DurationUtil;
import com.norconex.jef4.job.AbstractResumableJob;
import com.norconex.jef4.status.IJobStatus;
import com.norconex.jef4.status.JobStatusUpdater;
import com.norconex.jef4.suite.JobSuite;

/**
 * @author Pascal Essiembre
 *
 */
public abstract class AbstractCrawler 
        extends AbstractResumableJob implements ICrawler {

    private static final Logger LOG = 
            LogManager.getLogger(AbstractCrawler.class);
    
    private final ICrawlerConfig config;
    
    /**
     * Constructor.
     * @param config crawler configuration
     */
    public AbstractCrawler(ICrawlerConfig config) {
        this.config = config;
    }

    @Override
    public String getId() {
        return config.getId();
    }
    
    /**
     * Gets the crawler configuration
     * @return the crawler configuration
     */
    @Override
    public ICrawlerConfig getCrawlerConfig() {
        return config;
    }

    @Override
    public void stop(IJobStatus status, JobSuite suite) {
    }

    @Override
    protected void startExecution(
            JobStatusUpdater statusUpdater, JobSuite suite) {
        doExecute(statusUpdater, suite, false);
    }

    @Override
    protected void resumeExecution(
            JobStatusUpdater statusUpdater, JobSuite suite) {
        doExecute(statusUpdater, suite, true);
    }

    private void doExecute(JobStatusUpdater statusUpdater,
            JobSuite suite, boolean resume) {
        StopWatch stopWatch = new StopWatch();;
        stopWatch.start();
        ICrawlDataStore refStore = 
                config.getReferenceStoreFactory().createReferenceStore(
                        config, resume);
        try {
            prepareExecution(statusUpdater, suite, refStore, resume);
            execute(statusUpdater, suite, refStore);
        } finally {
            stopWatch.stop();
            LOG.info("Collector executed in " + DurationUtil.formatLong(
                    Locale.ENGLISH, stopWatch.getTime()));
            try {
                cleanupExecution(statusUpdater, suite, refStore);
            } finally {
                refStore.close();
            }
        }
    }
    
    protected abstract void prepareExecution(
            JobStatusUpdater statusUpdater, JobSuite suite, 
            ICrawlDataStore refStore, boolean resume);
    protected abstract void execute(
            JobStatusUpdater statusUpdater,
            JobSuite suite,
            ICrawlDataStore refStore);
    protected abstract void cleanupExecution(
            JobStatusUpdater statusUpdater, JobSuite suite, 
            ICrawlDataStore refStore);
}
