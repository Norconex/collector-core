/**
 * 
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
import com.norconex.importer.doc.ImporterDocument;

/**
 * @author Pascal Essiembre
 *
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
