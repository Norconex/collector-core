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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.AbstractCrawlDataStore;
import com.norconex.collector.core.data.store.CrawlDataStoreException;

public class JDBCCrawlDataStore extends AbstractCrawlDataStore {

    private static final Logger LOG = 
            LogManager.getLogger(JDBCCrawlDataStore.class);
    
    public static enum Database { DERBY, H2 };
    
    public static final String TABLE_QUEUE = "queue";
    public static final String TABLE_ACTIVE = "active";
    public static final String TABLE_CACHE = "cache";
    public static final String TABLE_PROCESSED_VALID = "valid";
    public static final String TABLE_PROCESSED_INVALID = "invalid";
    
    private static final int NUMBER_OF_TABLES = 5;
    private static final int DERBY_ERROR_ALREADY_EXISTS = 30000;
    private static final String DERBY_STATE_SHUTDOWN_SUCCESS = "08006";
    private static final int H2_ERROR_ALREADY_EXISTS = 23505;

    private final DataSource datasource;
    private final IJDBCSerializer serializer;
    private final String dbDir;
    private final Database database;
    
    public JDBCCrawlDataStore(Database database, String path, boolean resume,
            IJDBCSerializer serializer) {
        super();
        
        this.database = database;
        this.serializer = serializer;
        String fullPath = new File(path).getAbsolutePath();
        
        LOG.info("Initializing crawl document reference store: " + fullPath);

        new File(fullPath).mkdirs();
        if (database == Database.DERBY) {
            System.setProperty("derby.system.home", fullPath + "/derby/log");
            this.dbDir = fullPath + "/derby/db";
        } else {
            this.dbDir = fullPath + "/h2/db";
        }
        this.datasource = createDataSource(dbDir);
        boolean incrementalRun;
        try {
            incrementalRun = ensureTablesExist();
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem creating crawl store.", e);
        }
        if (resume) {
            LOG.debug("Resuming: putting active URLs back in the queue...");
            copyCrawlURLsToQueue(TABLE_ACTIVE);
            LOG.debug("Cleaning active database...");
            sqlClearTable(TABLE_ACTIVE);
        } else if (incrementalRun) {
            LOG.info("Caching processed URL from last run (if any)...");
            LOG.debug("Rename processed table to cache...");
            sqlUpdate("DROP TABLE " + TABLE_CACHE);
            if (database == Database.DERBY) {
                sqlUpdate("RENAME TABLE " + TABLE_PROCESSED_VALID 
                        + " TO " + TABLE_CACHE);
            } else {
                sqlUpdate("ALTER TABLE " + TABLE_PROCESSED_VALID 
                        + " RENAME TO " + TABLE_CACHE);
            }
            LOG.debug("Cleaning queue table...");
            sqlClearTable(TABLE_QUEUE);
            LOG.debug("Cleaning invalid URLS table...");
            sqlClearTable(TABLE_PROCESSED_INVALID);
            LOG.debug("Cleaning active table...");
            sqlClearTable(TABLE_ACTIVE);
            LOG.debug("Re-creating processed table...");
            sqlCreateTable(TABLE_PROCESSED_VALID);
        }
        LOG.info("Done crawl document reference store.");
    }

    @Override
    public final synchronized void queue(ICrawlData crawlData) {
        sqlInsertDocCrawl(TABLE_QUEUE, crawlData);
    }

    @Override
    public final synchronized void processed(ICrawlData crawlData) {
        ICrawlData docCrawlCopy = crawlData.safeClone();
        
        String table;
        if (docCrawlCopy.getState().isGoodState()) {
            table = TABLE_PROCESSED_VALID;
        } else {
            table = TABLE_PROCESSED_INVALID;
        }
        sqlInsertDocCrawl(table, docCrawlCopy);
        sqlDeleteCrawlURL(TABLE_ACTIVE, docCrawlCopy);
        sqlDeleteCrawlURL(TABLE_CACHE, docCrawlCopy);
    }

    @Override
    public final synchronized boolean isQueueEmpty() {
        return getQueueSize()  == 0;
    }

    @Override
    public final synchronized int getQueueSize() {
        return sqlRecordCount(TABLE_QUEUE);
    }

    @Override
    public final synchronized boolean isQueued(String url) {
        return sqlURLExists(TABLE_QUEUE, url);
    }

    @Override
    public final synchronized ICrawlData nextQueued() {
        ICrawlData crawlData = sqlFindCrawlURL(TABLE_QUEUE, 
                serializer.getNextQueuedDocCrawlSQL(),
                serializer.getNextQueuedDocCrawlValues());
        if (crawlData != null) {
            sqlInsertDocCrawl(TABLE_ACTIVE, crawlData);
            sqlDeleteCrawlURL(TABLE_QUEUE, crawlData);
        }
        return crawlData;
    }

    @Override
    public final synchronized boolean isActive(String url) {
        return sqlURLExists(TABLE_ACTIVE, url);
    }
    
    @Override
    public final synchronized int getActiveCount() {
        return sqlRecordCount(TABLE_ACTIVE);
    }

    @Override
    public synchronized ICrawlData getCached(String reference) {
        ICrawlData crawlData = sqlFindCrawlURL(TABLE_CACHE, 
                serializer.getCachedDocCrawlSQL(),
                serializer.getCachedDocCrawlValues(reference));
        return crawlData;
    }

    @Override
    public final synchronized boolean isCacheEmpty() {
        return sqlRecordCount(TABLE_CACHE) == 0;
    }
    
    @Override
    public final synchronized boolean isProcessed(String url) {
        return sqlURLExists(TABLE_PROCESSED_VALID, url)
                || sqlURLExists(TABLE_PROCESSED_INVALID, url);
    }

    @Override
    public final synchronized int getProcessedCount() {
        return sqlRecordCount(TABLE_PROCESSED_VALID)
                + sqlRecordCount(TABLE_PROCESSED_INVALID);
    }

    @Override
    public Iterator<ICrawlData> getCacheIterator() {
        try {
            final Connection conn = datasource.getConnection(); 
            final Statement stmt = conn.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            final ResultSet rs = stmt.executeQuery(
                    serializer.getSelectDocCrawlSQL(TABLE_CACHE));
            
            if (rs == null || !rs.first()) {
                return null;
            }
            rs.beforeFirst();
            return new CrawlURLIterator(TABLE_CACHE, rs, conn, stmt);
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem getting database cache iterator.", e);            
        }
    }
    
    
    @Override
    public void close() {
        if (database == Database.DERBY) {
            try {
                LOG.info("Closing Derby database...");
                DriverManager.getConnection(
                        "jdbc:derby:" + dbDir + ";shutdown=true");
            } catch (SQLException e) {
                if (!DERBY_STATE_SHUTDOWN_SUCCESS.equals(e.getSQLState())) {
                    throw new CrawlDataStoreException(
                            "Cannot shutdown Derby database.", e);
                }
                LOG.info("Derby database closed.");
            }
        }
    }
    
    private boolean sqlURLExists(String table, String reference) {
        return sqlQueryInteger(serializer.getReferenceExistsSQL(table),
                serializer.getReferenceExistsValues(table, reference)) > 0;
    }
    
    private void sqlClearTable(String table) {
        sqlUpdate("DELETE FROM " + table);
    }
    
    private void sqlDeleteCrawlURL(String table, ICrawlData crawlData) {
        sqlUpdate(serializer.getDeleteDocCrawlSQL(table),
                serializer.getDeleteDocCrawlValues(table, crawlData));
    }
    
    private int sqlRecordCount(String table) {
        return sqlQueryInteger("SELECT count(*) FROM " + table);
    }

    private void sqlInsertDocCrawl(String table, ICrawlData crawlData) {
        sqlUpdate(serializer.getInsertDocCrawlSQL(table),
                serializer.getInsertDocCrawlValues(table, crawlData));
    }
    
    private void copyCrawlURLsToQueue(final String sourceTable) {
        ResultSetHandler<Void> h = new ResultSetHandler<Void>() {
            @Override
            public Void handle(ResultSet rs) throws SQLException {
                while(rs.next()) {
                    ICrawlData crawlURL = serializer.toDocCrawl(sourceTable, rs);
                    //toCrawlURL(rs);
                    if (crawlURL != null) {
                        queue(crawlURL);
                    }
                }
                return null;
            }
        };
        try {
            new QueryRunner(datasource).query(
                    serializer.getSelectDocCrawlSQL(sourceTable), h);
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem loading crawl URL from database.", e);            
        }
    }

    private ICrawlData sqlFindCrawlURL(
            final String table, String sql, Object... params) {
      try {
          ResultSetHandler<ICrawlData> h = new ResultSetHandler<ICrawlData>() {
              @Override
              public ICrawlData handle(ResultSet rs) throws SQLException {
                  if (rs.next()) {
                      return serializer.toDocCrawl(table, rs);
                      //toCrawlURL(rs);
                  }
                  return null;
              }
          };
          if (LOG.isDebugEnabled()) {
              LOG.debug("SQL: " + sql);
          }
          return new QueryRunner(datasource).query(sql, h, params);
      } catch (SQLException e) {
          throw new CrawlDataStoreException(
                  "Problem running database query.", e);            
      }
    }
    
    private int sqlQueryInteger(String sql, Object... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL: " + sql);
        }
        try {
            Object value = new QueryRunner(datasource).query(
                    sql, new ScalarHandler<Object>(), params);
            if (value == null) {
                return 0;
            }
            if (value instanceof Long) {
                return ((Long) value).intValue();
            } else {
                return (int) value;
            }
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem getting database scalar value.", e);            
        }
    }
    private void sqlUpdate(String sql, Object... params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL: " + sql);
        }
        try {
            new QueryRunner(datasource).update(sql, params);
        } catch (SQLException e) {
            if (alreadyExists(e)) {
                LOG.debug("Already exists in table. SQL Error:" 
                        + e.getMessage());
            } else {
                throw new CrawlDataStoreException(
                        "Problem updating database.", e);            
            }
        }
    }

    private boolean alreadyExists(SQLException e) {
        return (database == Database.DERBY 
                && e.getErrorCode() == DERBY_ERROR_ALREADY_EXISTS)
                || (database == Database.H2
                        && e.getErrorCode() == H2_ERROR_ALREADY_EXISTS);
    }
    
    private DataSource createDataSource(String dbDir) {
        BasicDataSource ds = new BasicDataSource();
        if (database == Database.DERBY) {
            ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
            ds.setUrl("jdbc:derby:" + dbDir + ";create=true");
        } else {
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:" + dbDir);
        }
        ds.setDefaultAutoCommit(true);
        return ds;
    }

    private boolean ensureTablesExist() throws SQLException {
        ArrayListHandler arrayListHandler = new ArrayListHandler();
        Connection conn = datasource.getConnection();
        List<Object[]> tables = arrayListHandler.handle(
                conn.getMetaData().getTables(
                        null, null, null, new String[]{"TABLE"}));
        conn.close();
        if (tables.size() == NUMBER_OF_TABLES) {
            LOG.debug("    Re-using existing tables.");
            return true;
        }
        LOG.debug("    Creating new crawl tables...");
        sqlCreateTable(TABLE_QUEUE);
        sqlCreateTable(TABLE_ACTIVE);
        sqlCreateTable(TABLE_PROCESSED_VALID);
        sqlCreateTable(TABLE_PROCESSED_INVALID);
        sqlCreateTable(TABLE_CACHE);
        return false;
    }

    private void sqlCreateTable(String table) {
        String[] sqls = serializer.getCreateTableSQLs(table);
        for (String sql : sqls) {
            sqlUpdate(sql);
        }
    }
    
    private final class CrawlURLIterator implements Iterator<ICrawlData> {
        private final ResultSet rs;
        private final Connection conn;
        private final Statement stmt;
        private final String tableName;

        private CrawlURLIterator(
                String tableName,
                ResultSet rs, Connection conn, Statement stmt) {
            this.tableName = tableName;
            this.rs = rs;
            this.conn = conn;
            this.stmt = stmt;
        }

        @Override
        public boolean hasNext() {
            try {
                if (conn.isClosed()) {
                    return false;
                }
                if (!rs.isLast()) {
                    return true;
                } else {
                    DbUtils.closeQuietly(conn, stmt, rs);
                    return false;
                }
            } catch (SQLException e) {
                LOG.error("Database problem.", e);
                return false;
            }
        }

        @Override
        public ICrawlData next() {
            try {
                if (rs.next()) {
                    return serializer.toDocCrawl(tableName, rs);
                }
            } catch (SQLException e) {
                LOG.error("Database problem.", e);
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}
