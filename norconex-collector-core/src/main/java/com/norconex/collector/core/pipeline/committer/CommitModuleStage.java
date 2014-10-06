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
package com.norconex.collector.core.pipeline.committer;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.committer.ICommitter;
import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Common pipeline stage for committing documents.
 * @author Pascal Essiembre
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
                CrawlerEvent.DOCUMENT_COMMITTED_ADD, 
                ctx.getCrawlData(), committer);
        return true;
    }
}  