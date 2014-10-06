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
package com.norconex.collector.core.data.store.impl.mongo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * <p>Mongo implementation of {@link ICrawlDataStore}.</p>
 * 
 * <p>All the references are stored in a collection named 'references'. 
 * They go from the "QUEUED", "ACTIVE" and "PROCESSED" stages.</p>
 * 
 * <p>The cached references are stored in a separated collection named 
 * "cached".</p>
 * 
 * <p>
 * Implementing classes should contain the following XML configuration usage:
 * </p>
 * <pre>
 *  &lt;crawlDataStoreFactory class="(class name)"&gt;
 *      &lt;host&gt;(Optional Mongo server hostname. Default to localhost)&lt;/host&gt;
 *      &lt;port&gt;(Optional Mongo port. Default to 27017)&lt;/port&gt;
 *      &lt;dbname&gt;(Optional Mongo database name. Default to crawl id)&lt;/dbname&gt;
 *      &lt;username&gt;(Optional user name)&lt;/username&gt;
 *      &lt;password&gt;(Optional user password)&lt;/password&gt;
 *  &lt;/crawlDataStoreFactory&gt;
 * </pre>
 * <p>
 * If "username" is not provided, no authentication will be attempted. 
 * The "username" must be a valid user that has the "readWrite" role over 
 * the database (set with "dbname").
 * </p>
 *
 * @author Pascal Essiembre
 * @see BasicMongoSerializer
 */
public abstract class AbstractMongoCrawlDataStoreFactory 
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private static final long serialVersionUID = -919499699528485232L;

    private final MongoConnectionDetails connDetails = 
            new MongoConnectionDetails();
    
    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        
        return new MongoCrawlDataStore(
                config.getId(),
                resume,
                getConnectionDetails(),
                createMongoSerializer());
    }

    public MongoConnectionDetails getConnectionDetails() {
        return connDetails;
    }
    
    protected abstract IMongoSerializer createMongoSerializer();
    
    @Override
    public void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        connDetails.setPort(xml.getInt("port", connDetails.getPort()));
        connDetails.setHost(xml.getString("host", connDetails.getHost()));
        connDetails.setDatabaseName(
                xml.getString("dbname", connDetails.getDatabaseName()));
        connDetails.setUsername(
                xml.getString("username", connDetails.getUsername()));
        connDetails.setPassword(
                xml.getString("password", connDetails.getPassword()));
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawlDataStoreFactory");
            writer.writeAttribute("class", getClass().getCanonicalName());

            writer.writeElementInteger("port", connDetails.getPort());
            writer.writeElementString("host", connDetails.getHost());
            writer.writeElementString("dbname", connDetails.getDatabaseName());
            writer.writeElementString("username", connDetails.getUsername());
            writer.writeElementString("password", connDetails.getPassword());

            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
        
    }
}
