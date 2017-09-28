/* Copyright 2014-2017 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mongo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.mongo.IMongoSerializer.Stage;
import com.norconex.commons.lang.StringUtil;
import com.norconex.commons.lang.encrypt.EncryptionUtil;

/**
 * <p>Mongo implementation of {@link ICrawlDataStore}.</p>
 *
 * <p>All references are stored in a collection with a default name 
 * of "references".
 * They go from the "QUEUED", "ACTIVE" and "PROCESSED" stages.</p>
 *
 * <p>The cached references are stored in a separated collection with the 
 * default name "cached".</p>
 *
 * <p>
 * As of 1.9.0, you can define your own collection names using one of the
 * new constructors.
 * </p>
 *
 * @author Pascal Essiembre
 */
public class MongoCrawlDataStore extends AbstractCrawlDataStore {

    public static final String DEFAULT_CACHED_COL_NAME = "cached";
    public static final String DEFAULT_REFERENCES_COL_NAME = "references";

    private static final int BATCH_UPDATE_SIZE = 1000;

    private final MongoClient client;
    private final MongoDatabase database;
    private final IMongoSerializer serializer;
    
    private final String referencesCollectionName;
    private final String cachedCollectionName;
    
    /*
     * We need to have a separate collection for cached because a reference can
     * be both queued and cached (it will be removed from cache when it is
     * processed)
     */
    private final MongoCollection<Document> collCached;
    private final MongoCollection<Document> collRefs;

    /**
     * Constructor.
     * @param crawlerId crawler id
     * @param resume whether to resume an aborted job
     * @param serializer Mongo serializer
     * @param conn Mongo connection details
     */
    public MongoCrawlDataStore(String crawlerId, boolean resume,
            MongoConnectionDetails conn, IMongoSerializer serializer) {
        this(resume, buildMongoClient(crawlerId, conn), 
                MongoUtil.getSafeDBName(conn.getDatabaseName(), crawlerId),
                serializer);
    }
    /**
     * Constructor.
     * @param crawlerId crawler id
     * @param resume whether to resume an aborted job
     * @param serializer Mongo serializer
     * @param conn Mongo connection details
     * @param referencesCollectionName name of Mongo references collection
     * @param cachedCollectionName name of Mongo cached collection 
     * @since 1.9.0
     */
    public MongoCrawlDataStore(String crawlerId, boolean resume,
            MongoConnectionDetails conn, IMongoSerializer serializer, 
            String referencesCollectionName, String cachedCollectionName) {
        this(resume, buildMongoClient(crawlerId, conn), 
                MongoUtil.getSafeDBName(conn.getDatabaseName(), crawlerId),
                serializer, referencesCollectionName, cachedCollectionName);
    }

    /**
     * Constructor.
     * @param resume whether to resume an aborted job
     * @param client Mongo client
     * @param dbName Mongo database name
     * @param serializer Mongo serializer
     */
    public MongoCrawlDataStore(boolean resume, MongoClient client, 
            String dbName, IMongoSerializer serializer) {
        this(resume, client, dbName, serializer, null, null);
    }
    /**
     * Constructor.
     * @param resume whether to resume an aborted job
     * @param client Mongo client
     * @param dbName Mongo database name
     * @param serializer Mongo serializer
     * @param referencesCollectionName name of Mongo references collection
     * @param cachedCollectionName name of Mongo cached collection 
     * @since 1.9.0
     */
    public MongoCrawlDataStore(boolean resume, MongoClient client, 
            String dbName, IMongoSerializer serializer,
            String referencesCollectionName, String cachedCollectionName) {
        this.serializer = serializer;
        this.client = client;
        this.database = client.getDatabase(dbName);
        this.collRefs = database.getCollection(StringUtils.defaultIfBlank(
                referencesCollectionName, DEFAULT_REFERENCES_COL_NAME));
        this.collCached = database.getCollection(
                StringUtils.defaultIfBlank(
                        cachedCollectionName, DEFAULT_CACHED_COL_NAME));
        this.referencesCollectionName = referencesCollectionName;
        this.cachedCollectionName = cachedCollectionName;
        
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
    
    /**
     * Gets the references collection name. Defaults to "references".
     * @return collection name
     * @since 1.9.0
     */
    public String getReferencesCollectionName() {
        return referencesCollectionName;
    }
    /**
     * Gets the cached collection name. Defaults to "cached".
     * @return collection name
     * @since 1.9.0
     */
    public String getCachedCollectionName() {
        return cachedCollectionName;
    }

    protected static MongoClient buildMongoClient(
            String crawlerId, MongoConnectionDetails connDetails) {

        String dbName = MongoUtil.getSafeDBName(
                connDetails.getDatabaseName(), crawlerId);
        
        int port = connDetails.getPort();
        if (port <= 0) {
        	port = ServerAddress.defaultPort();
        }

        ServerAddress server = new ServerAddress(
                connDetails.getHost(), port);
        List<MongoCredential> credentialsList = new ArrayList<>();
        if (StringUtils.isNoneBlank(connDetails.getUsername())) {

            String password = EncryptionUtil.decrypt(
                    connDetails.getPassword(),
                    connDetails.getPasswordKey());

            MongoCredential credential = buildMongoCredential(
                    connDetails.getUsername(),
                    dbName, password.toCharArray(),
                    connDetails.getMechanism());
            credentialsList.add(credential);
        }
        return new MongoClient(server, credentialsList);
    }
    
    protected static MongoCredential buildMongoCredential(
            String username,
            String dbName,
            char[] password,
            String mechanism) {
        if (MongoCredential.MONGODB_CR_MECHANISM.equals(mechanism)) {
            return MongoCredential.createMongoCRCredential(
                    username, dbName, password);
        } 
        if (MongoCredential.SCRAM_SHA_1_MECHANISM.equals(mechanism)) {
            return MongoCredential.createScramSha1Credential(
                    username, dbName, password);
        }
        return MongoCredential.createCredential(username, dbName, password);
    }

    @Override
    public void queue(ICrawlData crawlData) {
        Document document = serializer.toDocument(Stage.QUEUED, crawlData);

        // If the document does not exist yet, it will be inserted. If exists,
        // it will be replaced.
        collRefs.updateOne(referenceFilter(crawlData.getReference()), 
                new Document("$set", document),
                new UpdateOptions().upsert(true));
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
        return serializer.fromDocument(serializer.getNextQueued(collRefs));
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
        Document result = collCached.find(referenceFilter(reference)).first();
        return serializer.fromDocument(result);
    }

    @Override
    public boolean isCacheEmpty() {
        return collCached.count() == 0;
    }

    @Override
    public void processed(ICrawlData crawlData) {
        Document document = serializer.toDocument(Stage.PROCESSED, crawlData);
        // If the document does not exist yet, it will be inserted. If exists,
        // it will be updated.
        Bson filter = referenceFilter(crawlData.getReference());
        collRefs.updateOne(filter, 
                new Document("$set", document),
                new UpdateOptions().upsert(true));

        // Remove from cache
        collCached.deleteOne(filter);
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
        Document newDocument = new Document("$set", new Document(
                IMongoSerializer.FIELD_STAGE, newStage.name()));
        // Batch update
        collRefs.updateMany(
                eq(IMongoSerializer.FIELD_STAGE, stage.name()), newDocument);
    }

    protected void deleteReferences(String... stages) {
        Document document = new Document();
        List<String> list = Arrays.asList(stages);
        document.put(IMongoSerializer.FIELD_STAGE, new Document("$in", list));
        collRefs.deleteMany(document);
    }

    protected int getReferencesCount(IMongoSerializer.Stage stage) {
        return (int) collRefs.count(
                eq(IMongoSerializer.FIELD_STAGE, stage.name()));
    }

    protected boolean isStage(String reference, IMongoSerializer.Stage stage) {
        Document result = collRefs.find(referenceFilter(reference)).first();
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
        client.close();
    }

    private void processedToCached() {
        Bson filter = and(
                eq(IMongoSerializer.FIELD_STAGE, Stage.PROCESSED.name()),
                eq(IMongoSerializer.FIELD_IS_VALID, true));
        MongoCursor<Document> cursor = collRefs.find(filter).iterator();

        // Add them to cache in batch
        ArrayList<Document> list = new ArrayList<>(BATCH_UPDATE_SIZE);
        while (cursor.hasNext()) {
            list.add(cursor.next());
            if (list.size() == BATCH_UPDATE_SIZE) {
                collCached.insertMany(list);
                list.clear();
            }
        }
        if (!list.isEmpty()) {
            collCached.insertMany(list);
        }
    }

    private void deleteAllDocuments(MongoCollection<Document> coll) {
        coll.deleteMany(new Document());
    }

    private Bson referenceFilter(String reference) {
        return eq(IMongoSerializer.FIELD_REFERENCE, 
                StringUtil.truncateWithHash(reference, 1024, '!'));
    }
    
    @Override
    public Iterator<ICrawlData> getCacheIterator() {
        final MongoCursor<Document> cursor = collCached.find().iterator();
        return new Iterator<ICrawlData>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }
            @Override
            public ICrawlData next() {
                return serializer.fromDocument(cursor.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
