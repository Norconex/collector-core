/* Copyright 2010-2014 Norconex Inc.
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
package com.norconex.collector.core.ref.store.impl.mongo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.ref.store.IReferenceStore;
import com.norconex.collector.core.ref.store.IReferenceStoreFactory;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;

/**
 * Database factory creating a {@link MongoReferenceStore} instance.
 * @author Pascal Dimassimo
 */
public class MongoReferenceStoreFactory implements IReferenceStoreFactory,
        IXMLConfigurable {

    private static final long serialVersionUID = 2798011257427173733L;

    /** Default Mongo port. */
    public static final int DEFAULT_MONGO_PORT = 27017;
    
    private int port = DEFAULT_MONGO_PORT;
    private String host = "localhost";
    private String dbName;
    private String username;
    private String password;
    private IMongoReferenceConverter converter;

    @Override
    public IReferenceStore createReferenceStore(
            ICrawlerConfig config, boolean resume) {
        MongoConnectionDetails connDetails = new MongoConnectionDetails();
        connDetails.setPort(port);
        connDetails.setHost(host);
        connDetails.setDbName(dbName);
        connDetails.setUsername(username);
        connDetails.setPassword(password);
        return new MongoReferenceStore(config, resume, connDetails, converter);
    }

    @Override
    public void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        port = xml.getInt("port", port);
        host = xml.getString("host", host);
        dbName = xml.getString("dbname");
        username = xml.getString("username");
        password = xml.getString("password");
        converter = ConfigurationUtil.newInstance(xml, "converter");
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);

            writer.writeStartElement("port");
            writer.writeCharacters(String.valueOf(port));
            writer.writeEndElement();

            writer.writeStartElement("host");
            writer.writeCharacters(host);
            writer.writeEndElement();

            if (StringUtils.isNotBlank(dbName)) {
                writer.writeStartElement("dbname");
                writer.writeCharacters(dbName);
                writer.writeEndElement();
            }
            
            if (StringUtils.isNotBlank(username)) {
                writer.writeStartElement("username");
                writer.writeCharacters(username);
                writer.writeEndElement();
                
                writer.writeStartElement("password");
                writer.writeCharacters(password);
                writer.writeEndElement();
            }

            writer.writeStartElement("converter");
            writer.writeAttribute("class", 
                    converter.getClass().getCanonicalName());
            writer.writeEndElement();
            
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
    }
}
