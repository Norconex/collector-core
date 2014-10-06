/**
 * 
 */
package com.norconex.collector.core;

import com.norconex.jef4.suite.IJobSuiteFactory;
import com.norconex.jef4.suite.JobSuite;

/**
 * @author Pascal Essiembre
 *
 */
public interface ICollector extends IJobSuiteFactory {

    /**
     * Gets the collector configuration
     * @return the collectorConfig
     */
    ICollectorConfig getCollectorConfig();

    String getId();
    
    JobSuite getJobSuite();

    /**
     * Launched all crawlers defined in configuration.
     * @param resumeNonCompleted whether to resume where previous crawler
     *        aborted (if applicable) 
     */
    void start(boolean resumeNonCompleted);

    /**
     * Stops a running instance of this HTTP Collector.
     */
    void stop();
}