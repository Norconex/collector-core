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
package com.norconex.collector.core.data.store.impl.mongo;

import org.apache.commons.lang3.StringUtils;


/**
 * Utility method for Mongo operations.
 * @author Pascal Dimassimo
 */
public final class MongoUtil {

    public static final String MONGO_INVALID_DBNAME_CHARACTERS = 
            "/\\.\"*<>:|?$";

    private MongoUtil() {
        super();
    }

    /**
     * Return or generate a DB name
     * 
     * If a valid dbName is provided, it is returned as is. If none is provided,
     * a name is generated from the crawl ID (provided in HttpCrawlerConfig)
     * 
     * @param dbName database name
     * @param crawlerId crawler id
     * @throws IllegalArgumentException
     *             if the dbName provided contains invalid characters
     * @return DB name
     */
    public static String getDbNameOrGenerate(
            String dbName, String crawlerId) {

        // If we already have a name, try to use it
        if (dbName != null && dbName.length() > 0) {
            // Validate it
            if (StringUtils
                    .containsAny(dbName, MONGO_INVALID_DBNAME_CHARACTERS)
                    || StringUtils.containsWhitespace(dbName)) {
                throw new IllegalArgumentException("Invalid Mongo DB name: "
                        + dbName);
            }
            return dbName;
        }

        // Generate a name from the crawl ID
        String dbIdName = crawlerId;
        // Replace invalid character with '_'
        for (int i = 0; i < MONGO_INVALID_DBNAME_CHARACTERS.length(); i++) {
            char c = MONGO_INVALID_DBNAME_CHARACTERS.charAt(i);
            dbIdName = dbIdName.replace(c, '_');
        }
        // Replace whitespaces
        dbIdName = dbIdName.replaceAll("\\s", "_");
        return dbIdName ;
    }
}
