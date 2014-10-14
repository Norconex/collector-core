package com.norconex.collector.core.data.store.impl.mvstore;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;

public class MVStoreCrawlDataStoreFactory implements ICrawlDataStoreFactory {

    private static final long serialVersionUID = 8432072538849428204L;

    @Override
    public ICrawlDataStore createCrawlDataStore(ICrawlerConfig config,
            boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/refstore/" + config.getId() + "/";
        return new MVStoreCrawlDataStore(storeDir, resume);
    }

}
