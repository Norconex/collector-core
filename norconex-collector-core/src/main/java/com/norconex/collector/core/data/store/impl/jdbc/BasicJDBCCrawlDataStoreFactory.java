/* Copyright 2016-2017 Norconex Inc.
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

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * <p>
 * JDBC implementation of {@link ICrawlDataStore} using H2 database.
 * </p>
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;crawlDataStoreFactory 
 *          class="com.norconex.collector.core.data.store.impl.jdbc.BasicJDBCCrawlDataStoreFactory"/&gt;
 * </pre>
 * 
 * <h4>Usage example:</h4>
 * <p>
 * The following changes the default to use an H2 database.
 * </p> 
 * <pre>
 *  &lt;crawlDataStoreFactory 
 *          class="com.norconex.collector.core.data.store.impl.jdbc.BasicJDBCCrawlDataStoreFactory"/&gt;
 * </pre>
 * @author Pascal Essiembre
 * @since 1.5.0
 */
public class BasicJDBCCrawlDataStoreFactory 
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    public BasicJDBCCrawlDataStoreFactory() {
        super();
    }

    protected IJDBCSerializer createJDBCSerializer() {
        return new BasicJDBCSerializer();
    }
    
    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        String storeDir = config.getWorkDir().getPath() + "/crawlstore/jdbc/" 
                + FileUtil.toSafeFileName(config.getId()) + "/";
        return new JDBCCrawlDataStore(
                storeDir, resume, createJDBCSerializer());
    }

    @Override
    public void loadFromXML(Reader in) throws IOException {
        // NOOP
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawlDataStoreFactory");
            writer.writeAttribute("class", getClass().getCanonicalName());
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
    }
    
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BasicJDBCCrawlDataStoreFactory)) {
            return false;
        }
        BasicJDBCCrawlDataStoreFactory castOther = 
                (BasicJDBCCrawlDataStoreFactory) other;
        return new EqualsBuilder()
                .append(getClass(), castOther.getClass())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getClass())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .toString();
    }    
}

