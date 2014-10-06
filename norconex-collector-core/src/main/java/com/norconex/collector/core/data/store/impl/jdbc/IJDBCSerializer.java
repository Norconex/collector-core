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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.norconex.collector.core.data.ICrawlData;

/**
 * Serializer holding necessary information to insert, load, delete and create
 * document reference information specific to each database tables.
 * @author Pascal Essiembre
 */
public interface IJDBCSerializer {

    /**
     * Gets the SQLs used to create a data store table.
     * @param table the table to create an SQL for
     * @return SQL
     */
    String[] getCreateTableSQLs(String table);
    
    /**
     * Gets the SQL to obtain all {@link ICrawlData} entries in the given 
     * table.
     * @param table table name
     * @return SQL
     */
    String getSelectCrawlDataSQL(String table);
    
    /**
     * Gets the SQL to delete a {@link ICrawlData} from the given table.
     * @param table table name
     * @return SQL
     */
    String getDeleteCrawlDataSQL(String table);
    /**
     * Gets the {@link PreparedStatement} values (if any) necessary to 
     * execute the SQL obtained with {@link #getDeleteCrawlDataSQL(String)}.
     * The values must be returned in the expected order.
     * @param table table name
     * @param crawlData the crawl data to delete
     * @return values
     */
    Object[] getDeleteCrawlDataValues(String table, ICrawlData crawlData);

    /**
     * Gets the SQL to insert a new {@link ICrawlData} in the given table.
     * @param table table name
     * @return SQL
     */
    String getInsertCrawlDataSQL(String table);
    /**
     * Gets the {@link PreparedStatement} values (if any) necessary to 
     * execute the SQL obtained with {@link #getInsertCrawlDataSQL(String)}.
     * The values must be returned in the expected order.
     * @param table table name
     * @param crawlData the crawl data to insert
     * @return values
     */
    Object[] getInsertCrawlDataValues(String table, ICrawlData crawlData);

    /**
     * Gets the SQL to obtain the next {@link ICrawlData} from the queue table.
     * @return SQL
     */
    String getNextQueuedCrawlDataSQL();
    /**
     * Gets the {@link PreparedStatement} values (if any) necessary to 
     * execute the SQL obtained with {@link #getNextQueuedCrawlDataSQL()}.
     * The values must be returned in the expected order.
     * @return values
     */
    Object[] getNextQueuedCrawlDataValues();

    /**
     * Gets the SQL to obtain all {@link ICrawlData} from the cache table.
     * @return SQL
     */
    String getCachedCrawlDataSQL();
    /**
     * Gets the {@link PreparedStatement} values (if any) necessary to 
     * execute the SQL obtained with {@link #getCachedCrawlDataSQL()}.
     * The values must be returned in the expected order.
     * @param reference the reference
     * @return values
     */
    Object[] getCachedCrawlDataValues(String reference);
    
    /**
     * Gets the SQL to find if a {@link ICrawlData} exists in the given table.
     * @param table table name
     * @return SQL
     */
    String getReferenceExistsSQL(String table);
    /**
     * Gets the {@link PreparedStatement} values (if any) necessary to 
     * execute the SQL obtained with {@link #getReferenceExistsSQL(String)}.
     * The values must be returned in the expected order.
     * @param table table name
     * @param reference the reference
     * @return values
     */
    Object[] getReferenceExistsValues(String table, String reference);
    
    /**
     * Convert a database entry to a {@link ICrawlData} instance.
     * @param table table name
     * @param rs SQL result set
     * @return the crawl data
     * @throws SQLException
     */
    ICrawlData toCrawlData(String table, ResultSet rs) throws SQLException;

}
