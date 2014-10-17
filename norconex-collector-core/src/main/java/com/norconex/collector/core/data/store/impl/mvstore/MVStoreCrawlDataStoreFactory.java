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
package com.norconex.collector.core.data.store.impl.mvstore;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.file.FileUtil;

/**
 * H2 MVStore crawl data store factory 
 * (<a href="http://h2database.com/html/mvstore.html"
 * >http://h2database.com/html/mvstore.html</a>).
 * 
 * XML Configuration Usage: 
 * 
 * <pre>
 *  &lt;crawlDataStoreFactory 
 *          class="com.norconex.collector.core.data.store.impl.mvstore.MVStoreCrawlDataStoreFactory" /&gt;
 * </pre>
 * 
 * @author Pascal Dimassimo
 */
public class MVStoreCrawlDataStoreFactory implements ICrawlDataStoreFactory {

    @Override
    public ICrawlDataStore createCrawlDataStore(ICrawlerConfig config,
            boolean resume) {
        String storeDir = config.getWorkDir().getPath()
                + "/crawlstore/mvstore/"
                + FileUtil.toSafeFileName(config.getId()) + "/";
        return new MVStoreCrawlDataStore(storeDir, resume);
    }

}
