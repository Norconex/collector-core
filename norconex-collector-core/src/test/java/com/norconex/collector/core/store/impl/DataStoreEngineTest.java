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
package com.norconex.collector.core.store.impl;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.norconex.collector.core.MockCollector;
import com.norconex.collector.core.MockCollectorConfig;
import com.norconex.collector.core.crawler.MockCrawler;
import com.norconex.collector.core.crawler.MockCrawlerConfig;
import com.norconex.collector.core.doc.CrawlDocInfo.Stage;
import com.norconex.collector.core.store.IDataStore;
import com.norconex.collector.core.store.IDataStoreEngine;
import com.norconex.collector.core.store.impl.nitrite.NitriteDataStoreEngine;
import com.norconex.commons.lang.EqualsUtil;
import com.norconex.commons.lang.file.ContentType;

public class DataStoreEngineTest {

    // Not using @TempDir as it fails on windows sometimes (OS lock issue)
    private Path tempFolder;
    private TestObject obj;

    @BeforeEach
    public void setup() throws IOException {
        tempFolder = Files.createTempDirectory("nitrite");
        // Test pojo record
        obj = new TestObject();
        obj.setReference("areference");
        obj.setCount(66);
        obj.setContentChecksum("checksumvalue");
        obj.setParentRootReference("parentReference");
        obj.setContentType(ContentType.TEXT);
        obj.setProcessingStage(Stage.PROCESSED);
        obj.setValid(true);
    }
    @AfterEach
    public void tearDown() throws IOException {
        tempFolder.toFile().deleteOnExit();
    }

    @DataStoreTest
    public void testFindById(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            TestObject newPojo = store.findById("areference").get();
            Assertions.assertEquals(obj, newPojo);
        });
    }
    @DataStoreTest
    public void testFindBy(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(obj, store.findFirstBy(
                    "contentChecksum", "checksumvalue").get());
            Assertions.assertEquals(obj, store.findFirstBy("count", 66).get());
            Assertions.assertFalse(store.findFirstBy(
                    "parentRootReference", "none").isPresent());
        });
    }
    @DataStoreTest
    public void testFindFirstBy(IDataStoreEngine f) throws IOException {
        // Saving two entries, only the first one should be returned
        savePojo(f, obj);
        obj.setReference("breference");
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals("areference", store.findFirstBy(
                    "contentChecksum", "checksumvalue").get().getReference());
            Assertions.assertEquals("areference", store.findFirstBy(
                    "count", 66).get().getReference());
        });
    }
    @DataStoreTest
    public void testFindAll(IDataStoreEngine f) throws IOException {
        // Saving two entries, make sure they are retreived
        savePojo(f, obj);
        TestObject obj2 = new TestObject("breference", 67, "blah", "ipsum");
        savePojo(f, obj2);
        inNewStoreSession(f, (store) -> {
            for (TestObject p : store.findAll()) {
                Assertions.assertTrue(EqualsUtil.equalsAny(p, obj, obj2));
            }
        });
    }

    @DataStoreTest
    public void testExistsById(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertTrue(store.existsById("areference"));
            Assertions.assertFalse(store.existsById("breference"));
        });
    }
    @DataStoreTest
    public void testExistsBy(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertTrue(
                    store.existsBy("contentChecksum", "checksumvalue"));
            Assertions.assertFalse(store.existsBy("contentChecksum", "nope"));
        });
    }

    @DataStoreTest
    public void testCount(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        // each test start with 1
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(1, store.count());
        });
        // add another one and try again
        obj.setReference("breference");
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(2, store.count());
        });
    }
    @DataStoreTest
    public void testCountBy(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(
                    1, store.countBy("contentChecksum", "checksumvalue"));
            Assertions.assertEquals(
                    0, store.countBy("parentRootReference", "none"));
        });
        // add another one and try again
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(f, obj);

        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(
                    2, store.countBy("contentChecksum", "checksumvalue"));
            Assertions.assertEquals(1, store.countBy("count", 66));
            Assertions.assertEquals(1, store.countBy("count", 67));
        });
    }

    @DataStoreTest
    public void testDeleteById(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(1, store.count());
            Assertions.assertTrue(store.deleteById(obj.getReference()));
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(0, store.count());
        });
    }
    @DataStoreTest
    public void testDeleteBy(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        // add 2nd:
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(f, obj);
        // add 3rd:
        obj.setReference("creference");
        obj.setCount(67);
        savePojo(f, obj);

        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(3, store.count());
            Assertions.assertEquals(2, store.deleteBy("count", 67));
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(1, store.count());
        });
    }

    @DataStoreTest
    public void testModifyById(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        inNewStoreSession(f, (store) -> {
            Assertions.assertTrue(
                    store.modifyById(obj.getReference(), "count", 99));
            Assertions.assertEquals(1, store.count());
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(
                    99, store.findById(obj.getReference()).get().getCount());
        });
    }
    @DataStoreTest
    public void testModifyBy4Args(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        // add 2nd:
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(f, obj);
        // add 3rd:
        obj.setReference("creference");
        obj.setCount(67);
        savePojo(f, obj);

        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(3, store.count());
            Assertions.assertEquals(2,
                    store.modifyBy("count", 67, "contentChecksum", "newVal"));
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals("checksumvalue",
                    store.findById("areference").get().getContentChecksum());
            Assertions.assertEquals("newVal",
                    store.findById("breference").get().getContentChecksum());
            Assertions.assertEquals("newVal",
                    store.findById("creference").get().getContentChecksum());
        });
    }
    @DataStoreTest
    public void testModifyBy3Args(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        // add 2nd:
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(f, obj);
        // add 3rd:
        obj.setReference("creference");
        obj.setCount(67);
        savePojo(f, obj);

        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(3, store.count());
            Assertions.assertEquals(2, store.modifyBy("count", 67, 99));
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(
                    66, store.findById("areference").get().getCount());
            Assertions.assertEquals(
                    99, store.findById("breference").get().getCount());
            Assertions.assertEquals(
                    99, store.findById("creference").get().getCount());
        });
    }

    @DataStoreTest
    public void testClear(IDataStoreEngine f) throws IOException {
        savePojo(f, obj);
        // add 2nd:
        obj.setReference("breference");
        obj.setCount(67);
        savePojo(f, obj);

        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(2, store.clear());
        });
        inNewStoreSession(f, (store) -> {
            Assertions.assertEquals(0, store.count());
        });
    }

    private void savePojo(IDataStoreEngine f, TestObject testPojo)
            throws IOException {
        inNewStoreSession(f, (store) -> {
            store.save(testPojo);
        });
    }


    private void inNewStoreSession(
            IDataStoreEngine engine, Consumer<IDataStore<TestObject>> c) {
        MockCollectorConfig collConfig = new MockCollectorConfig();
        collConfig.setWorkDir(tempFolder);
        MockCollector coll = new MockCollector(collConfig);
        MockCrawlerConfig crawlConfig = new MockCrawlerConfig();
        crawlConfig.setDataStoreEngine(engine);
        MockCrawler crawler = new MockCrawler(crawlConfig, coll);
        crawler.initMockCrawler();
        try (IDataStore<TestObject> store =
                engine.openStore("testStore", TestObject.class)) {
            c.accept(store);
        } finally {
            engine.close();
        }
    }

    static Stream<IDataStoreEngine> dataStoreEngineProvider() {
        return Stream.of(
                new NitriteDataStoreEngine()
        );
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ParameterizedTest(name = "dataStoreEngine: {0}")
    @MethodSource("dataStoreEngineProvider")
    @interface DataStoreTest {
    }
}
