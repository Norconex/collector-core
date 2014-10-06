/**
 * 
 */
package com.norconex.collector.core.pipeline.queue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * @author Pascal Essiembre
 *
 */
public class QueueReferenceStage 
        implements IPipelineStage<BasePipelineContext> {

    private static final Logger LOG = 
            LogManager.getLogger(QueueReferenceStage.class);
    
    /**
     * Constructor.
     */
    public QueueReferenceStage() {
    }

    @Override
    public boolean execute(BasePipelineContext ctx) {
        String ref = ctx.getCrawlData().getReference();
        if (StringUtils.isBlank(ref)) {
            return true;
        }
        ICrawlDataStore refStore = ctx.getCrawlDataStore();
        
        if (refStore.isActive(ref)) {
            debug("Already being processed: %s", ref);
        } else if (refStore.isQueued(ref)) {
            debug("Already queued: %s", ref);
        } else if (refStore.isProcessed(ref)) {
            debug("Already processed: %s", ref);
        } else {
            refStore.queue(ctx.getCrawlData().clone());
//            refStore.queue(new BaseCrawlData(ref));
            debug("Queued for processing: %s", ref);
        }
        return true;
    }
    
    private void debug(String message, Object... values) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(message, values));
        }
    }   
}
