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

import com.norconex.collector.core.data.ICrawlData;

/**
 * Serializer hoding necessary information to insert, load, delete and create
 * document reference information specific to each database tables.
 * @author Pascal Essiembre
 */
public interface IJDBCSerializer {

    
    String[] getCreateTableSQLs(String table);
    
    String getSelectCrawlDataSQL(String table);
    
    String getDeleteCrawlDataSQL(String table);
    Object[] getDeleteCrawlDataValues(String table, ICrawlData crawlData);
    
    String getInsertCrawlDataSQL(String table);
    Object[] getInsertCrawlDataValues(String table, ICrawlData crawlData);

    String getNextQueuedCrawlDataSQL();
    Object[] getNextQueuedCrawlDataValues();

    String getCachedCrawlDataSQL();
    Object[] getCachedCrawlDataValues(String reference);
    
    String getReferenceExistsSQL(String table);
    Object[] getReferenceExistsValues(String table, String reference);
    
    ICrawlData toCrawlData(String table, ResultSet rs) throws SQLException;

}
