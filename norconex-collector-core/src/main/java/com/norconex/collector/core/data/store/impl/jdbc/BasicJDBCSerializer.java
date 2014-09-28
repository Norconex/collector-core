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
package com.norconex.collector.core.data.store.impl.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.norconex.collector.core.data.BasicCrawlData;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;

/**
 * @author Pascal Essiembre
 * @since 2.0.0
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
    public String getSelectDocCrawlSQL(String table) {
        return "SELECT " + ALL_FIELDS + "FROM " + table;
    }

    @Override
    public String getDeleteDocCrawlSQL(String table) {
        return "DELETE FROM " + table + " WHERE reference = ?";
    }
    public Object[] getDeleteDocCrawlValues(String table, ICrawlData crawlURL) {
        return new Object[] { crawlURL.getReference() };
    }

    @Override
    public String getInsertDocCrawlSQL(String table) {
        return "INSERT INTO " + table + "(" + ALL_FIELDS 
                + ") values (?,?,?,?,?,?)";
    }
    @Override
    public Object[] getInsertDocCrawlValues(String table, ICrawlData crawlData) {
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
    public String getNextQueuedDocCrawlSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + JDBCCrawlDataStore.TABLE_QUEUE;
    }
    @Override
    public Object[] getNextQueuedDocCrawlValues() {
        return null;
    }

    @Override
    public String getCachedDocCrawlSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + JDBCCrawlDataStore.TABLE_CACHE
                + " WHERE reference = ? ";
    }
    @Override
    public Object[] getCachedDocCrawlValues(String reference) {
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
    public ICrawlData toDocCrawl(String table, ResultSet rs)
            throws SQLException {
        if (rs == null) {
            return null;
        }
        BasicCrawlData data = new BasicCrawlData();
        data.setReference(rs.getString("reference"));
        data.setParentRootReference(rs.getString("parentRootReference"));
        data.setRootParentReference(rs.getBoolean("isRootParentReference"));
        data.setState(CrawlState.valueOf(rs.getString("state")));
        data.setMetaChecksum(rs.getString("metaChecksum"));
        data.setContentChecksum(rs.getString("contentChecksum"));
        return data;
    }
}
