/* Copyright 2010-2014 Norconex Inc.
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
package com.norconex.collector.core.ref.store.impl.mongo;

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
import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.ref.IReference;
import com.norconex.collector.core.ref.store.AbstractReferenceStore;
import com.norconex.collector.core.ref.store.IReferenceStore;
import com.norconex.collector.core.ref.store.ReferenceStoreException;

/**
 * <p>Mongo implementation of {@link IReferenceStore}.</p>
 * 
 * <p>All the references are stored in a collection named 'reference'. 
 * They go from the
 * "QUEUED", "ACTIVE" and "PROCESSED" states.</p>
 * 
 * <p>The cached references are stored in a separated collection named 
 * "cached".</p>
 * 
 * <p>
 * XML configuration usage:
 * </p>
 * <pre>
 *  &lt;crawlURLDatabaseFactory  
 *      class="com.norconex.collector.http.db.impl.mongo.MongoCrawlURLDatabaseFactory"&gt;
 *      &lt;host&gt;(Optional Mongo server hostname. Default to localhost)&lt;/host&gt;
 *      &lt;port&gt;(Optional Mongo port. Default to 27017)&lt;/port&gt;
 *      &lt;dbname&gt;(Optional Mongo database name. Default to crawl id)&lt;/dbname&gt;
 *      &lt;username&gt;(Optional user name)&lt;/username&gt;
 *      &lt;password&gt;(User password)&lt;/password&gt;
 *  &lt;/crawlURLDatabaseFactory&gt;
 * </pre>
 * <p>
 * If "username" is not provided, no authentication will be attempted. The "username" must be a valid user that has the "readWrite" role over the database (set with "dbname").
 * </p>
 * 
 * @author Pascal Dimassimo
 * @since 1.2
 */
public class MongoReferenceStore extends AbstractReferenceStore {

    private static final String FIELD_VALID = "valid";

//    private static final String FIELD_DOC_CHECKSUM = "doc_checksum";
//
//    private static final String FIELD_HEAD_CHECKSUM = "head_checksum";
//
//    private static final String FIELD_CRAWL_STATUS = "crawl_status";

    private static final String FIELD_DEPTH = "depth";

    private static final String FIELD_STATE = "state";

    private static final String FIELD_REFERENCE = "reference";

    private static final String COLLECTION_CACHED = "cached";

    private static final String COLLECTION_SITEMAPS = "sitemaps";

    private static final String COLLECTION_REFERENCES = "references";

    private static final int BATCH_UPDATE_SIZE = 1000;

    private final DB database;

    private final DBCollection collUrls;
    /*
     * We need to have a separate collection for cached because an url can be
     * both queued and cached (it will be removed from cache when it is
     * processed)
     */
    private final DBCollection collCached;
    private final DBCollection collSitemaps;
    private final IMongoReferenceConverter converter;
    
    enum State {
        QUEUED, ACTIVE, PROCESSED;
    };

    /**
     * Constructor.
     * @param config crawler config
     * @param resume whether to resume an aborted job
     * @param connDetails Mongo connection details
     * @param converter reference converter
     */
    public MongoReferenceStore(
            ICrawlerConfig config, boolean resume,
            MongoConnectionDetails connDetails, 
            IMongoReferenceConverter converter) {
        this(resume, buildMongoDB(config.getId(), connDetails), converter);
    }

    /**
     * Constructor.
     * @param resume whether to resume an aborted job
     * @param database Mongo database
     */
    private MongoReferenceStore(boolean resume, DB database, 
            IMongoReferenceConverter converter) {

        this.converter = converter;
        this.database = database;
        this.collUrls = database.getCollection(COLLECTION_REFERENCES);
        this.collSitemaps = database.getCollection(COLLECTION_SITEMAPS);
        this.collCached = database.getCollection(COLLECTION_CACHED);

        if (resume) {
            changeUrlsState(State.ACTIVE, State.QUEUED);
        } else {
            // Delete everything except valid processed urls that are transfered
            // to cache
            deleteAllDocuments(collSitemaps);
            deleteAllDocuments(collCached);
            processedToCached();
            deleteAllDocuments(collUrls);
        }

        ensureIndex(collUrls, true, FIELD_REFERENCE);
        ensureIndex(collUrls, false, FIELD_STATE);
        ensureIndex(collUrls, false, FIELD_DEPTH, FIELD_STATE);
        ensureIndex(collSitemaps, true, FIELD_REFERENCE);
        ensureIndex(collCached, true, FIELD_REFERENCE);
    }

    private void ensureIndex(DBCollection coll, boolean unique,
            String... fields) {
        BasicDBObject fieldsObject = new BasicDBObject();
        for (String field : fields) {
            fieldsObject.append(field, 1);
        }
        coll.ensureIndex(fieldsObject, null, unique);
    }

    protected static DB buildMongoDB(
            String id, MongoConnectionDetails connDetails) {

        String dbName = MongoUtil.getDbNameOrGenerate(
                connDetails.getDbName(), id);
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
            throw new ReferenceStoreException(e);
        }
    }

    protected void clear() {
        database.dropDatabase();
    }

    public void queue(IReference crawlUrl) {

        BasicDBObject document = converter.convertToMongo(crawlUrl);
//        BasicDBObject document = new BasicDBObject();
//        document.append(FIELD_REFERENCE, crawlUrl.getReference());
//        document.append(FIELD_DEPTH, crawlUrl.getDepth());
        
        
        document.append(FIELD_STATE, State.QUEUED.name());

        // If the document does not exist yet, it will be inserted. If exists,
        // it will be replaced.
        BasicDBObject whereQuery = new BasicDBObject(FIELD_REFERENCE,
                crawlUrl.getReference());
        collUrls.update(whereQuery, document, true, false);
    }

    public boolean isQueueEmpty() {
        return getQueueSize() == 0;
    }

    public int getQueueSize() {
        return getUrlsCount(State.QUEUED);
    }

    public boolean isQueued(String url) {
        return isState(url, State.QUEUED);
    }

    public IReference nextQueued() {

        BasicDBObject whereQuery = new BasicDBObject(FIELD_STATE,
                State.QUEUED.name());
        BasicDBObject sort = new BasicDBObject(FIELD_DEPTH, 1);

        BasicDBObject newDocument = new BasicDBObject("$set",
                new BasicDBObject(FIELD_STATE, State.ACTIVE.name()));

        DBObject next = collUrls.findAndModify(whereQuery, sort, newDocument);
        return convertToCrawlURL(next);
    }

    public boolean isActive(String url) {
        return isState(url, State.ACTIVE);
    }

    public int getActiveCount() {
        return getUrlsCount(State.ACTIVE);
    }

    public IReference getCached(String cacheURL) {
        BasicDBObject whereQuery = new BasicDBObject(FIELD_REFERENCE, cacheURL);
        DBObject result = collCached.findOne(whereQuery);
        return convertToCrawlURL(result);
    }

    private IReference convertToCrawlURL(DBObject result) {
        if (result == null) {
            return null;
        }

        return (IReference) converter.convertFromMongo(result);
        
//        String url = (String) result.get(FIELD_REFERENCE);
//        Integer depth = (Integer) result.get(FIELD_DEPTH);
//        IReference httpDocReference = new IReference(url, depth);
//
//        String crawlStatus = (String) result.get(FIELD_CRAWL_STATUS);
//        if (crawlStatus != null) {
//            httpDocReference.setState(IReferenceState.valueOf(crawlStatus));
//        }
//
//        httpDocReference.setHeadChecksum((String) result.get(FIELD_HEAD_CHECKSUM));
//        httpDocReference.setDocChecksum((String) result.get(FIELD_DOC_CHECKSUM));
//
//        return httpDocReference;
    }

    public boolean isCacheEmpty() {
        return collCached.count() == 0;
    }

    public void processed(IReference httpDocReference) {

        
        
        DBObject document = converter.convertToMongo(httpDocReference);
        document.put(FIELD_STATE, State.PROCESSED.name());
        
//        document.put(FIELD_REFERENCE, httpDocReference.getReference());
//        document.put(FIELD_DEPTH, httpDocReference.getDepth());
//        document.put(FIELD_CRAWL_STATUS, httpDocReference.getState().toString());
//        document.put(FIELD_HEAD_CHECKSUM, httpDocReference.getHeadChecksum());
//        document.put(FIELD_DOC_CHECKSUM, httpDocReference.getDocChecksum());
//        document.put(FIELD_VALID, isValidStatus(httpDocReference));

        // If the document does not exist yet, it will be inserted. If exists,
        // it will be updated.
        BasicDBObject whereQuery = new BasicDBObject(FIELD_REFERENCE,
                httpDocReference.getReference());
        collUrls.update(whereQuery, new BasicDBObject("$set", document), true,
                false);

        // Remove from cache
        collCached.remove(whereQuery);
    }

    public boolean isProcessed(String url) {
        return isState(url, State.PROCESSED);
    }

    public int getProcessedCount() {
        return getUrlsCount(State.PROCESSED);
    }

//    /**
//     * TODO: same code as Derby impl. Put in a abstract base class?
//     */
//    public boolean isVanished(IReference httpDocReference) {
//        IReference cachedURL = getCached(httpDocReference.getReference());
//        if (cachedURL == null) {
//            return false;
//        }
//        IReferenceState cur = httpDocReference.getState();
//        IReferenceState last = cachedURL.getState();
//        return cur != IReferenceState.OK && cur != IReferenceState.UNMODIFIED
//                && (last == IReferenceState.OK || last == IReferenceState.UNMODIFIED);
//    }
//
//    /**
//     * TODO: same code as Derby impl. Put in a abstract base class?
//     */
//    private boolean isValidStatus(IReference httpDocReference) {
//        return httpDocReference.getState() == IReferenceState.OK
//                || httpDocReference.getState() == IReferenceState.UNMODIFIED;
//    }

    private void changeUrlsState(State state, State newState) {
        BasicDBObject whereQuery = new BasicDBObject(FIELD_STATE, state.name());
        BasicDBObject newDocument = new BasicDBObject("$set",
                new BasicDBObject(FIELD_STATE, newState.name()));
        // Batch update
        collUrls.update(whereQuery, newDocument, false, true);
    }

    protected void deleteUrls(String... states) {
        BasicDBObject document = new BasicDBObject();
        List<String> list = Arrays.asList(states);
        document.put(FIELD_STATE, new BasicDBObject("$in", list));
        collUrls.remove(document);
    }

    protected int getUrlsCount(State state) {
        BasicDBObject whereQuery = new BasicDBObject(FIELD_STATE, state.name());
        return (int) collUrls.count(whereQuery);
    }

    protected boolean isState(String url, State state) {
        BasicDBObject whereQuery = new BasicDBObject(FIELD_REFERENCE, url);
        DBObject result = collUrls.findOne(whereQuery);
        if (result == null || result.get(FIELD_STATE) == null) {
            return false;
        }
        State currentState = State.valueOf((String) result.get(FIELD_STATE));
        return state.equals(currentState);
    }

//    @Override
//    public void sitemapResolved(String urlRoot) {
//        BasicDBObject document = new BasicDBObject(FIELD_REFERENCE, urlRoot);
//        collSitemaps.insert(document);
//    }
//
//    @Override
//    public boolean isSitemapResolved(String urlRoot) {
//        BasicDBObject whereQuery = new BasicDBObject(FIELD_REFERENCE, urlRoot);
//        return collSitemaps.findOne(whereQuery) != null;
//    }

    @Override
    public void close() {
        database.getMongo().close();
    }

    private void processedToCached() {
        BasicDBObject whereQuery = new BasicDBObject(FIELD_STATE,
                State.PROCESSED.name());
        whereQuery.put(FIELD_VALID, true);
        DBCursor cursor = collUrls.find(whereQuery);

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
    public Iterator<IReference> getCacheIterator() {
        final DBCursor cursor = collCached.find();
        return new Iterator<IReference>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public IReference next() {
                return convertToCrawlURL(cursor.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
