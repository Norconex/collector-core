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
            BaseCrawlData crawlData, ImporterDocument document) {
        super(crawler, crawlDataStore, crawlData);
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
