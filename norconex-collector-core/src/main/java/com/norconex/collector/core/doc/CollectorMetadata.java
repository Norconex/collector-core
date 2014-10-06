/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.doc;

import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterMetadata;

/**
 * Collector metadata with constants for common metadata field
 * names.
 * @author Pascal Essiembre
 */
public class CollectorMetadata extends ImporterMetadata {

    private static final long serialVersionUID = -562425360774678869L;

    public static final String COLLECTOR_PREFIX = "collector.";

    public static final String COLLECTOR_CONTENT_ENCODING = 
            COLLECTOR_PREFIX + "content-encoding";
    public static final String COLLECTOR_CONTENT_TYPE = 
            COLLECTOR_PREFIX + "content-type";
    public static final String COLLECTOR_CHECKSUM_META = 
            COLLECTOR_PREFIX + "checksum-metadata";
    public static final String COLLECTOR_CHECKSUM_DOC = 
            COLLECTOR_PREFIX + "checksum-doc";
    
    public CollectorMetadata() {
        super(false);
    }
    public CollectorMetadata(Properties metadata) {
        super(metadata, false);
    }
}
