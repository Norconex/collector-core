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
package com.norconex.collector.core.pipeline;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.CrawlerEvent;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.doc.CrawlState;

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
        CrawlDocInfo docInfo = ctx.getDocInfo();

        // Set new checksum on crawlData + metadata
        String type;
        if (isMeta) {
            docInfo.setMetaChecksum(newChecksum);
            type = "metadata";
        } else {
            docInfo.setContentChecksum(newChecksum);
            type = "document";
        }

        // Get old checksum from cache
        CrawlDocInfo cachedDocInfo = ctx.getCachedDocInfo();

        // if there was nothing in cache, or what is in cache is a deleted
        // doc, consider as new.
        if (cachedDocInfo == null
                || CrawlState.DELETED.isOneOf(cachedDocInfo.getState())) {
            LOG.debug("ACCEPTED {} checkum (new): Reference={}",
                    type, docInfo.getReference());

            // Prevent not having status when finalizing document on embedded docs
            // (which otherwise do not have a status. 
            // But if already has a status, keep it.
            if (docInfo.getState() == null) {
                docInfo.setState(CrawlState.NEW);
            }
            return true;
        }

        String oldChecksum = null;
        if (isMeta) {
            oldChecksum = cachedDocInfo.getMetaChecksum();
        } else {
            oldChecksum = cachedDocInfo.getContentChecksum();
        }

        // Compare checksums
        if (StringUtils.isNotBlank(newChecksum)
                && Objects.equals(newChecksum, oldChecksum)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("REJECTED {} checkum (unmodified): Reference={}",
                        type, docInfo.getReference());
            }
            docInfo.setState(CrawlState.UNMODIFIED);
            ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_UNMODIFIED,
                    ctx.getDocInfo(), subject);
            return false;
        }

        docInfo.setState(CrawlState.MODIFIED);
        LOG.debug("ACCEPTED {} checksum (modified): Reference={}",
                type, docInfo.getReference());
        return true;
    }
}
