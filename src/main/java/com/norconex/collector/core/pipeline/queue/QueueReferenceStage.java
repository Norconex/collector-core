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
package com.norconex.collector.core.pipeline.queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.doc.CrawlDocInfo.Stage;
import com.norconex.collector.core.pipeline.DocInfoPipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * Common pipeline stage for queuing documents.
 * @author Pascal Essiembre
 */
public class QueueReferenceStage
        implements IPipelineStage<DocInfoPipelineContext> {

    private static final Logger LOG =
            LoggerFactory.getLogger(QueueReferenceStage.class);

    /**
     * Constructor.
     */
    public QueueReferenceStage() {
        super();
    }

    @Override
    public boolean execute(DocInfoPipelineContext ctx) {
        //TODO document and make sure it cannot be blank and remove this check?
        String ref = ctx.getDocInfo().getReference();
        if (StringUtils.isBlank(ref)) {
            return true;
        }

        Stage stage = ctx.getDocInfoService().getProcessingStage(ref);

        //TODO make this a reusable method somewhere, or part of the
        //CrawlDocInfoService?
        if (Stage.ACTIVE.is(stage)) {
            debug("Already being processed: %s", ref);
//            return false;
        } else if (Stage.QUEUED.is(stage)) {
            debug("Already queued: %s", ref);
//            return false;
        } else if (Stage.PROCESSED.is(stage)) {
            debug("Already processed: %s", ref);
//            return false;
        } else {
            ctx.getDocInfoService().queue(ctx.getDocInfo());
//            refStore.queue(ctx.getCrawlData().clone());
            debug("Queued for processing: %s", ref);
        }
        return true;
    }

    private void debug(String message, Object... values) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(message, values));
        }
    }
}
