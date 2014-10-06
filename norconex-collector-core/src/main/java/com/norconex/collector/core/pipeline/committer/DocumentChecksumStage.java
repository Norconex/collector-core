/**
 * 
 */
package com.norconex.collector.core.pipeline.committer;

import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.pipeline.ChecksumStageUtil;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * @author Pascal Essiembre
 *
 */
public class DocumentChecksumStage 
        implements IPipelineStage<DocumentPipelineContext> {
    
    @Override
    public boolean execute(DocumentPipelineContext ctx) {
        //TODO only if an INCREMENTAL run... else skip.
        IDocumentChecksummer check = 
                ctx.getConfig().getDocumentChecksummer();
        if (check == null) {
            // NEW is default state (?)
            ctx.getCrawlData().setState(CrawlState.NEW);
            return true;
        }
        String newDocChecksum = check.createDocumentChecksum(ctx.getDocument());
        return ChecksumStageUtil.resolveDocumentChecksum(
                newDocChecksum, ctx, check);
    }
    
}   