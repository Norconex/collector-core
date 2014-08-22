/**
 * 
 */
package com.norconex.collector.core.crawler;

import com.norconex.jef4.job.IJob;

/**
 * @author Pascal Essiembre
 *
 */
public interface ICrawler extends IJob {

    /**
     * Gets the crawler configuration
     * @return the crawler configuration
     */
    ICrawlerConfig getCrawlerConfig();

}