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
package com.norconex.collector.core.pipeline;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.collector.core.data.BaseCrawlData;
import com.norconex.collector.core.data.CrawlState;

/**
 * Checksum utility methods.
 * @author Pascal Essiembre
 */
public final class ChecksumStageUtil {

    private static final Logger LOG = 
            LogManager.getLogger(ChecksumStageUtil.class);
    
    private ChecksumStageUtil() {
        super();
    }

    
    public static boolean resolveMetaChecksum(
            String newChecksum, BasePipelineContext ctx, Object subject) {
        return resolveChecksum(true, newChecksum, ctx, subject);
    }
    public static boolean resolveDocumentChecksum(
            String newChecksum, BasePipelineContext ctx, Object subject) {
        return resolveChecksum(false, newChecksum, ctx, subject);
    }

    
    // return false if checksum is rejected/unmodified
    private static boolean resolveChecksum(boolean isMeta, String newChecksum, 
            BasePipelineContext ctx, Object subject) {
        BaseCrawlData crawlData = ctx.getCrawlData();
        
        // Set new checksum on crawlData + metadata
        String type;
        if (isMeta) {
            crawlData.setMetaChecksum(newChecksum);
            type = "metadata";
        } else {
            crawlData.setDocumentChecksum(newChecksum);
            type = "document";
        }
        
        // Get old checksum from cache
        BaseCrawlData cachedCrawlData = (BaseCrawlData) 
                ctx.getCrawlDataStore().getCached(crawlData.getReference());
        String oldChecksum = null;
        if (cachedCrawlData != null) {
            if (isMeta) {
                oldChecksum = cachedCrawlData.getMetaChecksum();
            } else {
                oldChecksum = cachedCrawlData.getContentChecksum();
            }
        } else {
            LOG.debug("ACCEPTED " + type + " checkum (new): Reference=" 
                    + crawlData.getReference());
            return true;
        }
        
        // Compare checksums
        if (StringUtils.isNotBlank(newChecksum) 
                && Objects.equals(newChecksum, oldChecksum)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("REJECTED " + type 
                        + " checkum (unmodified): Reference=" 
                        + crawlData.getReference());
            }
            crawlData.setState(CrawlState.UNMODIFIED);
            ctx.fireCrawlerEvent(CrawlerEvent.REJECTED_UNMODIFIED, 
                    ctx.getCrawlData(), subject);
            return false;
        }
        LOG.debug("ACCEPTED " + type + " checksum (modified): Reference=" 
                + crawlData.getReference());
        return true;
    }
    
}
