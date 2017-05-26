/* Copyright 2017 Norconex Inc.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.importer.handler.filter.OnMatch;

public class RegexReferenceFilterTest {

    @Test
    public void testCaseSensitivity() throws IOException {
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
    public void testWriteRead() throws IOException {
        RegexReferenceFilter f = new RegexReferenceFilter();
        f.setCaseSensitive(true);
        f.setRegex(".*blah.*");
        f.setOnMatch(OnMatch.EXCLUDE);
        System.out.println("Writing/Reading this: " + f);
        XMLConfigurationUtil.assertWriteRead(f);
    }
}
