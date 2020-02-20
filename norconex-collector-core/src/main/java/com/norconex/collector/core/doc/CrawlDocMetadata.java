/* Copyright 2014-2020 Norconex Inc.
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

import com.norconex.importer.doc.DocMetadata;

/**
 * Metadata constants for common metadata field
 * names typically set by a collector crawler.
 * @author Pascal Essiembre
 * @see DocMetadata
 */
public final class CrawlDocMetadata {

    public static final String PREFIX = "collector.";

    //TODO really keep those found in DocMetadata?
    //TODO really use these constants when fields on DocInfo should be used?
    public static final String CONTENT_ENCODING =
            PREFIX + "content-encoding";
    public static final String CONTENT_TYPE =
            PREFIX + "content-type";
    public static final String CHECKSUM_METADATA =
            PREFIX + "checksum-metadata";
    public static final String CHECKSUM_DOC =
            PREFIX + "checksum-doc";

//    /**
//     * A document ACL if ACL extraction is supported.
//     * @since 1.10.0
//     */
//    public static final String COLLECTOR_ACL = PREFIX + "acl";

    /**
     * Boolean flag indicating whether a document is new to the crawler that
     * fetched it.
     * That is, a URL cache from a previous run exists and the document was
     * not found in that cache. If the crawler runs for the first time
     * or its URL cache has been deleted, this flag will always be
     * <code>true</code>.
     * @since 1.3.0
     */
    public static final String IS_CRAWL_NEW =
            PREFIX + "is-crawl-new";

    private CrawlDocMetadata() {
        super();
    }
}
