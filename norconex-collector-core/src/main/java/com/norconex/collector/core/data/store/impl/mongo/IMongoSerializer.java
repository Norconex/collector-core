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

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.norconex.collector.core.data.ICrawlData;

/**
 * Mongo serializer.
 * @author Pascal Essiembre
 */
public interface IMongoSerializer {

    public enum Stage {
        QUEUED, ACTIVE, PROCESSED;
    }

    String FIELD_REFERENCE = "reference";
    String FIELD_PARENT_ROOT_REFERENCE = "parentRootReference";
    String FIELD_IS_ROOT_PARENT_REFERENCE = "isRootParentReference";
    String FIELD_CRAWL_STATE = "crawlState";
    String FIELD_META_CHECKSUM = "metaChecksum";
    String FIELD_CONTENT_CHECKSUM = "contentChecksum";
    String FIELD_IS_VALID = "isValid";
    String FIELD_STAGE = "stage";
    String FIELD_DEPTH = "depth";
    /** @since 1.5.0 */
    String FIELD_CONTENT_TYPE = "contentType";
    /** @since 1.5.0 */
    String FIELD_CRAWL_DATE = "crawlDate";
    
    /**
     * Converts a {@link ICrawlData} to a Mongo {@link Document}.
     * @param stage the Mongo serializer stage
     * @param crawlData the data to serialize
     * @return Mongo Basic DB object
     */
    Document toDocument(Stage stage, ICrawlData crawlData);
    
    /**
     * Converts a Mongo {@link Document} to an {@link ICrawlData}.
     * @param document Mongo document
     * @return crawl data
     */
    ICrawlData fromDocument(Document document);

    /**
     * Gets the next queued DB document from the given collection.
     * @param referenceCollection the collection to get the next document from
     * @return Mongo document
     */
    Document getNextQueued(MongoCollection<Document> referenceCollection);
    
    /**
     * Creates Mongo indices for the given collections.
     * @param referenceCollection the collection holding crawl references
     * @param cachedCollection the collection holding cached crawl references
     */
    void createIndices(
            MongoCollection<Document> referenceCollection, 
            MongoCollection<Document> cachedCollection);
}
