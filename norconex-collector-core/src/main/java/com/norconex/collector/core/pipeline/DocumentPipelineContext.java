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
package com.norconex.collector.core.pipeline;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.CharEncoding;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;
import com.norconex.importer.doc.ImporterDocument;

/**
 * {@link IPipelineStage} context for collector {@link Pipeline}s dealing with 
 * an {@link ImporterDocument}.
 * @author Pascal Essiembre
 */
public class DocumentPipelineContext extends BasePipelineContext {

    private final ImporterDocument document;
    
    public DocumentPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData,
            BaseCrawlData cachedCrawlData,
            ImporterDocument document) {
        super(crawler, crawlDataStore, crawlData, cachedCrawlData);
        this.document = document;
    }

    public ImporterDocument getDocument() {
        return document;
    }

    public CachedInputStream getContent() {
        return getDocument().getContent();
    }

    public Reader getContentReader() {
        try {
            return new InputStreamReader(
                    getDocument().getContent(), 
                    CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new CollectorException(e);
        }
    }    
}
