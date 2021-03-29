/* Copyright 2019-2021 Norconex Inc.
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
package com.norconex.collector.core.store;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.MockCollector;
import com.norconex.collector.core.MockCollectorConfig;
import com.norconex.collector.core.crawler.MockCrawler;
import com.norconex.collector.core.crawler.MockCrawlerConfig;
import com.norconex.commons.lang.EqualsUtil;
import com.norconex.commons.lang.file.ContentType;

// Uses inheritance instead of parameterized test because we need to
// disable some store engine in some environments and report them as skipped.
public abstract class AbstractDataStoreEngineTest {

    private static final Logger LOG =
            LoggerFactory.getLogger(AbstractDataStoreEngineTest.class);

    protected static final String TEST_STORE_NAME = "testStore";

    @TempDir
    public Path tempFolder;

    private TestObject obj;

    @BeforeEach
    protected void beforeEach() throws IOException {
        // Test pojo record
        obj = new TestObject();
        obj.setReference("areference");
        obj.setCount(66);
        obj.setContentChecksum("checksumvalue");
        obj.setParentRootReference("parentReference");
        obj.setContentType(ContentType.TEXT);
        obj.setValid(true);

        // Delete any data so tests start on a clean slate.
        inNewStoreSession((store) -> {
            store.clear();
        });
    }

    protected abstract IDataStoreEngine createEngine();

    @Test
    void testFind() {
        savePojo(obj);
        inNewStoreSession((store) -> {
            TestObject newPojo = store.find("areference").get();
            Assertions.assertEquals(obj, newPojo);
        });
    }

    @Test
    void testFindFirst() {
        savePojo(obj);
        TestObject obj2 = new TestObject("breference", 67, "blah", "ipsum");
        savePojo(obj2);
        inNewStoreSession((store) -> {
            TestObject newPojo = store.findFirst().get();
            Assertions.assertEquals(obj, newPojo);
        });
    }

    @Test
    void testForEach() {
        // Saving two entries, make sure they are retreived
        savePojo(obj);
        TestObject obj2 = new TestObject("breference", 67, "blah", "ipsum");
        savePojo(obj2);
        inNewStoreSession((store) -> {
            store.forEach((k, v) -> {
                Assertions.assertTrue(EqualsUtil.equalsAny(v, obj, obj2));
                return true;
            });
        });
    }

    @Test
    void testExists() {
        savePojo(obj);
        inNewStoreSession((store) -> {
            Assertions.assertTrue(store.exists("areference"));
            Assertions.assertFalse(store.exists("breference"));
        });
    }

    @Test
    void testCount() {
        savePojo(obj);
        // each test start with 1
        inNewStoreSession((store) -> {
            Assertions.assertEquals(1, store.count());
        });
        // add another one and try again
        obj.setReference("breference");
        savePojo(obj);
        inNewStoreSession((store) -> {
            Assertions.assertEquals(2, store.count());
        });
    }

    @Test
    void testDelete() {
        savePojo(obj);
        inNewStoreSession((store) -> {
            Assertions.assertEquals(1, store.count());
            Assertions.assertTrue(store.delete(obj.getReference()));
        });
        inNewStoreSession((store) -> {
            Assertions.assertEquals(0, store.count());
        });
    }

    @Test
    void testModify() {
        // 1st save:
        savePojo(obj);
        // 2nd save:
        obj.setCount(67);
        obj.setContentChecksum("newVal2");
        savePojo(obj);
        // 3rd save:
        obj.setCount(67);
        obj.setContentChecksum("newVal3");
        savePojo(obj);

        inNewStoreSession((store) -> {
            Assertions.assertEquals(1, store.count());
        });
        inNewStoreSession((store) -> {
            Assertions.assertEquals("newVal3",
                    store.find("areference").get().getContentChecksum());
        });
    }

    @Test
    void testInstanceModifyId() {
        // 1st save:
        savePojo(obj);
        // 2nd save:
        obj.setReference("breference");
        obj.setCount(67);
        obj.setContentChecksum("newVal");
        savePojo(obj);
        // 3rd save:
        obj.setReference("creference");
        obj.setCount(67);
        obj.setContentChecksum("newVal");
        savePojo(obj);

        inNewStoreSession((store) -> {
            Assertions.assertEquals(3, store.count());
        });
        inNewStoreSession((store) -> {
            Assertions.assertEquals("checksumvalue",
                    store.find("areference").get().getContentChecksum());
            Assertions.assertEquals("newVal",
                    store.find("breference").get().getContentChecksum());
            Assertions.assertEquals("newVal",
                    store.find("creference").get().getContentChecksum());
        });
    }

    @Test
    void testClear() {
        savePojo(obj);

        // add 2nd:
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(obj);

        inNewStoreSession((store) -> {
            Assertions.assertEquals(2, store.count());
            store.clear();
        });
        inNewStoreSession((store) -> {
            Assertions.assertEquals(0, store.count());
        });
    }

    private void savePojo(TestObject testPojo) {
        inNewStoreSession((store) -> {
            store.save(testPojo.getReference(), testPojo);
        });
    }

    private void inNewStoreSession(Consumer<IDataStore<TestObject>> c) {
        IDataStoreEngine engine = createEngine();
        MockCollectorConfig collConfig = new MockCollectorConfig();
        collConfig.setWorkDir(tempFolder.resolve("storeEngine"));
        MockCollector coll = new MockCollector(collConfig);
        MockCrawlerConfig crawlConfig = new MockCrawlerConfig();
        crawlConfig.setDataStoreEngine(engine);
        MockCrawler crawler = new MockCrawler(crawlConfig, coll);
        try {
            crawler.initMockCrawler();
            LOG.debug("Start data store test...");
            try (IDataStore<TestObject> store =
                    engine.openStore(TEST_STORE_NAME, TestObject.class)) {
                c.accept(store);
            }
            LOG.debug("Data store test done.");
        } finally {
            crawler.destroyMockCrawler();
        }
    }
}
