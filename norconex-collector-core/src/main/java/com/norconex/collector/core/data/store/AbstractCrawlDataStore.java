/**
 * 
 */
package com.norconex.collector.core.data.store;

import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;

/**
 * @author Pascal Essiembre
 */
public abstract class AbstractCrawlDataStore implements ICrawlDataStore {

    @Override
    public boolean isVanished(ICrawlData crawlData) {
        ICrawlData cachedReference = getCached(crawlData.getReference());
        if (cachedReference == null) {
            return false;
        }
        CrawlState current = crawlData.getState();
        CrawlState last = cachedReference.getState();
        return !current.isGoodState() && last.isGoodState();
    }


}
