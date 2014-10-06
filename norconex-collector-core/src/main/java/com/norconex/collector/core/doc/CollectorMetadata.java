/**
 * 
 */
package com.norconex.collector.core.doc;

import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterMetadata;

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
