/* Copyright 2013-2017 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mongo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.norconex.collector.core.data.store.impl.mongo.MongoUtil;

public class MongoUtilTest {

    @Test
    public void testGetDbNameOrGenerateDoGenarate() throws Exception {
        String id = "my-crawl";
        assertEquals(id, MongoUtil.getSafeDBName("", id));
    }

    @Test
    public void testGetDbNameOrGenerateDoGenerateAndReplace()
            throws Exception {
        String id = "my crawl";
        // Whitespace should be replaced with '_'
        assertEquals("my_crawl", MongoUtil.getSafeDBName("", id));
    }

    @Test
    public void testGetDbNameOrGenerateInvalidName() throws Exception {
        // Tests some of the invalid characters
        checkInvalidName("invalid.name");
        checkInvalidName("invalid$name");
        checkInvalidName("invalid/name");
        checkInvalidName("invalid:name");
        checkInvalidName("invalid name");
    }

    @Test
    public void testTruncateWithHash() {
        String text = "I am a string with 28 chars.";

        // Test no truncate needed
        assertEquals(text, MongoUtil.truncateWithHash(text, 30));
        // Test no truncate needed equal size
        assertEquals(text, MongoUtil.truncateWithHash(text, 28));

        // Test truncate needed
        assertEquals("I am a string w!" + "ith 28 chars.".hashCode(), 
                MongoUtil.truncateWithHash(text, 15));
    }

    
    private void checkInvalidName(String name) {
        try {
            MongoUtil.getSafeDBName(name, null);
            fail("Should throw an IllegalArgumentException "
                    + "because the name is invalid");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}
