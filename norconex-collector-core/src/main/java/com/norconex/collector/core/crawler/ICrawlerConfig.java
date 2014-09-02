/**
 * 
 */
package com.norconex.collector.core.crawler;

import java.io.File;

import com.norconex.collector.core.doccrawl.store.IDocCrawlStoreFactory;
import com.norconex.commons.lang.config.IXMLConfigurable;

/**
 * @author Pascal Essiembre
 *
 */
public interface ICrawlerConfig extends IXMLConfigurable {

    /**
     * Gets this crawler unique identifier.
     * @return unique identifier
     */
    String getId();

    File getWorkDir();

    IDocCrawlStoreFactory getReferenceStoreFactory();

    ICrawlerConfig safeClone();
}