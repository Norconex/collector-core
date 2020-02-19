/* Copyright 2020 Norconex Inc.
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

import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.importer.doc.DocInfo;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.doc.ImporterMetadata;

//TODO consider dropping since it just brings HttpMetadata cast.

//TODO forcing to pass COLLECTOR_URL that way is best?
//TODO rename CrawlDoc to CrawlerDocument (Same for metadata)
public class CrawlDoc extends ImporterDocument {

    public CrawlDoc(DocInfo docDetails, CachedInputStream content,
            ImporterMetadata metadata) {
        super(docDetails, content, metadata);
    }

    public CrawlDoc(DocInfo docDetails, CachedInputStream content) {
        super(docDetails, content);
    }

//    public CrawlDoc(CrawlDocInfo docDetails,
//            CachedStreamFactory streamFactory, ImporterMetadata metadata) {
//        super(docDetails, streamFactory, metadata);
//    }
//
//    public CrawlDoc(CrawlDocInfo docDetails,
//            CachedStreamFactory streamFactory) {
//        super(docDetails, streamFactory);
//    }

//    @Override
//    public CrawlDocInfo getDocInfo() {
//        return (CrawlDocInfo) super.getDocInfo();
//    }

//    @Override
//    public CrawlDocMetadata getMetadata() {
//        return (CrawlDocMetadata) super.getMetadata();
//    }


}
