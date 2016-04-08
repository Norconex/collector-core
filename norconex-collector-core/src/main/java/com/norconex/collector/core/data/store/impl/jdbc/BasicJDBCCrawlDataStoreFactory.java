/* Copyright 2016 Norconex Inc.
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

import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.impl.jdbc.AbstractJDBCDataStoreFactory;
import com.norconex.collector.core.data.store.impl.jdbc.BasicJDBCSerializer;
import com.norconex.collector.core.data.store.impl.jdbc.IJDBCSerializer;
import com.norconex.collector.core.data.store.impl.jdbc.JDBCCrawlDataStore.Database;

/**
 * JDBC implementation of {@link ICrawlDataStore}.  Defaults to H2 
 * database.
 * <br><br>
 * XML configuration usage:
 * <br><br>
 * <pre>
 *  &lt;crawlDataStoreFactory 
 *          class="com.norconex.collector.core.data.store.impl.jdbc.BasicJDBCCrawlDataStoreFactory"&gt;
 *      &lt;database&gt;[h2|derby]&lt;/database&gt;
 *  &lt;/crawlDataStoreFactory&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 * @since 1.5.0
 */
public class BasicJDBCCrawlDataStoreFactory 
        extends AbstractJDBCDataStoreFactory {

    public BasicJDBCCrawlDataStoreFactory() {
        super();
    }
    public BasicJDBCCrawlDataStoreFactory(Database database) {
        super(database);
    }

    @Override
    protected IJDBCSerializer createJDBCSerializer() {
        return new BasicJDBCSerializer();
    }
}

