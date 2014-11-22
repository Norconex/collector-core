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
package com.norconex.collector.core.data.store.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;

/**
 * Basic JDBC serializer for storing and retrieving {@link BaseCrawlData}
 * instances.
 * @author Pascal Essiembre
 */
public class BasicJDBCSerializer implements IJDBCSerializer {

    protected static final String ALL_FIELDS = 
              "reference, "
            + "parentRootReference, "
            + "isRootParentReference, "
            + "state, "
            + "metaChecksum, "
            + "contentChecksum ";
    
    @Override
    public String[] getCreateTableSQLs(String table) {
        String sql = "CREATE TABLE " + table + " ("
                + "reference VARCHAR(32672) NOT NULL, "
                + "parentRootReference VARCHAR(32672), "
                + "isRootParentReference BOOLEAN, "
                + "state VARCHAR(256), "
                + "metaChecksum VARCHAR(32672), "
                + "contentChecksum VARCHAR(32672), "
                + "PRIMARY KEY (reference))";
        return new String[] { sql };
    }

    
    @Override
    public String getSelectCrawlDataSQL(String table) {
        return "SELECT " + ALL_FIELDS + "FROM " + table;
    }

    @Override
    public String getDeleteCrawlDataSQL(String table) {
        return "DELETE FROM " + table + " WHERE reference = ?";
    }
    public Object[] getDeleteCrawlDataValues(String table, ICrawlData crawlURL) {
        return new Object[] { crawlURL.getReference() };
    }

    @Override
    public String getInsertCrawlDataSQL(String table) {
        return "INSERT INTO " + table + "(" + ALL_FIELDS 
                + ") values (?,?,?,?,?,?)";
    }
    @Override
    public Object[] getInsertCrawlDataValues(String table, ICrawlData crawlData) {
        return new Object[] { 
                crawlData.getReference(),
                crawlData.getParentRootReference(),
                crawlData.isRootParentReference(),
                crawlData.getState().toString(),
                crawlData.getMetaChecksum(),
                crawlData.getContentChecksum()
        };
    }

    @Override
    public String getNextQueuedCrawlDataSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + JDBCCrawlDataStore.TABLE_QUEUE;
    }
    @Override
    public Object[] getNextQueuedCrawlDataValues() {
        return null;
    }

    @Override
    public String getCachedCrawlDataSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + JDBCCrawlDataStore.TABLE_CACHE
                + " WHERE reference = ? ";
    }
    @Override
    public Object[] getCachedCrawlDataValues(String reference) {
        return new Object[] { reference };
    }

    @Override
    public String getReferenceExistsSQL(String table) {
        return "SELECT 1 FROM " + table + " WHERE reference = ?";
    }
    @Override
    public Object[] getReferenceExistsValues(String table, String reference) {
        return new Object[] { reference };
    }

    @Override
    public ICrawlData toCrawlData(String table, ResultSet rs)
            throws SQLException {
        if (rs == null) {
            return null;
        }
        BaseCrawlData data = new BaseCrawlData();
        data.setReference(rs.getString("reference"));
        data.setParentRootReference(rs.getString("parentRootReference"));
        data.setRootParentReference(rs.getBoolean("isRootParentReference"));
        data.setState(CrawlState.valueOf(rs.getString("state")));
        data.setMetaChecksum(rs.getString("metaChecksum"));
        data.setDocumentChecksum(rs.getString("contentChecksum"));
        return data;
    }
}
