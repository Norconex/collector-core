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
package com.norconex.collector.core.crawler;

import java.io.File;

import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.crawler.event.ICrawlerEventListener;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.committer.ICommitter;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.importer.ImporterConfig;

/**
 * @author Pascal Essiembre
 *
 */
public interface ICrawlerConfig extends IXMLConfigurable, Cloneable {

    /**
     * Gets this crawler unique identifier.
     * @return unique identifier
     */
    String getId();

    File getWorkDir();

    int getNumThreads();

    /**
     * Gets the maximum number of documents that can be processed.
     * @return maximum number of documents that can be processed
     */
    int getMaxDocuments();
    
    boolean isDeleteOrphans();
    
    ICrawlDataStoreFactory getCrawlDataStoreFactory();
    
    ICrawlerEventListener[] getCrawlerListeners();

    ImporterConfig getImporterConfig();
    
    ICommitter getCommitter();

    ICrawlerConfig clone();
    
    IReferenceFilter[] getReferenceFilters();
    
    IDocumentChecksummer getDocumentChecksummer();
}