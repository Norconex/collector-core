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
                    CrawlerEvent.REJECTED_FILTER, ctx.getCrawlData(), 
                    "No \"include\" metadata filters matched.");
            return true;
        }
        return false;        
    }

}
