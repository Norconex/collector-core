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
package com.norconex.collector.core.pipeline.queue;

import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * Common pipeline stage for filtering references.
 * @author Pascal Essiembre
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
