/**
 * 
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

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.crawler.event.CrawlerEvent;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.pipeline.IPipelineStage;

/**
 * @author Pascal Essiembre
 *
 */
public class SaveDocumentStage
        implements IPipelineStage<ImporterPipelineContext> {

    private static final int MAX_SEGMENT_LENGTH = 25;
    
    @Override
    public boolean execute(ImporterPipelineContext ctx) {
        //TODO have an interface for how to store downloaded files
        //(i.e., location, directory structure, file naming)
        File workdir = ctx.getConfig().getWorkDir();
        File downloadDir = new File(workdir, "/downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        File downloadFile = new File(downloadDir, 
                urlToPath(ctx.getCrawlData().getReference()));
        try {
            OutputStream out = FileUtils.openOutputStream(downloadFile);
            IOUtils.copy(ctx.getDocument().getContent(), out);
            IOUtils.closeQuietly(out);
            
            ctx.fireCrawlerEvent(
                    CrawlerEvent.DOCUMENT_SAVED, ctx.getCrawlData(), 
                    downloadFile);
        } catch (IOException e) {
            throw new CollectorException("Cannot create RobotsMeta for : " 
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
        for (String segment : segments) {
            if (StringUtils.isNotBlank(segment)) {
                String[] segParts = splitLargeSegment(segment);
                for (String segPart : segParts) {
                    if (b.length() > 0) {
                        b.append(File.separatorChar);
                    }
                    b.append(FileUtil.toSafeFileName(segPart));
                }
            }
        }
        return domain + File.separatorChar + b.toString();
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