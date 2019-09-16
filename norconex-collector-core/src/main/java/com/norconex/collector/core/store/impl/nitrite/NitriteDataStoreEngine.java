/* Copyright 2019 Norconex Inc.
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
package com.norconex.collector.core.store.impl.nitrite;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.reference.CrawlState;
import com.norconex.collector.core.store.DataStoreException;
import com.norconex.collector.core.store.IDataStore;
import com.norconex.collector.core.store.IDataStoreEngine;
import com.norconex.commons.lang.file.ContentType;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

public class NitriteDataStoreEngine //extends CrawlerLifeCycleListener
        implements IDataStoreEngine, IXMLConfigurable {

    private static final Logger LOG =
            LoggerFactory.getLogger(NitriteDataStoreEngine.class);

    private final NitriteDataStoreConfig cfg = new NitriteDataStoreConfig();

    private Path storeDir;
    private Nitrite db;

    @Override
    public void init(Crawler crawler) {
        if (db != null) {
            throw new IllegalStateException(
                    "NitriteDataStore already initialized. "
                  + "Close existing instance before initializing it again.");
        }

        //TODO get path from store config or will it break having all artifacts
        // under same dir?
        storeDir = crawler.getWorkDir().resolve("datastore");
        try {
            FileUtils.forceMkdir(storeDir.toFile());
        } catch (IOException e) {
            throw new DataStoreException(
                    "Cannot create data store directory: " + storeDir, e);
        }

        NitriteBuilder builder = Nitrite.builder();
        if (cfg.getAutoCommitBufferSize() != null) {
            builder.autoCommitBufferSize(cfg.getAutoCommitBufferSize());
        }
        if (Boolean.TRUE.equals(cfg.getCompress())) {
            builder.compressed();
        }
        if (Boolean.TRUE.equals(cfg.getDisableAutoCommit())) {
            builder.disableAutoCommit();
        }
        if (Boolean.TRUE.equals(cfg.getDisableAutoCompact())) {
            builder.disableAutoCompact();
        }
        if (Boolean.TRUE.equals(cfg.getDisableShutdownHook())) {
            builder.disableShutdownHook();
        }
        builder.filePath(
                storeDir.resolve("nitrite").toAbsolutePath().toString());
        builder.registerModule(new ValueOfToStringModule(
                ContentType.class, CrawlState.class));
        db = builder.openOrCreate();
        LOG.debug("Nitrite data store engine initialized.");
    }


    @Override
    public boolean clean() {
        Path dirToDelete = storeDir;
        Set<String> names = getStoreNames();
        boolean hadStores = false;
        if (!names.isEmpty()) {
            hadStores = true;
            names.stream().forEach(
                    name -> db.getCollection(name).drop());
            close();
        }
        try {
            FileUtils.deleteDirectory(dirToDelete.toFile());
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not delete crawl store directory.", e);
        }
        return hadStores;
    }
    @Override
    public void close() {
        db.close();
        db = null;
        storeDir = null;
        LOG.debug("Nitrite data store engine closed.");
    }
    @Override
    public boolean dropStore(String name) {
        if (db.hasCollection(name)) {
            db.getCollection(name).drop();
            return true;
        }
        return false;
    }
    @Override
    public Set<String> getStoreNames() {
        return db.listCollectionNames();
    }

    @Override
    public <T> IDataStore<T> openStore(String name, Class<T> type) {
        return new NitriteDataStore<>(db, name, type);
    }

    @Override
    public void loadFromXML(XML xml) {
        cfg.setAutoCommitBufferSize(xml.getInteger(
                "autoCommitBufferSize", cfg.getAutoCommitBufferSize()));
        cfg.setCompress(xml.getBoolean("compress", cfg.getCompress()));
        cfg.setDisableAutoCommit(xml.getBoolean(
                "disableAutoCommit", cfg.getDisableAutoCommit()));
        cfg.setDisableAutoCompact(xml.getBoolean(
                "disableAutoCompact", cfg.getDisableAutoCompact()));
        cfg.setDisableShutdownHook(xml.getBoolean(
                "disableShutdownHook", cfg.getDisableShutdownHook()));
    }
    @Override
    public void saveToXML(XML xml) {
        xml.addElement("autoCommitBufferSize", cfg.getAutoCommitBufferSize());
        xml.addElement("compress", cfg.getCompress());
        xml.addElement("disableAutoCommit", cfg.getDisableAutoCommit());
        xml.addElement("disableAutoCompact", cfg.getDisableAutoCompact());
        xml.addElement("disableShutdownHook", cfg.getDisableShutdownHook());
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
