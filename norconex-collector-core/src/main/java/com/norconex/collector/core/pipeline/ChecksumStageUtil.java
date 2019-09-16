/* Copyright 2014-2019 Norconex Inc.
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
package com.norconex.collector.core.pipeline;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.reference.CrawlReference;
import com.norconex.collector.core.reference.CrawlState;

/**
 * Checksum stage utility methods.
 * @author Pascal Essiembre
 */
public final class ChecksumStageUtil {

    private static final Logger LOG =
            LoggerFactory.getLogger(ChecksumStageUtil.class);

    private ChecksumStageUtil() {
        super();
    }


    public static boolean resolveMetaChecksum(
            String newChecksum, DocumentPipelineContext ctx, Object subject) {
        return resolveChecksum(true, newChecksum, ctx, subject);
    }
    public static boolean resolveDocumentChecksum(
            String newChecksum, DocumentPipelineContext ctx, Object subject) {
        return resolveChecksum(false, newChecksum, ctx, subject);
    }


    // return false if checksum is rejected/unmodified
    private static boolean resolveChecksum(boolean isMeta, String newChecksum,
            DocumentPipelineContext ctx, Object subject) {
        CrawlReference crawlRef = ctx.getCrawlReference();

        // Set new checksum on crawlData + metadata
        String type;
        if (isMeta) {
            crawlRef.setMetaChecksum(newChecksum);
            type = "metadata";
        } else {
            crawlRef.setContentChecksum(newChecksum);
            type = "document";
        }

        // Get old checksum from cache
        CrawlReference cachedCrawlRef = ctx.getCachedCrawlReference();
        String oldChecksum = null;
        if (cachedCrawlRef != null) {
            if (isMeta) {
                oldChecksum = cachedCrawlRef.getMetaChecksum();
            } else {
                oldChecksum = cachedCrawlRef.getContentChecksum();
            }
        } else {
            LOG.debug("ACCEPTED {} checkum (new): Reference={}",
                    type, crawlRef.getReference());
            return true;
        }

        // Compare checksums
        if (StringUtils.isNotBlank(newChecksum)
                && Objects.equals(newChecksum, oldChecksum)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("REJECTED {} checkum (unmodified): Reference={}",
                        type, crawlRef.getReference());
            }
            crawlRef.setState(CrawlState.UNMODIFIED);
            ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_UNMODIFIED,
                    ctx.getCrawlReference(), subject);
            return false;
        }

        crawlRef.setState(CrawlState.MODIFIED);
        LOG.debug("ACCEPTED {} checksum (modified): Reference={}",
                type, crawlRef.getReference());
        return true;
    }
}
