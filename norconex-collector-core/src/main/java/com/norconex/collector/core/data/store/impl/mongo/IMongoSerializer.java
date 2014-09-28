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
 * @author Pascal Essiembre
 *
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
    
    BasicDBObject toDBObject(Stage stage, ICrawlData crawlData);
    
    ICrawlData fromDBObject(DBObject dbObject);
    
    DBObject getNextQueued(DBCollection referenceCollection);
    
    void createIndices(
            DBCollection referenceCollection, DBCollection cachedCollection);
}
