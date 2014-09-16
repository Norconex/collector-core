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
package com.norconex.collector.core.crawler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.doccrawl.store.IDocCrawlStoreFactory;
import com.norconex.collector.core.doccrawl.store.impl.mapdb.MapDBDocCrawlStoreFactory;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * Base Collector configuration.
 * @author Pascal Essiembre
 */
public abstract class AbstractCrawlerConfig implements ICrawlerConfig {

    private static final long serialVersionUID = -6935734360988586588L;

    private static final Logger LOG = LogManager.getLogger(
            AbstractCrawlerConfig.class);
    
    private String id;
    private File workDir = new File("./work");
    private IDocCrawlStoreFactory docCrawlStoreFactory = 
            new MapDBDocCrawlStoreFactory();
    
    
    /**
     * Creates a new crawler configuration.
     */
	public AbstractCrawlerConfig() {
        super();
    }

	/**
	 * Gets this crawler unique identifier.
	 * @return unique identifier
	 */
	@Override
    public String getId() {
        return id;
    }
    /**
     * Sets this crawler unique identifier. It is important
     * the id of the crawler is unique amongst your collector crawlers.  This
     * facilitates integration with different systems and facilitates
     * tracking.
     * @param id unique identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public File getWorkDir() {
        return workDir;
    }
    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    @Override
    public IDocCrawlStoreFactory getReferenceStoreFactory() {
        return docCrawlStoreFactory;
    }
    public void setReferenceStoreFactory(
            IDocCrawlStoreFactory docCrawlStoreFactory) {
        this.docCrawlStoreFactory = docCrawlStoreFactory;
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawler");
            writer.writeAttributeClass("class", getClass());
            writer.writeAttribute("id", getId());
            writer.flush();

            writeObject(out, "referenceStoreFactory", 
                    getReferenceStoreFactory());
            saveCrawlerConfigToXML(out);
            
            writer.writeEndElement();
            
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }   
        
    }
    private void writeObject(Writer out, String tagName, Object object) 
                throws IOException {
        if (object == null) {
            return;
        }
        if (object instanceof IXMLConfigurable) {
            ((IXMLConfigurable) object).saveToXML(out);
        } else {
            out.write("<" + tagName + " class=\"" 
                    + object.getClass().getCanonicalName() + "\" />");
        }
        out.flush();
    }
    protected abstract void saveCrawlerConfigToXML(Writer out)
            throws IOException;
    
    @Override
    public final void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        String crawlerId = xml.getString("[@id]", null);
        setId(crawlerId);
        
        setReferenceStoreFactory(ConfigurationUtil.newInstance(xml,
                "referenceStoreFactory", getReferenceStoreFactory()));

        
        loadCrawlerConfigFromXML(xml);

        if (LOG.isInfoEnabled()) {
            LOG.info("Crawler loaded: id=" + crawlerId);
        }
    }
    protected abstract void loadCrawlerConfigFromXML(XMLConfiguration xml)
            throws IOException;
}
