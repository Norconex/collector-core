/* Copyright 2014-2015 Norconex Inc.
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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MVStoreCrawlDataStoreFactory)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .toString();
    }
}
