/**
 * 
 */
package com.norconex.collector.core.pipeline.importer;

import com.norconex.collector.core.crawler.ICrawler;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.response.ImporterResponse;

/**
 * @author Pascal Essiembre
 *
 */
public class ImporterPipelineContext extends DocumentPipelineContext {

    private ImporterResponse importerResponse;
    
    public ImporterPipelineContext(
            ICrawler crawler, ICrawlDataStore crawlDataStore, 
            BaseCrawlData crawlData, ImporterDocument document) {
        super(crawler, crawlDataStore, crawlData, document);
    }

    public ImporterResponse getImporterResponse() {
        return importerResponse;
    }
    public void setImporterResponse(ImporterResponse importerResponse) {
        this.importerResponse = importerResponse;
    }

}
