/* Copyright 2014-2017 Norconex Inc.
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
package com.norconex.collector.core.pipeline.importer;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.commons.lang.pipeline.Pipeline;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.response.ImporterResponse;

/**
 * {@link IPipelineStage} context for collector {@link Pipeline}s dealing
 * with {@link ImporterResponse}.
 * @author Pascal Essiembre
 *
 */
public class ImporterPipelineContext extends DocumentPipelineContext {

    private ImporterResponse importerResponse;
    private boolean delete;
    private boolean orphan;

    /**
     * Constructor creating a copy of supplied context.
     * @param copiable the item to be copied
     * @since 1.9.0
     */
    public ImporterPipelineContext(ImporterPipelineContext copiable) {
        this(copiable.getCrawler(), copiable.getCrawlDataStore());
        try {
            BeanUtils.copyProperties(this, copiable);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CollectorException("Could not copy importer context.", e);
        }
    }
    
    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlDataStore crawl data store
     * @since 1.9.0
     */
    public ImporterPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore) {
        super(crawler, crawlDataStore, null);
    }
    /**
     * Constructor.
     * @param crawler the crawler
     * @param crawlDataStore crawl data store
     * @param crawlData current crawl data
     * @since 1.9.0
     */
    public ImporterPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData) {
        super(crawler, crawlDataStore, crawlData);
    }
    public ImporterPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData, BaseCrawlData cachedCrawlData, 
            ImporterDocument document) {
        super(crawler, crawlDataStore, crawlData, cachedCrawlData, document);
    }

    public ImporterResponse getImporterResponse() {
        return importerResponse;
    }
    public void setImporterResponse(ImporterResponse importerResponse) {
        this.importerResponse = importerResponse;
    }

    /**
     * Gets whether the document should be deleted.
     * @return <code>true</code> if should be deleted
     * @since 1.9.0
     */
    public boolean isDelete() {
        return delete;
    }
    /**
     * Sets whether the document should be deleted.
     * @param delete <code>true</code> if should be deleted
     * @since 1.9.0
     */
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    /**
     * Gets whether the document is an orphan (no longer referenced).
     * @return <code>true</code> is an orphan
     * @since 1.9.0
     */
    public boolean isOrphan() {
        return orphan;
    }
    /**
     * Sets whether the document is an orphan (no longer referenced).
     * @param orphan <code>true</code> is an orphan
     * @since 1.9.0
     */
    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }
}
