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
package com.norconex.collector.core.pipeline.importer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.importer.doc.ImporterMetadata;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * @author Pascal Essiembre
 *
 */
public final class ImporterPipelineUtil {

    private static final Logger LOG = 
            LogManager.getLogger(ImporterPipelineUtil.class);
    
    /**
     * Constructor.
     */
    private ImporterPipelineUtil() {
    }

    public static boolean isHeadersRejected(ImporterPipelineContext ctx) {
        IMetadataFilter[] filters = ctx.getConfig().getMetadataFilters();
        if (filters == null) {
            return false;
        }
        ImporterMetadata metadata = ctx.getDocument().getMetadata();
        boolean hasIncludes = false;
        boolean atLeastOneIncludeMatch = false;
        for (IMetadataFilter filter : filters) {
            boolean accepted = filter.acceptMetadata(
                    ctx.getCrawlData().getReference(), metadata);
            boolean isInclude = filter instanceof IOnMatchFilter
                   && OnMatch.INCLUDE == ((IOnMatchFilter) filter).getOnMatch();
            if (isInclude) {
                hasIncludes = true;
                if (accepted) {
                    atLeastOneIncludeMatch = true;
                }
                continue;
            }
            if (accepted) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("ACCEPTED document metadata. "
                            + "Reference=%s Filter=%s",
                            ctx.getCrawlData().getReference(), filter));
                }
            } else {
                ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_FILTER, 
                        ctx.getCrawlData(), filter);
                return true;
            }
        }
        if (hasIncludes && !atLeastOneIncludeMatch) {
            ctx.fireCrawlerEvent(
                    CrawlerEvent.REJECTED_FILTER, ctx.getCrawlData(), null);
            return true;
        }
        return false;        
    }

}
