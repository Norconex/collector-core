/* Copyright 2014 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.collector.core.data.store.impl.mapdb;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;
import com.norconex.collector.core.data.store.CrawlDataStoreException;
import com.norconex.collector.core.data.store.ICrawlDataStore;

/**
 * MapDB implementation of {@link ICrawlDataStore}.
 * @author Pascal Essiembre
 */
public class MapDBCrawlDataStore extends AbstractCrawlDataStore {

    private static final Logger LOG = 
            LogManager.getLogger(MapDBCrawlDataStore.class);

    //TODO make configurable
    private static final int COMMIT_SIZE = 1000;

    private static final String STORE_QUEUE = "queue";
    private static final String STORE_ACTIVE = "active";
    private static final String STORE_CACHE = "cache";
    private static final String STORE_PROCESSED_VALID = "valid";
    private static final String STORE_PROCESSED_INVALID = "invalid";
    
    private final String path;
    private final DB db;
    private Queue<ICrawlData> queue;
    private Map<String, ICrawlData> active;
    private Map<String, ICrawlData> cache;
    private Map<String, ICrawlData> processedValid;
    private Map<String, ICrawlData> processedInvalid;
    
    private long commitCounter;
    
    private final Serializer<ICrawlData> valueSerializer;
    
    
    public MapDBCrawlDataStore(String path, boolean resume) {
        this(path, resume, null);
    }
    public MapDBCrawlDataStore(String path, boolean resume,
            Serializer<ICrawlData> valueSerializer) {
        super();
        
        this.valueSerializer = valueSerializer;
        this.path = path;

        LOG.info("Initializing reference store " + path);
        
        try {
            FileUtils.forceMkdir(new File(path));
        } catch (IOException e) {
            throw new CrawlDataStoreException(
                    "Cannot create crawl data store directory: " + path, e);
        }
        File dbFile = new File(path + "/mapdb");
        boolean create = !dbFile.exists() || !dbFile.isFile();
        
        // Configure and open database
        this.db = createDB(dbFile);
    
        initDB(create);
        if (resume) {
            LOG.debug(path
                    + " Resuming: putting active URLs back in the queue...");
            for (ICrawlData crawlData : active.values()) {
                queue.add(crawlData);
            }
            LOG.debug(path + ": Cleaning active database...");
            active.clear();
        } else if (!create) {
            LOG.debug(path + ": Cleaning queue database...");
            queue.clear();
            LOG.debug(path + ": Cleaning active database...");
            active.clear();
            LOG.debug(path + ": Cleaning invalid URLs database...");
            processedInvalid.clear();
            LOG.debug(path + ": Cleaning cache database...");
            db.delete(STORE_CACHE);
            LOG.debug(path 
                    + ": Caching valid URLs from last run (if applicable)...");
            db.rename(STORE_PROCESSED_VALID, STORE_CACHE);
            cache = processedValid;
            processedValid = db.createHashMap(
                    STORE_PROCESSED_VALID).counterEnable().make();
            db.commit();
        } else {
            LOG.debug(path + ": New databases created.");
        }
        LOG.info(path + ": Done initializing databases.");
    }
    
    private DB createDB(File dbFile) {
        return DBMaker.newFileDB(dbFile)
                .asyncWriteEnable()
                .cacheSoftRefEnable()
                .closeOnJvmShutdown()
                .make();
    }
    
    private void initDB(boolean create) {
        queue = new MappedQueue<>(db, STORE_QUEUE, create);
        if (create) {
            active = db.createHashMap(STORE_ACTIVE)
                    .valueSerializer(valueSerializer).counterEnable().make();
            cache = db.createHashMap(STORE_CACHE)
                    .valueSerializer(valueSerializer).counterEnable().make();
            processedValid = db.createHashMap(
                    STORE_PROCESSED_VALID)
                    .valueSerializer(valueSerializer).counterEnable().make();
            processedInvalid = db.createHashMap(
                    STORE_PROCESSED_INVALID)
                    .valueSerializer(valueSerializer).counterEnable().make();
        } else {
            active = db.getHashMap(STORE_ACTIVE);
            cache = db.getHashMap(STORE_CACHE);
            processedValid = db.getHashMap(STORE_PROCESSED_VALID);
            processedInvalid = db.getHashMap(STORE_PROCESSED_INVALID);
        }
    }
    
    @Override
    public void queue(ICrawlData crawlData) {
        // Short of being immutable, make a defensive copy of crawl URL.
        //TODO why again?
        ICrawlData crawlDataCopy = crawlData.clone();
        queue.add(crawlDataCopy);
    }

    @Override
    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    @Override
    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public boolean isQueued(String reference) {
        return queue.contains(reference);
    }

    @Override
    public synchronized ICrawlData nextQueued() {
        ICrawlData crawlData = (ICrawlData) queue.poll();
        if (crawlData != null) {
            active.put(crawlData.getReference(), crawlData);
        }
        return crawlData;
    }

    @Override
    public boolean isActive(String reference) {
        return active.containsKey(reference);
    }

    @Override
    public int getActiveCount() {
        return active.size();
    }

    @Override
    public ICrawlData getCached(String cacheReference) {
        return cache.get(cacheReference);
    }

    @Override
    public boolean isCacheEmpty() {
        return cache.isEmpty();
    }

    @Override
    public synchronized void processed(ICrawlData crawlData) {
        // Short of being immutable, make a defensive copy of crawl URL.

        //TODO why clone here if we are only readonly?
        ICrawlData referenceCopy = crawlData.clone();
        if (referenceCopy.getState().isGoodState()) {
            processedValid.put(referenceCopy.getReference(), referenceCopy);
        } else {
            processedInvalid.put(referenceCopy.getReference(), referenceCopy);
        }
        if (!active.isEmpty()) {
            active.remove(referenceCopy.getReference());
        }
        if (!cache.isEmpty()) {
            cache.remove(referenceCopy.getReference());
        }
        commitCounter++;
        if (commitCounter % COMMIT_SIZE == 0) {
            LOG.debug("Committing reference store to disk: " + path);
            db.commit();
        }
        //TODO Compact database and LOG the event once MapDB fixed issue #160
        //TODO call db.compact(); when commit counter modulus commit size
        //     10 is encountered.
    }

    @Override
    public boolean isProcessed(String url) {
        return processedValid.containsKey(url)
                || processedInvalid.containsKey(url);
    }

    @Override
    public int getProcessedCount() {
        return processedValid.size() + processedInvalid.size();
    }

    public Iterator<ICrawlData> getCacheIterator() {
        return cache.values().iterator();
    };
    
    @Override
    public synchronized void close() {
        if (!db.isClosed()) {
            LOG.info("Closing reference store: " + path);
            db.commit();
            db.close();
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (!db.isClosed()) {
            LOG.info("Closing reference store: " + path);
            db.commit();
            db.close();
        }
        super.finalize();
    }
}
