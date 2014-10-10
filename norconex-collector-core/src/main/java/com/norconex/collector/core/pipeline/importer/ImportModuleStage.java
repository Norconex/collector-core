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

import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.importer.Importer;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.response.ImporterResponse;

/**
 * Common pipeline stage for importing documents.
 * @author Pascal Essiembre
 */
public class ImportModuleStage
            implements IPipelineStage<ImporterPipelineContext> {
        
    @Override
    public boolean execute(ImporterPipelineContext ctx) {
        Importer importer = ctx.getCrawler().getImporter();
            
        ImporterDocument doc = ctx.getDocument();
        
        ImporterResponse response = importer.importDocument(
                doc.getContent(),
                doc.getContentType(),
                doc.getContentEncoding(),
                doc.getMetadata(),
                doc.getReference());
        ctx.setImporterResponse(response);
        return true;
    }
}