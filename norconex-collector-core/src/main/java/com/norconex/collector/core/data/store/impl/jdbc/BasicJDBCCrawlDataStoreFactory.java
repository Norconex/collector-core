/* Copyright 2016-2019 Norconex Inc.
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

import java.nio.file.Path;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

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
 * <pre>
 *  &lt;crawlDataStoreFactory
 *          class="com.norconex.collector.core.data.store.impl.jdbc.BasicJDBCCrawlDataStoreFactory"/&gt;
 * </pre>
 * @author Pascal Essiembre
 * @since 1.5.0
 */
public class BasicJDBCCrawlDataStoreFactory //extends CrawlerLifeCycleListener
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private Path storeDir;

    public BasicJDBCCrawlDataStoreFactory() {
        super();
    }

    protected IJDBCSerializer createJDBCSerializer() {
        return new BasicJDBCSerializer();
    }

    public Path getStoreDir() {
        return storeDir;
    }
    public void setStoreDir(Path storeDir) {
        this.storeDir = storeDir;
    }
//
//    @Override
//    protected void crawlerStartup(XMLValidationEvent<Crawler> event) {
//        if (storeDir == null) {
//            storeDir = event.getSource().getWorkDir().resolve(
//                    "/crawlstore-jdbc");
//        }
//    }
//    @Override
//    protected void crawlerShutdown(XMLValidationEvent<Crawler> event) {
//    }

    @Override
    public ICrawlDataStore createCrawlDataStore(
            CrawlerConfig config, boolean resume) {

        if (storeDir == null) {
            storeDir = Crawler.get().getWorkDir().resolve("crawlstore-jdbc");
        }
//        String storeDir = config.getWorkDir().toString() + "/crawlstore/jdbc/"
//                + FileUtil.toSafeFileName(config.getId()) + "/";
        return new JDBCCrawlDataStore(
                 storeDir.toString(), resume, createJDBCSerializer());
    }

    @Override
    public void loadFromXML(XML xml) {
        //NOOP
    }

    @Override
    public void saveToXML(XML xml) {
        //NOOP
//        try {
//            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
//            writer.writeStartElement("crawlDataStoreFactory");
//            xml.setAttribute("class", getClass().getCanonicalName());
//            writer.flush();
//            writer.close();
//        } catch (XMLStreamException e) {
//            throw new IOException("Cannot save as XML.", e);
//        }
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

