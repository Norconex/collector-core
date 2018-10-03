/* Copyright 2014-2016 Norconex Inc.
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

        boolean isContentTypeSet = doc.getContentType() != null;

        ImporterResponse response = importer.importDocument(
                doc.getInputStream(),
                doc.getContentType(),
                doc.getContentEncoding(),
                doc.getMetadata(),
                doc.getReference());
        ctx.setImporterResponse(response);

        //TODO is it possible for content type not to be set here??
        // We make sure to set it to save it to store so IRecrawlableResolver
        // has one to deal with
        if (!isContentTypeSet && response.getDocument() != null) {
            ctx.getCrawlData().setContentType(
                    response.getDocument().getContentType());
        }

        return true;
    }
}