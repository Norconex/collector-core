/**
 * 
 */
package com.norconex.collector.core.pipeline.committer;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.committer.ICommitter;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.importer.doc.ImporterDocument;

/**
 * @author Pascal Essiembre
 *
 */
public class CommitModuleStage
        implements IPipelineStage<DocumentPipelineContext> {
    @Override
    public boolean execute(DocumentPipelineContext ctx) {
        ICommitter committer = ctx.getConfig().getCommitter();
        if (committer != null) {
            ImporterDocument doc = ctx.getDocument();
            committer.add(doc.getReference(), 
                    doc.getContent(), doc.getMetadata());
        }
        ctx.fireCrawlerEvent(
                CrawlerEvent.DOCUMENT_COMMITTED, 
                ctx.getCrawlData(), committer);
        return true;
    }
}  