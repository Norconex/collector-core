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
package com.norconex.collector.core.data.store;

import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.data.ICrawlData;

/**
 * Abstract crawl data store.
 * @author Pascal Essiembre
 */
public abstract class AbstractCrawlDataStore implements ICrawlDataStore {

    @Override
    public final boolean isVanished(ICrawlData crawlData) {
        ICrawlData cachedReference = getCached(crawlData.getReference());
        if (cachedReference == null) {
            return false;
        }
        CrawlState current = crawlData.getState();
        CrawlState last = cachedReference.getState();
        return !current.isGoodState() && last.isGoodState();
    }


}
