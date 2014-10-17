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
package com.norconex.collector.core.data.store.impl.mvstore;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;
import com.norconex.collector.core.data.store.CrawlDataStoreException;
import com.norconex.collector.core.data.store.ICrawlDataStore;

/**
 * H2 MVStore {@link ICrawlDataStore} implementation.
 * @author Pascal Dimassimo
 */
public class MVStoreCrawlDataStore extends AbstractCrawlDataStore {
    
    private final MVStore store;
    
    private final MVMap<String, ICrawlData> mapQueued;
    private final MVMap<String, ICrawlData> mapActive;
    private final MVMap<String, ICrawlData> mapProcessedValid;
    private final MVMap<String, ICrawlData> mapProcessedInvalid;
    private final MVMap<String, ICrawlData> mapCached;
    
    public MVStoreCrawlDataStore(String path, boolean resume) {
        
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            throw new CrawlDataStoreException(
                    "Cannot create crawl data store directory: " + path, e);
        }
        store = MVStore.open(path + "/mvstore");
        
        mapQueued = store.openMap("queued");
        mapActive = store.openMap("active");
        mapProcessedValid = store.openMap("processedValid");
        mapProcessedInvalid = store.openMap("processedInvalid");
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
            mapProcessedInvalid.clear();

            // Valid Processed -> Cached
            for (String key : mapProcessedValid.keySet()) {
                ICrawlData processed = mapProcessedValid.remove(key);
                if (processed.getState().isGoodState()) {
                    mapCached.put(key, processed);
                }
            }
        }
    }

    @Override
    public void queue(ICrawlData crawlData) {
        ICrawlData crawlDataCopy = crawlData.clone();
        mapQueued.put(crawlDataCopy.getReference(), crawlDataCopy);
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
        ICrawlData crawlDataCopy = crawlData.clone();
        String ref = crawlDataCopy.getReference();
        if (crawlDataCopy.getState().isGoodState()) {
            mapProcessedValid.put(ref, crawlDataCopy);
        } else {
            mapProcessedInvalid.put(ref, crawlDataCopy);
        }
        mapActive.remove(ref);
        mapCached.remove(ref);
    }

    @Override
    public boolean isProcessed(String referenceId) {
        return mapProcessedValid.containsKey(referenceId)
                || mapProcessedInvalid.containsKey(referenceId);
    }

    @Override
    public int getProcessedCount() {
        return mapProcessedValid.size() + mapProcessedInvalid.size();
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
