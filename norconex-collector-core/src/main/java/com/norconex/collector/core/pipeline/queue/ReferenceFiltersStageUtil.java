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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * Reference-filtering stage utility methods.
 * @author Pascal Essiembre
 */
public final class ReferenceFiltersStageUtil {

    private static final Logger LOG = 
            LogManager.getLogger(ReferenceFiltersStageUtil.class);
    
    private ReferenceFiltersStageUtil() {
        super();
    }

    // return false if reference is rejected
    public static boolean resolveReferenceFilters(IReferenceFilter[] filters, 
            BasePipelineContext ctx, String type) {
        if (filters == null) {
            return false;
        }
        String msg = StringUtils.trimToEmpty(type);
        if (StringUtils.isNotBlank(msg)) {
            msg = " (" + msg + ")";
        }
        
        boolean hasIncludes = false;
        boolean atLeastOneIncludeMatch = false;
        for (IReferenceFilter filter : filters) {
            boolean accepted = filter.acceptReference(
                    ctx.getCrawlData().getReference());
            
            // Deal with includes
            if (isIncludeFilter(filter)) {
                hasIncludes = true;
                if (accepted) {
                    atLeastOneIncludeMatch = true;
                }
                continue;
            }

            // Deal with exclude and non-OnMatch filters
            if (accepted) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ACCEPTED document reference" + msg
                            + ". Reference=" + ctx.getCrawlData().getReference()
                            + " Filter=" + filter);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("REJECTED document reference " + msg
                            + ". Reference=" + ctx.getCrawlData().getReference()
                            + " Filter=" + filter);
                }
                fireDocumentRejected(filter, ctx);
                return true;
            }
        }
        if (hasIncludes && !atLeastOneIncludeMatch) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("REJECTED document reference" + msg
                      + ". No include filters matched. Reference=" 
                      + ctx.getCrawlData().getReference() 
                      + " Filter=[one or more filter 'onMatch' "
                      + "attribute is set to 'include', but none of them were "
                      + "matched]");
            }
            fireDocumentRejected(null, ctx);
            return true;
        }
        return false;
    }
    
    private static void fireDocumentRejected(
            IReferenceFilter filter, BasePipelineContext ctx) {
        ctx.fireCrawlerEvent(
                CrawlerEvent.REJECTED_FILTER, ctx.getCrawlData(), filter);

    }
    
    private static boolean isIncludeFilter(IReferenceFilter filter) {
        return filter instanceof IOnMatchFilter
                && OnMatch.INCLUDE == ((IOnMatchFilter) filter).getOnMatch();
    }
}
