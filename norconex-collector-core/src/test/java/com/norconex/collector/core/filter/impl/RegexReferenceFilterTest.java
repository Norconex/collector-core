/* Copyright 2017-2020 Norconex Inc.
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
package com.norconex.collector.core.filter.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.handler.filter.OnMatch;

public class RegexReferenceFilterTest {

    @Test
    public void testCaseSensitivity() {
        RegexReferenceFilter f = new RegexReferenceFilter();
        f.setOnMatch(OnMatch.INCLUDE);
        f.setRegex("case");

        // must match any case:
        f.setCaseSensitive(false);
        assertTrue(f.acceptReference("case"));
        assertTrue(f.acceptReference("CASE"));

        // must match only matching case:
        f.setCaseSensitive(true);
        assertTrue(f.acceptReference("case"));
        assertFalse(f.acceptReference("CASE"));
    }


    @Test
    public void testWriteRead() {
        RegexReferenceFilter f = new RegexReferenceFilter();
        f.setCaseSensitive(true);
        f.setRegex(".*blah.*");
        f.setOnMatch(OnMatch.EXCLUDE);
        XML.assertWriteRead(f, "filter");
    }
}
