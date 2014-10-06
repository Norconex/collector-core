/**
 * 
 */
package com.norconex.collector.core.pipeline.importer;

import com.norconex.commons.lang.pipeline.IPipelineStage;
import com.norconex.importer.Importer;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.response.ImporterResponse;

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