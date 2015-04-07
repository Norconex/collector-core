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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.data.store.impl.jdbc.JDBCCrawlDataStore.Database;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * JDBC implementation of {@link ICrawlDataStore}.  Defaults to Derby 
 * database.
 * <br><br>
 * Implementing classes should contain the following XML configuration usage:
 * <br><br>
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

    public static final Database DEFAULT_DATABASE = Database.DERBY;
    
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
        Database db = database;
        if (db == null) {
            db = DEFAULT_DATABASE;
        }
        String storeDir = config.getWorkDir().getPath() + "/crawlstore/" 
                + Objects.toString(db).toLowerCase() + "/" 
                + FileUtil.toSafeFileName(config.getId()) + "/";
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
            database = Database.valueOf(dbStr.toUpperCase());
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
