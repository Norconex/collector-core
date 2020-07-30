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
package com.norconex.collector.core.spoil.impl;

import org.junit.jupiter.api.Test;

import com.norconex.collector.core.doc.CrawlState;
import com.norconex.collector.core.spoil.SpoiledReferenceStrategy;
import com.norconex.commons.lang.xml.XML;


public class GenericSpoiledReferenceStrategizerTest  {


    @Test
    public void testWriteRead() {
        GenericSpoiledReferenceStrategizer s =
                new GenericSpoiledReferenceStrategizer();
        s.setFallbackStrategy(SpoiledReferenceStrategy.GRACE_ONCE);
        s.addMapping(CrawlState.MODIFIED, SpoiledReferenceStrategy.IGNORE);
        s.addMapping(CrawlState.BAD_STATUS, SpoiledReferenceStrategy.DELETE);
        XML.assertWriteRead(s, "spoiledReferenceStrategizer");
    }
}
