/**
 * 
 */
package com.norconex.collector.core;

import com.norconex.jef4.suite.IJobSuiteFactory;

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
    //    
    //    
    //    @Override
    //    public JobSuite createJobSuite() {
    //        if (abstractCollectorConfig == null) {
    //            try {
    //                abstractCollectorConfig = CollectorConfigLoader.loadCollectorConfig(
    //                        getConfigurationFile(), getVariablesFile());
    //            } catch (Exception e) {
    //                throw new CollectorException(e);
    //            }
    //        }
    //        if (abstractCollectorConfig == null) {
    //        	throw new CollectorException(
    //        			"Configuration file does not exists: "
    //        			+ getConfigurationFile());
    //        }
    //        HttpCrawlerConfig[] configs = abstractCollectorConfig.getCrawlerConfigs();
    //        crawlers = new HttpCrawler[configs.length];
    //        for (int i = 0; i < configs.length; i++) {
    //            HttpCrawlerConfig crawlerConfig = configs[i];
    //            crawlers[i] = new HttpCrawler(crawlerConfig);
    //        }
    //
    //        IJob rootJob = null;
    //        if (crawlers.length > 1) {
    //            rootJob = new AsyncJobGroup(
    //                    abstractCollectorConfig.getId(), crawlers
    //            );
    //        } else if (crawlers.length == 1) {
    //            rootJob = crawlers[0];
    //        }
    //        
    //        JobSuite suite = new JobSuite(
    //                rootJob, 
    //                new JobProgressPropertiesFileSerializer(
    //                        abstractCollectorConfig.getProgressDir()),
    //                new FileLogManager(abstractCollectorConfig.getLogsDir()),
    //                new FileStopRequestHandler(abstractCollectorConfig.getId(), 
    //                        abstractCollectorConfig.getProgressDir()));
    //        LOG.info("Suite of " + crawlers.length + " HTTP crawler jobs created.");
    //        return suite;
    //    }

}