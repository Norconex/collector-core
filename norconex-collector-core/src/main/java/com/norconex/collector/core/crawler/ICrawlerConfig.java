/* Copyright 2014-2018 Norconex Inc.
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
package com.norconex.collector.core.crawler;

import java.nio.file.Path;
import java.util.List;

import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.committer.core.ICommitter;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.importer.ImporterConfig;

/**
 * Crawler configuration.  Crawlers usually read this configuration just before
 * they start their execution.  Once execution has started, it is not
 * recommended to change it.
 * @author Pascal Essiembre
 */
public interface ICrawlerConfig extends IXMLConfigurable {

    enum OrphansStrategy {
        /**
         * Deleting orphans sends them to the Committer for deletions and
         * they are removed from the internal reference cache.
         */
        DELETE,
        /**
         * Processing orphans tries to obtain and process them again,
         * normally.
         */
        PROCESS,
        /**
         * Ignoring orphans effectively does nothing with them
         * (not deleted, not processed).
         */
        IGNORE
    }

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
    Path getWorkDir();

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
     * Gets the exceptions we want to stop the crawler on.
     * By default the crawler will log exceptions from processing
     * a document and try to move on to the next without stopping.
     * Even if no exceptions are returned by this method,
     * the crawler can sometimes stop regardless if it cannot recover
     * safely from an exception.
     * To capture more exceptions, use a parent class (e.g., Exception
     * should catch them all).
     * @return exceptions that will stop the crawler when encountered
     * @since 1.9.0
     */
    List<Class<? extends Exception>> getStopOnExceptions();

    /**
     * <p>Gets the strategy to adopt when there are orphans.  Orphans are
     * references that were processed in a previous run, but were not in the
     * current run.  In other words, they are leftovers from a previous run
     * that were not re-encountered in the current.
     * </p><p>
     * Unless explicitly stated otherwise by an implementing class, the default
     * strategy is to <code>PROCESS</code> orphans.
     * Setting a <code>null</code> value is the same as setting
     * <code>IGNORE</code>.
     * </p><p>
     * Since 1.2.0, unless otherwise stated in implementing classes,
     * the default orphan strategy is now <code>PROCESS</code>.
     * </p><p>
     * <b>Be careful:</b> Setting the orphan strategy to <code>DELETE</code>
     * is NOT recommended in most cases. With some collectors, a temporary
     * failure such as a network outage or a web page timing out, may cause
     * some documents not to be crawled. When this happens, unreachable
     * documents would be considered "orphans" and be deleted while under
     * normal circumstances, they should be kept.  Re-processing them
     * (default), is usually the safest approach to confirm they still
     * exist before deleting or updating them.
     * </p>
     * @return orphans strategy
     */
    OrphansStrategy getOrphansStrategy();

    /**
     * Gets the crawl data store factory a crawler should use.
     * @return crawl data store factory.
     */
    ICrawlDataStoreFactory getCrawlDataStoreFactory();

//    /**
//     * Gets crawler event listeners.
//     * @return crawler evetn listeners
//     */
//    List<ICrawlerEventListener> getCrawlerListeners();

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
     * Gets the reference filters.
     * @return reference filters
     */
    List<IReferenceFilter> getReferenceFilters();

    /**
     * Gets the document filters.
     * @return document filters
     */
    List<IDocumentFilter> getDocumentFilters();

    /**
     * Gets the metadata filters.
     * @return metadata filters
     */
    List<IMetadataFilter> getMetadataFilters();

    /**
     * Gets the document checksummer.
     * @return document checksummer
     */
    IDocumentChecksummer getDocumentChecksummer();

    /**
     * Gets the spoiled state strategy resolver.
     * @return spoiled state strategy resolver
     * @since 1.2.0
     */
    ISpoiledReferenceStrategizer getSpoiledReferenceStrategizer();

}