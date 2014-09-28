/* Copyright 2013-2014 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;
import com.norconex.collector.core.data.store.CrawlDataStoreException;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.mongo.IMongoSerializer.Stage;

/**
 * <p>Mongo implementation of {@link ICrawlDataStore}.</p>
 * 
 * <p>All the references are stored in a collection named 'references'. 
 * They go from the "QUEUED", "ACTIVE" and "PROCESSED" stages.</p>
 * 
 * <p>The cached references are stored in a separated collection named 
 * "cached".</p>
 * 
 * @author Pascal Dimassimo
 * @author Pascal Essiembre
 */
public class MongoCrawlDataStore extends AbstractCrawlDataStore {

    private static final String COLLECTION_CACHED = "cached";
    private static final String COLLECTION_REFERENCES = "references";
    
    private static final int BATCH_UPDATE_SIZE = 1000;

    private final DB database;
    private final IMongoSerializer serializer;
    
    /*
     * We need to have a separate collection for cached because a reference can 
     * be both queued and cached (it will be removed from cache when it is
     * processed)
     */
    private final DBCollection collCached;
    private final DBCollection collRefs;

    /**
     * Constructor.
     * @param crawlerId crawler id
     * @param resume whether to resume an aborted job
     * @param serializer Mongo serializer
     * @param connDetails Mongo connection details
     */
    public MongoCrawlDataStore(String crawlerId, boolean resume,
            MongoConnectionDetails connDetails, IMongoSerializer serializer) {
        this(resume, buildMongoDB(crawlerId, connDetails), serializer);
    }

    /**
     * Constructor.
     * @param resume whether to resume an aborted job
     * @param database Mongo database
     * @param serializer Mongo serializer
     */
    public MongoCrawlDataStore(
            boolean resume, DB database, IMongoSerializer serializer) {

        this.serializer = serializer;
        this.database = database;
        this.collRefs = database.getCollection(COLLECTION_REFERENCES);
        this.collCached = database.getCollection(COLLECTION_CACHED);
        if (resume) {
            changeStage(Stage.ACTIVE, Stage.QUEUED);
        } else {
            // Delete everything except valid processed urls that are 
            // transfered to cache
            deleteAllDocuments(collCached);
            processedToCached();
            deleteAllDocuments(collRefs);
        }
        serializer.createIndices(collRefs, collCached);
    }

    protected static DB buildMongoDB(
            String crawlerId, MongoConnectionDetails connDetails) {
        
        String dbName = MongoUtil.getDbNameOrGenerate(
                connDetails.getDatabaseName(),
                crawlerId);
        try {
            ServerAddress server = new ServerAddress(
                    connDetails.getHost(), connDetails.getPort());
            List<MongoCredential> credentialsList = 
                    new ArrayList<MongoCredential>();
            if (StringUtils.isNoneBlank(connDetails.getUsername())) {
                MongoCredential credential = 
                        MongoCredential.createMongoCRCredential(
                                connDetails.getUsername(), dbName, 
                                connDetails.getPassword().toCharArray());
                credentialsList.add(credential);
            }
            MongoClient client = new MongoClient(server, credentialsList);
            return client.getDB(dbName);
        } catch (UnknownHostException e) {
            throw new CrawlDataStoreException(e);
        }
    }

    protected void clear() {
        database.dropDatabase();
    }

    @Override
    public void queue(ICrawlData crawlData) {
        BasicDBObject document = serializer.toDBObject(Stage.QUEUED, crawlData);
        
        // If the document does not exist yet, it will be inserted. If exists,
        // it will be replaced.
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_REFERENCE, crawlData.getReference());
        collRefs.update(whereQuery, document, true, false);
    }

    @Override
    public boolean isQueueEmpty() {
        return getQueueSize() == 0;
    }

    @Override
    public int getQueueSize() {
        return getReferencesCount(Stage.QUEUED);
    }

    @Override
    public boolean isQueued(String reference) {
        return isStage(reference, Stage.QUEUED);
    }

    @Override
    public ICrawlData nextQueued() {
        return serializer.fromDBObject(serializer.getNextQueued(collRefs));
    }

    @Override
    public boolean isActive(String reference) {
        return isStage(reference, Stage.ACTIVE);
    }

    @Override
    public int getActiveCount() {
        return getReferencesCount(Stage.ACTIVE);
    }

    @Override
    public ICrawlData getCached(String reference) {
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_REFERENCE, reference);
        DBObject result = collCached.findOne(whereQuery);
        return serializer.fromDBObject(result);
    }

    @Override
    public boolean isCacheEmpty() {
        return collCached.count() == 0;
    }

    @Override
    public void processed(ICrawlData crawlData) {
        BasicDBObject document = 
                serializer.toDBObject(Stage.PROCESSED, crawlData);
        // If the document does not exist yet, it will be inserted. If exists,
        // it will be updated.
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_REFERENCE, crawlData.getReference());
        collRefs.update(whereQuery, new BasicDBObject("$set", document), true,
                false);
        // Remove from cache
        collCached.remove(whereQuery);
    }

    @Override
    public boolean isProcessed(String reference) {
        return isStage(reference, Stage.PROCESSED);
    }

    @Override
    public int getProcessedCount() {
        return getReferencesCount(Stage.PROCESSED);
    }

    private void changeStage(
            IMongoSerializer.Stage stage, IMongoSerializer.Stage newStage) {
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_STAGE, stage.name());
        BasicDBObject newDocument = new BasicDBObject("$set", new BasicDBObject(
                IMongoSerializer.FIELD_STAGE, newStage.name()));
        // Batch update
        collRefs.update(whereQuery, newDocument, false, true);
    }

    protected void deleteReferences(String... stages) {
        BasicDBObject document = new BasicDBObject();
        List<String> list = Arrays.asList(stages);
        document.put(IMongoSerializer.FIELD_STAGE, 
                new BasicDBObject("$in", list));
        collRefs.remove(document);
    }

    protected int getReferencesCount(IMongoSerializer.Stage stage) {
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_STAGE, stage.name());
        return (int) collRefs.count(whereQuery);
    }

    protected boolean isStage(String reference, IMongoSerializer.Stage stage) {
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_REFERENCE, reference);        
        DBObject result = collRefs.findOne(whereQuery);
        if (result == null 
                || result.get(IMongoSerializer.FIELD_STAGE) == null) {
            return false;
        }
        IMongoSerializer.Stage currentStage = Stage.valueOf(
                (String) result.get(IMongoSerializer.FIELD_STAGE));
        return stage.equals(currentStage);
    }

    @Override
    public void close() {
        database.getMongo().close();
    }

    private void processedToCached() {
        BasicDBObject whereQuery = new BasicDBObject(
                IMongoSerializer.FIELD_STAGE, Stage.PROCESSED.name());
        whereQuery.put(IMongoSerializer.FIELD_IS_VALID, true);
        DBCursor cursor = collRefs.find(whereQuery);

        // Add them to cache in batch
        ArrayList<DBObject> list = new ArrayList<DBObject>(BATCH_UPDATE_SIZE);
        while (cursor.hasNext()) {
            list.add(cursor.next());
            if (list.size() == BATCH_UPDATE_SIZE) {
                collCached.insert(list);
                list.clear();
            }
        }
        if (!list.isEmpty()) {
            collCached.insert(list);
        }
    }

    private void deleteAllDocuments(DBCollection coll) {
        coll.remove(new BasicDBObject());
    }

    @Override
    public Iterator<ICrawlData> getCacheIterator() {
        final DBCursor cursor = collCached.find();
        return new Iterator<ICrawlData>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }
            @Override
            public ICrawlData next() {
                return serializer.fromDBObject(cursor.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
