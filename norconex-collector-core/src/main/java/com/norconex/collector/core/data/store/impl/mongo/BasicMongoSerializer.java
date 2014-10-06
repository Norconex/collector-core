package com.norconex.collector.core.data.store.impl.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;

public class BasicMongoSerializer implements IMongoSerializer {

    @Override
    public BasicDBObject toDBObject(
            Stage stage, ICrawlData crawlData) {
 
        BasicDBObject doc = new BasicDBObject();
        doc.put(IMongoSerializer.FIELD_REFERENCE, crawlData.getReference());
        doc.put(IMongoSerializer.FIELD_PARENT_ROOT_REFERENCE, 
                crawlData.getParentRootReference());
        doc.put(IMongoSerializer.FIELD_IS_ROOT_PARENT_REFERENCE, 
                crawlData.isRootParentReference());
        doc.put(IMongoSerializer.FIELD_CRAWL_STATE, 
                crawlData.getState().toString());
        doc.put(IMongoSerializer.FIELD_META_CHECKSUM, 
                crawlData.getMetaChecksum());
        doc.put(IMongoSerializer.FIELD_CONTENT_CHECKSUM, 
                crawlData.getContentChecksum());
        doc.put(IMongoSerializer.FIELD_IS_VALID, 
                crawlData.getState().isGoodState());
        doc.put(IMongoSerializer.FIELD_STAGE, stage.toString());
        return doc;
    }

    @Override
    public ICrawlData fromDBObject(DBObject dbObject) {
        if (dbObject == null) {
            return null;
        }
        BaseCrawlData data = new BaseCrawlData();
      
        data.setReference((String) dbObject.get(FIELD_REFERENCE));
        data.setParentRootReference(
                (String) dbObject.get(FIELD_PARENT_ROOT_REFERENCE));
        data.setRootParentReference(
                (Boolean) dbObject.get(FIELD_IS_ROOT_PARENT_REFERENCE));
        String crawlState = (String) dbObject.get(FIELD_CRAWL_STATE);
        if (crawlState != null) {
            data.setState(CrawlState.valueOf(crawlState));
        }
        data.setMetaChecksum((String) dbObject.get(FIELD_META_CHECKSUM));
        data.setDocumentChecksum((String) dbObject.get(FIELD_CONTENT_CHECKSUM));
        return data;
    }

    @Override
    public DBObject getNextQueued(DBCollection collRefs) {
        BasicDBObject whereQuery = 
                new BasicDBObject(FIELD_STAGE, Stage.QUEUED.name());
        BasicDBObject newDocument = new BasicDBObject("$set",
              new BasicDBObject(FIELD_STAGE, Stage.ACTIVE.name()));
        return collRefs.findAndModify(whereQuery, newDocument);
    }

    @Override
    public void createIndices(
            DBCollection referenceCollection, DBCollection cachedCollection) {
        ensureIndex(referenceCollection, true, FIELD_REFERENCE);
        ensureIndex(referenceCollection, false, FIELD_CRAWL_STATE);
        ensureIndex(cachedCollection, true, FIELD_REFERENCE);
    }

    @SuppressWarnings("deprecation")
    protected final void ensureIndex(DBCollection coll, boolean unique,
            String... fields) {
        BasicDBObject fieldsObject = new BasicDBObject();
        for (String field : fields) {
            fieldsObject.append(field, 1);
        }
        coll.ensureIndex(fieldsObject, null, unique);
    }
}
