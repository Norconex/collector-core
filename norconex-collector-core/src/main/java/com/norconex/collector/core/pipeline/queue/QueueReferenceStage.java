/* Copyright 2014 Norconex Inc.
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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.pipeline.BasePipelineContext;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * Common pipeline stage for queuing documents.
 * @author Pascal Essiembre
 */
public class QueueReferenceStage 
        implements IPipelineStage<BasePipelineContext> {

    private static final Logger LOG = 
            LoggerFactory.getLogger(QueueReferenceStage.class);
    
    /**
     * Constructor.
     */
    public QueueReferenceStage() {
        super();
    }

    @Override
    public boolean execute(BasePipelineContext ctx) {
        String ref = ctx.getCrawlData().getReference();
        if (StringUtils.isBlank(ref)) {
            return true;
        }
        ICrawlDataStore refStore = ctx.getCrawlDataStore();
        
        if (refStore.isActive(ref)) {
            debug("Already being processed: %s", ref);
        } else if (refStore.isQueued(ref)) {
            debug("Already queued: %s", ref);
        } else if (refStore.isProcessed(ref)) {
            debug("Already processed: %s", ref);
        } else {
            refStore.queue(ctx.getCrawlData().clone());
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
