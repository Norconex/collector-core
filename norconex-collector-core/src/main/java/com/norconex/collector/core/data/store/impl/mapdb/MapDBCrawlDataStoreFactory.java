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
package com.norconex.collector.core.data.store.impl.mapdb;

import org.mapdb.Serializer;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.ICrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.file.FileUtil;

/**
 * MapDB crawl data store factory 
 * (<a href="http://www.mapdb.org/">http://www.mapdb.org/</a>).
 * 
 * XML Configuration Usage: 
 * 
 * <pre>
 *  &lt;crawlDataStoreFactory 
 *          class="com.norconex.collector.core.data.store.impl.mapdb.MapDBCrawlDataStoreFactory" /&gt;
 * </pre>
 * 
 * @author Pascal Essiembre
 */
public class MapDBCrawlDataStoreFactory implements ICrawlDataStoreFactory {

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
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/crawlstore/mapdb/" 
                + FileUtil.toSafeFileName(config.getId()) + "/";
        return new MapDBCrawlDataStore(storeDir, resume, valueSerializer);
    }
    
    //TODO implement IXMLConfigurable? To set a custom serializer?
}
