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

import com.norconex.collector.core.doccrawl.IDocCrawl;

/**
 * Serializer hoding necessary information to insert, load, delete and create
 * document reference information specific to each collector.
 * @author Pascal Essiembre
 */
public interface IDerbySerializer {

    
    String[] getCreateTableSQLs(String table);
    
    String getSelectDocCrawlSQL(String table);
    
    String getDeleteDocCrawlSQL(String table);
    Object[] getDeleteDocCrawlValues(String table, IDocCrawl docCrawl);
    
    String getInsertDocCrawlSQL(String table);
    Object[] getInsertDocCrawlValues(String table, IDocCrawl docCrawl);

    String getNextQueuedDocCrawlSQL();
    Object[] getNextQueuedDocCrawlValues();

    String getCachedDocCrawlSQL();
    Object[] getCachedDocCrawlValues(String reference);
    
    String getReferenceExistsSQL(String table);
    Object[] getReferenceExistsValues(String table, String reference);
    
    IDocCrawl toDocCrawl(String table, ResultSet rs) throws SQLException;

}
