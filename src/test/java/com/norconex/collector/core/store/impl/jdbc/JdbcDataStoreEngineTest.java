/* Copyright 2021-2022 Norconex Inc.
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
package com.norconex.collector.core.store.impl.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.store.AbstractDataStoreEngineTest;
import com.norconex.collector.core.store.IDataStoreEngine;
import com.norconex.commons.lang.map.Properties;

public class JdbcDataStoreEngineTest extends AbstractDataStoreEngineTest {

    private static final Logger LOG =
            LoggerFactory.getLogger(JdbcDataStoreEngineTest.class);

    @Override
    protected IDataStoreEngine createEngine() {
        return Assertions.assertDoesNotThrow(() -> {
            String connStr = "jdbc:h2:file:" + StringUtils.removeStart(
                    tempFolder.toUri().toURL() + "test", "file:/");
            LOG.info("Creating new JDBC data store engine using: {}", connStr);
            JdbcDataStoreEngine engine = new JdbcDataStoreEngine();
            Properties cfg = new Properties();
            cfg.add("jdbcUrl", connStr);
            engine.setConfigProperties(cfg);
            return engine;
        });
    }
}
