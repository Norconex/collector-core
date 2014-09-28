/* Copyright 2010-2014 Norconex Inc.
 * 
 * This file is part of Norconex HTTP Collector.
 * 
 * Norconex HTTP Collector is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex HTTP Collector is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex HTTP Collector. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.data.store.impl.jdbc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.data.store.impl.jdbc.JDBCCrawlDataStore.Database;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * JDBC implementation of {@link ICrawlDataStore}.  Defaults to H2 
 * database.
 * <p />
 * Implementing classes should contain the following XML configuration usage:
 * <p />
 * <pre>
 *  &lt;crawlDataStoreFactory class="(class name)"&gt;
 *      &lt;database&gt;[h2|derby]&lt;/database&gt;
 *  &lt;/crawlDataStoreFactory&gt;
 * </pre>
 * 
 * @author Pascal Essiembre
 * @see BasicJDBCSerializer
 */
public abstract class AbstractJDBCDataStoreFactory 
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private static final long serialVersionUID = 6751191940364554397L;

    public static final Database DEFAULT_DATABASE = Database.H2;
    
    private Database database;
    
    public AbstractJDBCDataStoreFactory() {
        super();
    }
    public AbstractJDBCDataStoreFactory(Database database) {
        super();
        this.database = database;
    }

    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/refstore/" + config.getId() + "/";
        
        Database db = database;
        if (db == null) {
            db = DEFAULT_DATABASE;
        }
        return new JDBCCrawlDataStore(
                db, storeDir, resume, createJDBCSerializer());
    }

    protected abstract IJDBCSerializer createJDBCSerializer();

    public void setDatabase(Database database) {
        this.database = database;
    }
    public Database getDatabase() {
        return database;
    }
    
    @Override
    public void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        String dbStr = xml.getString("database");
        if (StringUtils.isNotBlank(dbStr)) {
            database = Database.valueOf(dbStr);
        }
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawlDataStoreFactory");
            writer.writeAttribute("class", getClass().getCanonicalName());
            if (database != null) {
                writer.writeElementString("database", database.toString());
            }
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
        
    }
}
