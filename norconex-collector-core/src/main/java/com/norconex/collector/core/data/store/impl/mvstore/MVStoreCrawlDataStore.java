package com.norconex.collector.core.data.store.impl.mvstore;

import java.io.File;
import java.util.Iterator;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;

public class MVStoreCrawlDataStore extends AbstractCrawlDataStore {
    
    private final MVStore store;
    
    private final MVMap<String, ICrawlData> mapQueued;
    private final MVMap<String, ICrawlData> mapActive;
    private final MVMap<String, ICrawlData> mapProcessed;
    private final MVMap<String, ICrawlData> mapCached;
    
    public MVStoreCrawlDataStore(String path, boolean resume) {
        
        new File(path).mkdirs();
        store = MVStore.open(path + "/mvstore");
        
        mapQueued = store.openMap("queued");
        mapActive = store.openMap("active");
        mapProcessed = store.openMap("processed");
        mapCached = store.openMap("cached");
        
        if (resume) {
            
            // Active -> Queued
            for (String key : mapActive.keySet()) {
                mapQueued.put(key, mapActive.remove(key));
            }
            
        } else {
            
            mapCached.clear();
            mapActive.clear();
            mapQueued.clear();
            
            // Valid Processed -> Cached
            for (String key : mapProcessed.keySet()) {
                ICrawlData processed = mapProcessed.remove(key);
                if (processed.getState().isGoodState()) {
                    mapCached.put(key, processed);
                }
            }
        }
    }

    @Override
    public void queue(ICrawlData crawlData) {
        mapQueued.put(crawlData.getReference(), crawlData);
    }

    @Override
    public boolean isQueueEmpty() {
        return mapQueued.isEmpty();
    }

    @Override
    public int getQueueSize() {
        return mapQueued.size();
    }

    @Override
    public boolean isQueued(String reference) {
        return mapQueued.containsKey(reference);
    }

    @Override
    public synchronized ICrawlData nextQueued() {
        String key = mapQueued.firstKey();
        if (key == null) {
            return null;
        }
        ICrawlData data = mapQueued.remove(key);
        mapActive.put(key, data);
        return data;
    }

    @Override
    public boolean isActive(String reference) {
        return mapActive.containsKey(reference);
    }

    @Override
    public int getActiveCount() {
        return mapActive.size();
    }

    @Override
    public ICrawlData getCached(String cacheReference) {
        return mapCached.get(cacheReference);
    }

    @Override
    public boolean isCacheEmpty() {
        return mapCached.isEmpty();
    }

    @Override
    public synchronized void processed(ICrawlData crawlData) {
        mapActive.remove(crawlData.getReference());
        mapCached.remove(crawlData.getReference());
        mapProcessed.put(crawlData.getReference(), crawlData);
    }

    @Override
    public boolean isProcessed(String referenceId) {
        return mapProcessed.containsKey(referenceId);
    }

    @Override
    public int getProcessedCount() {
        return mapProcessed.size();
    }

    @Override
    public Iterator<ICrawlData> getCacheIterator() {
        return mapCached.values().iterator();
    }

    @Override
    public void close() {
        store.close();
    }
}
