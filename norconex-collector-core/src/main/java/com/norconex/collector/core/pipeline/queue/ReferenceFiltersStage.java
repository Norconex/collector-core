/**
 * 
 */
package com.norconex.collector.core.pipeline.queue;

import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * @author Pascal Essiembre
 *
 */
public class ReferenceFiltersStage 
        implements IPipelineStage<BasePipelineContext> {

    private final String type;
    
    public ReferenceFiltersStage() {
        this(null);
    }
    public ReferenceFiltersStage(String type) {
        super();
        this.type = type;
    }

    @Override
    public boolean execute(BasePipelineContext ctx) {
        if (ReferenceFiltersStageUtil.resolveReferenceFilters(
                ctx.getConfig().getReferenceFilters(), ctx, type)) {
            ctx.getCrawlData().setState(CrawlState.REJECTED);
            return false;
        }
        return true;
    }
}
