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
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.committer.ICommitter;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.importer.ImporterConfig;

/**
 * Crawler configuration.  Crawlers usually read this configuration just before 
 * they start their execution.  Once execution has started, it is not
 * recommended to change it.
 * @author Pascal Essiembre
 */
public interface ICrawlerConfig extends IXMLConfigurable, Cloneable {

    /**
     * Gets this crawler unique identifier.  Using usual names is
     * perfectly fine (non-alphanumeric characters are OK).
     * @return unique identifier
     */
    String getId();

    /**
     * Gets the crawler working directory where many files created at 
     * execution time are stored.
     * @return working directory
     */
    File getWorkDir();

    /**
     * Gets the number of threads (maximum) a crawler should use.
     * @return number of threads
     */
    int getNumThreads();

    /**
     * Gets the maximum number of documents that can be processed.
     * @return maximum number of documents that can be processed
     */
    int getMaxDocuments();
    
    /**
     * Whether orphan files from a previous run (files previously crawled
     * but no longer reachable) should be deleted.
     * @return <code>true</code> if orphans should be deleted
     */
    boolean isDeleteOrphans();
    
    /**
     * Gets the crawl data store factory a crawler should use.
     * @return crawl data store factory.
     */
    ICrawlDataStoreFactory getCrawlDataStoreFactory();
    
    /**
     * Gets crawler event listeners.
     * @return crawler evetn listeners
     */
    ICrawlerEventListener[] getCrawlerListeners();

    /**
     * Gets the Importer module configuration.
     * @return Importer module configuration
     */
    ImporterConfig getImporterConfig();
    
    /**
     * Gets the Committer module configuration.
     * @return Committer module configuration
     */
    ICommitter getCommitter();

    /**
     * Clone this configuration.
     * @return new configuration
     */
    ICrawlerConfig clone();
    
    /**
     * Gets the reference filters.
     * @return reference filters
     */
    IReferenceFilter[] getReferenceFilters();

    /**
     * Gets the document filters.
     * @return document filters
     */
    IDocumentFilter[] getDocumentFilters();

    /**
     * Gets the metadata filters.
     * @return metadata filters
     */
    IMetadataFilter[] getMetadataFilters();

    
    /**
     * Gets the document checksummer.
     * @return document checksummer
     */
    IDocumentChecksummer getDocumentChecksummer();
}