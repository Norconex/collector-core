/* Copyright 2014-2015 Norconex Inc.
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
package com.norconex.collector.core.doc;

import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterMetadata;

/**
 * Collector metadata with constants for common metadata field
 * names. Keys are case insensitive.
 * @author Pascal Essiembre
 */
public class CollectorMetadata extends ImporterMetadata {

    private static final long serialVersionUID = -562425360774678869L;

    public static final String COLLECTOR_PREFIX = "collector.";

    public static final String COLLECTOR_CONTENT_ENCODING = 
            COLLECTOR_PREFIX + "content-encoding";
    public static final String COLLECTOR_CONTENT_TYPE = 
            COLLECTOR_PREFIX + "content-type";
    public static final String COLLECTOR_CHECKSUM_METADATA = 
            COLLECTOR_PREFIX + "checksum-metadata";
    public static final String COLLECTOR_CHECKSUM_DOC = 
            COLLECTOR_PREFIX + "checksum-doc";
    
    public CollectorMetadata() {
        super(true);
    }
    public CollectorMetadata(Properties metadata) {
        super(metadata, true);
    }
}
