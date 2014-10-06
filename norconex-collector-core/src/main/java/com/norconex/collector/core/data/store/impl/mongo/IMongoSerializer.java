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
package com.norconex.collector.core.data.store.impl.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.norconex.collector.core.data.ICrawlData;

/**
 * Mongo serializer.
 * @author Pascal Essiembre
 */
public interface IMongoSerializer {

    public static enum Stage {
        QUEUED, ACTIVE, PROCESSED;
    }

    public static final String FIELD_REFERENCE = "reference";
    public static final String FIELD_PARENT_ROOT_REFERENCE = 
            "parentRootReference";
    public static final String FIELD_IS_ROOT_PARENT_REFERENCE = 
            "isRootParentReference";
    public static final String FIELD_CRAWL_STATE = "crawlState";
    public static final String FIELD_META_CHECKSUM = "metaChecksum";
    public static final String FIELD_CONTENT_CHECKSUM = "contentChecksum";
    public static final String FIELD_IS_VALID = "isValid";
    public static final String FIELD_STAGE = "stage";
    
    /**
     * Converts a {@link ICrawlData} to a Mongo {@link BasicDBObject}.
     * @param stage the Mongo serializer stage
     * @param crawlData the data to serialize
     * @return Mongo Basic DB object
     */
    BasicDBObject toDBObject(Stage stage, ICrawlData crawlData);
    
    /**
     * Converts a Mongo {@link DBObject} to an {@link ICrawlData}.
     * @param dbObject Mongo db object
     * @return crawl data
     */
    ICrawlData fromDBObject(DBObject dbObject);

    /**
     * Gets the next queued DB object from the given collection.
     * @param referenceCollection the collection to get the next DB object from
     * @return Mongo DB object
     */
    DBObject getNextQueued(DBCollection referenceCollection);
    
    /**
     * Creates Mongo indices for the given collections.
     * @param referenceCollection the collection holding crawl references
     * @param cachedCollection the collection holding cached crawl references
     */
    void createIndices(
            DBCollection referenceCollection, DBCollection cachedCollection);
}
