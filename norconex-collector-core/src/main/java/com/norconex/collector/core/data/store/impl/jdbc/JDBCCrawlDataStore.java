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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
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
import org.apache.commons.io.FileUtils;
import org.apache.derby.jdbc.EmbeddedDriver;
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

        try {
            FileUtils.forceMkdir(new File(fullPath));
        } catch (IOException e) {
            throw new CrawlDataStoreException(
                    "Cannot create crawl store directory: " + fullPath, e);
        }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Active count: " + getActiveCount());
                LOG.debug("Processed count: " + getProcessedCount());
                LOG.debug("Putting active references back in the queue...");
            }
            copyCrawlDatasToQueue(TABLE_ACTIVE);
            LOG.debug("Cleaning active database...");
            sqlClearTable(TABLE_ACTIVE);
        } else if (incrementalRun) {
            LOG.info("Caching processed reference from last run (if any)...");
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
            LOG.debug("Cleaning invalid references table...");
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
        sqlInsertCrawlData(TABLE_QUEUE, crawlData);
    }

    @Override
    public final synchronized void processed(ICrawlData crawlData) {
        ICrawlData crawlDataCopy = crawlData.clone();
        String table;
        if (crawlDataCopy.getState().isGoodState()) {
            table = TABLE_PROCESSED_VALID;
        } else {
            table = TABLE_PROCESSED_INVALID;
        }
        sqlInsertCrawlData(table, crawlDataCopy);
        sqlDeleteCrawlData(TABLE_ACTIVE, crawlDataCopy);
        sqlDeleteCrawlData(TABLE_CACHE, crawlDataCopy);
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
    public final synchronized boolean isQueued(String reference) {
        return sqlReferenceExists(TABLE_QUEUE, reference);
    }

    @Override
    public final synchronized ICrawlData nextQueued() {
        ICrawlData crawlData = sqlFindCrawlData(TABLE_QUEUE, 
                serializer.getNextQueuedCrawlDataSQL(),
                serializer.getNextQueuedCrawlDataValues());
        if (crawlData != null) {
            sqlInsertCrawlData(TABLE_ACTIVE, crawlData);
            sqlDeleteCrawlData(TABLE_QUEUE, crawlData);
        }
        return crawlData;
    }

    @Override
    public final synchronized boolean isActive(String reference) {
        return sqlReferenceExists(TABLE_ACTIVE, reference);
    }
    
    @Override
    public final synchronized int getActiveCount() {
        return sqlRecordCount(TABLE_ACTIVE);
    }

    @Override
    public synchronized ICrawlData getCached(String reference) {
        ICrawlData crawlData = sqlFindCrawlData(TABLE_CACHE, 
                serializer.getCachedCrawlDataSQL(),
                serializer.getCachedCrawlDataValues(reference));
        return crawlData;
    }

    @Override
    public final synchronized boolean isCacheEmpty() {
        return sqlRecordCount(TABLE_CACHE) == 0;
    }
    
    @Override
    public final synchronized boolean isProcessed(String reference) {
        return sqlReferenceExists(TABLE_PROCESSED_VALID, reference)
                || sqlReferenceExists(TABLE_PROCESSED_INVALID, reference);
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
                    serializer.getSelectCrawlDataSQL(TABLE_CACHE));
            if (rs == null || !rs.first()) {
                return null;
            }
            rs.beforeFirst();
            return new CrawlDataIterator(TABLE_CACHE, rs, conn, stmt);
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem getting database cache iterator.", e);            
        }
    }
    
    
    @Override
    public void close() {
        if (database == Database.DERBY) {
            Connection conn = null;
            try {
                LOG.info("Closing Derby database...");
                conn = new EmbeddedDriver().connect(
                        "jdbc:derby:" + dbDir + ";shutdown=true", null);
            } catch (SQLException e) {
                if (!DERBY_STATE_SHUTDOWN_SUCCESS.equals(e.getSQLState())) {
                    throw new CrawlDataStoreException(
                            "Cannot shutdown Derby database.", e);
                }
                LOG.info("Derby database closed.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        LOG.info("Problem closing database "
                                + "shutdown connection.", e);
                    }
                }
            }
        }
    }
    
    private boolean sqlReferenceExists(String table, String reference) {
        return sqlQueryInteger(serializer.getReferenceExistsSQL(table),
                serializer.getReferenceExistsValues(table, reference)) > 0;
    }
    
    private void sqlClearTable(String table) {
        sqlUpdate("DELETE FROM " + table);
    }
    
    private void sqlDeleteCrawlData(String table, ICrawlData crawlData) {
        sqlUpdate(serializer.getDeleteCrawlDataSQL(table),
                serializer.getDeleteCrawlDataValues(table, crawlData));
    }
    
    private int sqlRecordCount(String table) {
        return sqlQueryInteger("SELECT count(*) FROM " + table);
    }

    private void sqlInsertCrawlData(String table, ICrawlData crawlData) {
        sqlUpdate(serializer.getInsertCrawlDataSQL(table),
                serializer.getInsertCrawlDataValues(table, crawlData));
    }
    
    private void copyCrawlDatasToQueue(final String sourceTable) {
        ResultSetHandler<Void> h = new ResultSetHandler<Void>() {
            @Override
            public Void handle(ResultSet rs) throws SQLException {
                while(rs.next()) {
                    ICrawlData crawlData = 
                            serializer.toCrawlData(sourceTable, rs);
                    if (crawlData != null) {
                        queue(crawlData);
                    }
                }
                return null;
            }
        };
        try {
            new QueryRunner(datasource).query(
                    serializer.getSelectCrawlDataSQL(sourceTable), h);
        } catch (SQLException e) {
            throw new CrawlDataStoreException(
                    "Problem loading crawl data from database.", e);            
        }
    }

    private ICrawlData sqlFindCrawlData(
            final String table, String sql, Object... params) {
      try {
          ResultSetHandler<ICrawlData> h = new ResultSetHandler<ICrawlData>() {
              @Override
              public ICrawlData handle(ResultSet rs) throws SQLException {
                  if (rs.next()) {
                      return serializer.toCrawlData(table, rs);
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
            ds.setUrl("jdbc:h2:" + dbDir + ";WRITE_DELAY=0;AUTOCOMMIT=ON");
        }
        ds.setDefaultAutoCommit(true);
        return ds;
    }

    private boolean ensureTablesExist() throws SQLException {
        ArrayListHandler arrayListHandler = new ArrayListHandler();
        Connection conn = null;
        try {                
            conn = datasource.getConnection();
            List<Object[]> tables = arrayListHandler.handle(
                    conn.getMetaData().getTables(
                            null, null, null, new String[]{"TABLE"}));
            if (tables.size() == NUMBER_OF_TABLES) {
                LOG.debug("    Re-using existing tables.");
                return true;
            }
        } finally {
            DbUtils.closeQuietly(conn);
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
    
    private final class CrawlDataIterator implements Iterator<ICrawlData> {
        private final ResultSet rs;
        private final Connection conn;
        private final Statement stmt;
        private final String tableName;

        private CrawlDataIterator(
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
                    return serializer.toCrawlData(tableName, rs);
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
