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
package com.norconex.collector.core.pipeline.importer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * Common pipeline stage for saving documents.
 * @author Pascal Essiembre
 */
public class SaveDocumentStage
        implements IPipelineStage<ImporterPipelineContext> {

    private static final Logger LOG = 
            LogManager.getLogger(SaveDocumentStage.class);
    
    private static final int MAX_SEGMENT_LENGTH = 25;
    
    @Override
    public boolean execute(ImporterPipelineContext ctx) {
        //TODO have an interface for how to store downloaded files
        //(i.e., location, directory structure, file naming)
        File workdir = ctx.getConfig().getWorkDir();
        File downloadDir = new File(workdir, "/downloads");
        if (!downloadDir.exists()) {
            try {
                FileUtils.forceMkdir(downloadDir);
            } catch (IOException e) {
                throw new CollectorException(
                        "Cannot create download directory: " + downloadDir, e);
            }
        }
        String path = urlToPath(ctx.getCrawlData().getReference());
        
        File downloadFile = new File(downloadDir, path);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saved file: " + downloadFile);
        }
        try {
            OutputStream out = FileUtils.openOutputStream(downloadFile);
            IOUtils.copy(ctx.getDocument().getContent(), out);
            IOUtils.closeQuietly(out);
            
            ctx.fireCrawlerEvent(
                    CrawlerEvent.DOCUMENT_SAVED, ctx.getCrawlData(), 
                    downloadFile);
        } catch (IOException e) {
            throw new CollectorException("Cannot save document: " 
                            + ctx.getCrawlData().getReference(), e);
        }            
        return true;
    }
    
    public static String urlToPath(final String url) {
        if (url == null) {
            return null;
        }
        String sep = File.separator;
        if (sep.equals("\\")) {
            sep = "\\" + sep;
        }
        
        String domain = url.replaceFirst("(.*?)(://)(.*?)(/)(.*)", "$1_$3");
        domain = domain.replaceAll("[\\W]+", "_");
        String path = url.replaceFirst("(.*?)(://)(.*?)(/)(.*)", "$5");
        
        String[] segments = path.split("[\\/]");
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            if (StringUtils.isNotBlank(segment)) {
                boolean lastSegment = (i + 1) == segments.length;
                String[] segParts = splitLargeSegment(segment);
                for (int j = 0; j < segParts.length; j++) {
                    String segPart = segParts[j];
                    if (b.length() > 0) {
                        b.append(File.separatorChar);
                    }
                    // Prefixes directories or files with different letter
                    // to ensure directory and files can't have the same 
                    // names (github #44).
                    if (lastSegment && (j + 1) == segParts.length) {
                        b.append("f.");
                    } else {
                        b.append("d.");
                    }
                    b.append(FileUtil.toSafeFileName(segPart));
                }
            }
        }
        if (b.length() > 0) {
            return "d." + domain + File.separatorChar + b.toString();
        }
        return "f." + domain;
    }
    
    private static String[] splitLargeSegment(String segment) {
        if (segment.length() <= MAX_SEGMENT_LENGTH) {
            return new String[] { segment };
        }
        
        List<String> segments = new ArrayList<>();
        StringBuilder b = new StringBuilder(segment);
        while (b.length() > MAX_SEGMENT_LENGTH) {
            segments.add(b.substring(0, MAX_SEGMENT_LENGTH));
            b.delete(0, MAX_SEGMENT_LENGTH);
        }
        segments.add(b.substring(0));
        return segments.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
}   