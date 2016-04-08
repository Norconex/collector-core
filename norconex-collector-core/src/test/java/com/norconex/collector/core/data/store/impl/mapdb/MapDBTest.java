/* Copyright 2010-2016 Norconex Inc.
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.ICrawlData;

public class MapDBTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private DB db;

    private Map<String, ICrawlData> map;

    private File dbFile;

    @Before
    public void setUp() throws Exception {
        dbFile = tempFolder.newFile();
        initDB(dbFile);
        map = db.createHashMap("test").counterEnable().make();
    }

    private void initDB(File file) throws IOException {
        db = DBMaker.newFileDB(file).make();
    }

    @After
    public void tearDown() throws Exception {
        db.close();
    }

    private ICrawlData buildCrawlURL() {
        BaseCrawlData c = new BaseCrawlData("http://www.example.com");
        c.setContentChecksum("contentChecksum");
        c.setMetaChecksum("metaChecksum");
        return c;
    }

    /**
     * Simple test that inserts an url into a new map
     * @throws Exception something went wrong
     */
    @Test
    public void createDatabaseTest() throws Exception {
        ICrawlData c = buildCrawlURL();
        map.put("url1", c);
        assertEquals(1, map.size());
    }

    /**
     * Test that inserts an url in the map, close the DB, re-open the DB (with
     * same file) and re-open the map and check the content is still there.
     * @throws Exception something went wrong
     */
    @Test
    public void loadDatabaseTest() throws Exception {
        // Insert test data
        ICrawlData c = buildCrawlURL();
        map.put("url1", c);

        // Close DB
        db.commit();
        db.close();

        // Re-open DB and map
        initDB(dbFile);
        map = db.getHashMap("test");

        // Check content
        assertEquals(1, map.size());
        c = map.get("url1");
        Assert.assertEquals("contentChecksum",  c.getContentChecksum());
        Assert.assertEquals("metaChecksum",  c.getMetaChecksum());
    }
}
