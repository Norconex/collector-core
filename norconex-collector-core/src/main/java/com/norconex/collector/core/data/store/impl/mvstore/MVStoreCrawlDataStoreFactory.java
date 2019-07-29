/* Copyright 2014-2019 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mvstore;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.h2.mvstore.MVStore;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * <p>
 * H2 MVStore crawl data store factory
 * (<a href="http://h2database.com/html/mvstore.html"
 * >http://h2database.com/html/mvstore.html</a>).
 * </p>
 * <h3>Advanced MVStore configuration</h3>
 * <p>
 * Since 1.10.0, it possible to specify advanced MVStore configuration. It is
 * recommended you do not play with those unless you know what you are doing.
 * For information about these advanced options, have a look at methods offered
 * on {@link MVStore.Builder} or
 * <a href="https://github.com/h2database/h2database/blob/master/h2/src/main/org/h2/mvstore/MVStore.java#L3087">
 * its source</a>.
 * </p>
 *
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;crawlDataStoreFactory
 *          class="com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStoreFactory"&gt;
 *
 *    &lt;!-- For advanced MVStore configuration: --&gt;
 *    &lt;pageSplitSize&gt;(number)&lt;/pageSplitSize&gt;
 *    &lt;compress&gt;(number: 0=none; 1=normal; 2=high)&lt;/compress&gt;
 *    &lt;cacheConcurrency&gt;(number)&lt;/cacheConcurrency&gt;
 *    &lt;cacheSize&gt;(number)&lt;/cacheSize&gt;
 *    &lt;autoCompactFillRate&gt;(number)&lt;/autoCompactFillRate&gt;
 *    &lt;autoCommitBufferSize&gt;(number)&lt;/autoCommitBufferSize&gt;
 *    &lt;autoCommitDelay&gt;(number)&lt;/autoCommitDelay&gt;
 *  &lt;/crawlDataStoreFactory&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 */
public class MVStoreCrawlDataStoreFactory
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private final MVStoreConfig cfg = new MVStoreConfig();

    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/crawlstore/mvstore/"
                + FileUtil.toSafeFileName(config.getId()) + "/";
        return new MVStoreCrawlDataStore(storeDir, resume, cfg);
    }

    public MVStoreConfig getMVStoreConfig() {
        return cfg;
    }

    @Override
    public void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = XMLConfigurationUtil.newXMLConfiguration(in);
        cfg.setPageSplitSize(xml.getInteger(
                "pageSplitSize", cfg.getPageSplitSize()));
        cfg.setCompress(xml.getInteger("compress", cfg.getCompress()));
        cfg.setCacheConcurrency(xml.getInteger(
                "cacheConcurrency", cfg.getCacheConcurrency()));
        cfg.setCacheSize(xml.getInteger("cacheSize", cfg.getCacheSize()));
        cfg.setAutoCompactFillRate(xml.getInteger(
                "autoCompactFillRate", cfg.getAutoCompactFillRate()));
        cfg.setAutoCommitBufferSize(xml.getInteger(
                "autoCommitBufferSize", cfg.getAutoCommitBufferSize()));
        cfg.setAutoCommitDelay(xml.getInteger(
                "autoCommitDelay", cfg.getAutoCommitDelay()));
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawlDataStoreFactory");
            writer.writeAttribute("class", getClass().getCanonicalName());
            writer.writeElementInteger("pageSplitSize", cfg.getPageSplitSize());
            writer.writeElementInteger("compress", cfg.getCompress());
            writer.writeElementInteger(
                    "cacheConcurrency", cfg.getCacheConcurrency());
            writer.writeElementInteger("cacheSize", cfg.getCacheSize());
            writer.writeElementInteger(
                    "autoCompactFillRate", cfg.getAutoCompactFillRate());
            writer.writeElementInteger(
                    "autoCommitBufferSize", cfg.getAutoCommitBufferSize());
            writer.writeElementInteger(
                    "autoCommitDelay", cfg.getAutoCommitDelay());
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
