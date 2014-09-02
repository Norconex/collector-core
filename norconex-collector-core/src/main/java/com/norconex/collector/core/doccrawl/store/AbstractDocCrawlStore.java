/**
 * 
 */
package com.norconex.collector.core.doccrawl.store;

import com.norconex.collector.core.doccrawl.IDocCrawl;
import com.norconex.collector.core.doccrawl.DocCrawlState;

/**
 * @author Pascal Essiembre
 */
public abstract class AbstractDocCrawlStore implements IDocCrawlStore {

    @Override
    public boolean isVanished(IDocCrawl docCrawl) {
        IDocCrawl cachedReference = getCached(docCrawl.getReference());
        if (cachedReference == null) {
            return false;
        }
        DocCrawlState current = docCrawl.getState();
        DocCrawlState last = cachedReference.getState();
        return !current.isGoodState() && last.isGoodState();
    }


}
