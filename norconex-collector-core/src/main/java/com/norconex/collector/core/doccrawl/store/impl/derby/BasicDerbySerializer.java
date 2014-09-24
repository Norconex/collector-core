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
package com.norconex.collector.core.doccrawl.store.impl.derby;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.norconex.collector.core.doccrawl.BasicDocCrawl;
import com.norconex.collector.core.doccrawl.DocCrawlState;
import com.norconex.collector.core.doccrawl.IDocCrawl;

/**
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class BasicDerbySerializer implements IDerbySerializer {

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
    public Object[] getDeleteDocCrawlValues(String table, IDocCrawl crawlURL) {
        return new Object[] { crawlURL.getReference() };
    }

    @Override
    public String getInsertDocCrawlSQL(String table) {
        return "INSERT INTO " + table + "(" + ALL_FIELDS 
                + ") values (?,?,?,?,?,?)";
    }
    @Override
    public Object[] getInsertDocCrawlValues(String table, IDocCrawl docCrawl) {
        return new Object[] { 
                docCrawl.getReference(),
                docCrawl.getParentRootReference(),
                docCrawl.isRootParentReference(),
                docCrawl.getState().toString(),
                docCrawl.getMetaChecksum(),
                docCrawl.getContentChecksum()
        };
    }

    @Override
    public String getNextQueuedDocCrawlSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + DerbyDocCrawlStore.TABLE_QUEUE;
    }
    @Override
    public Object[] getNextQueuedDocCrawlValues() {
        return null;
    }

    @Override
    public String getCachedDocCrawlSQL() {
        return "SELECT " + ALL_FIELDS 
                + "FROM " + DerbyDocCrawlStore.TABLE_CACHE
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
    public IDocCrawl toDocCrawl(String table, ResultSet rs)
            throws SQLException {
        if (rs == null) {
            return null;
        }
        BasicDocCrawl data = new BasicDocCrawl();
        data.setReference(rs.getString("reference"));
        data.setParentRootReference(rs.getString("parentRootReference"));
        data.setRootParentReference(rs.getBoolean("isRootParentReference"));
        data.setState(DocCrawlState.valueOf(rs.getString("state")));
        data.setMetaChecksum(rs.getString("metaChecksum"));
        data.setContentChecksum(rs.getString("contentChecksum"));
        return data;
    }
}
