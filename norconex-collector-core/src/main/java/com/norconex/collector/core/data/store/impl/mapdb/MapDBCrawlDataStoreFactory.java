/* Copyright 2010-2014 Norconex Inc.
 * 
 * This file is part of Norconex Filesystem Collector.
 * 
 * Norconex Filesystem Collector is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Filesystem Collector is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Filesystem Collector. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.data.store.impl.mapdb;

import org.mapdb.Serializer;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;

/**
 * Default reference store factory.
 * 
 * @author Pascal Essiembre
 */
public class MapDBCrawlDataStoreFactory 
        implements ICrawlDataStoreFactory {

    private static final long serialVersionUID = 197714845943448133L;
    
    private Serializer<ICrawlData> valueSerializer;
    
    public MapDBCrawlDataStoreFactory() {
        this(null);
    }
    public MapDBCrawlDataStoreFactory(Serializer<ICrawlData> valueSerializer) {
        super();
        this.valueSerializer = valueSerializer;
    }

    public Serializer<ICrawlData> getValueSerializer() {
        return valueSerializer;
    }
    /**
     * @param valueSerializer the valueSerializer to set
     */
    public void setValueSerializer(Serializer<ICrawlData> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }
    

    @Override
    public ICrawlDataStore createReferenceStore(
            ICrawlerConfig config, boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/refstore/" + config.getId() + "/";
        return new MapDBCrawlDataStore(storeDir, resume, valueSerializer);
    }
    
    //TODO implement IXMLConfigurable? To set a custom serializer?
}
