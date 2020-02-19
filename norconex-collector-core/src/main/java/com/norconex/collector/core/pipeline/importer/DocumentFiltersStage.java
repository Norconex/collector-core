/* Copyright 2014-2020 Norconex Inc.
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.doc.CrawlState;
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * @author Pascal Essiembre
 *
 */
public class DocumentFiltersStage
        implements IPipelineStage<ImporterPipelineContext> {

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentFiltersStage.class);

    @Override
    public boolean execute(ImporterPipelineContext ctx) {
        List<IDocumentFilter> filters = ctx.getConfig().getDocumentFilters();
        if (filters == null) {
            return true;
        }

        boolean hasIncludes = false;
        boolean atLeastOneIncludeMatch = false;
        for (IDocumentFilter filter : filters) {
            boolean accepted = filter.acceptDocument(ctx.getDocument());

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
                    LOG.debug(String.format(
                            "ACCEPTED document. Reference=%s Filter=%s", ctx
                                .getCrawlReference().getReference(), filter));
                }
            } else {
                ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_FILTER,
                        ctx.getCrawlReference(), filter);
                ctx.getCrawlReference().setState(CrawlState.REJECTED);
                return false;
            }
        }
        if (hasIncludes && !atLeastOneIncludeMatch) {
            ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_FILTER,
                    ctx.getCrawlReference(),
                    "No \"include\" document filters matched.");
            ctx.getCrawlReference().setState(CrawlState.REJECTED);
            return false;
        }
        return true;
    }

    private boolean isIncludeFilter(IDocumentFilter filter) {
        if (filter instanceof IOnMatchFilter) {
            OnMatch onMatch = OnMatch.includeIfNull(
                    ((IOnMatchFilter) filter).getOnMatch());
            return OnMatch.INCLUDE == onMatch;
        }
        return false;
    }
}