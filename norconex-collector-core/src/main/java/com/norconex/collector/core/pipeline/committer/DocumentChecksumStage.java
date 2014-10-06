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

import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.pipeline.ChecksumStageUtil;
import com.norconex.collector.core.pipeline.DocumentPipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * Common pipeline stage for creating a document checksum.
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