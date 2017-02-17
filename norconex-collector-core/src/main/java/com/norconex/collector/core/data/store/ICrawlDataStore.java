/* Copyright 2014-2017 Norconex Inc.
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
package com.norconex.collector.core.data.store;

import java.util.Iterator;

import com.norconex.collector.core.data.ICrawlData;

/**
 * <p>Holds necessary information about all references (e.g. url, path, etc) 
 * crawling activities.
 * </p>
 * <p>
 * The few stages a reference should have in most implementations are:</p>
 * <ul>
 *   <li><b>Queued:</b> References extracted from documents are first queued for 
 *       future processing.</li>
 *   <li><b>Active:</b> A reference is being processed.</li>
 *   <li><b>Processed:</b> A reference has been processed.  If the same URL is 
 *       encountered again during the same run, it will be ignored.</li>
 *   <li><b>Cached:</b> When crawling is over, processed references will be 
 *       cached on the next run.</li>
 * </ul>
 * @author Pascal Essiembre
 */
public interface ICrawlDataStore {

    /**
     * <p>
     * Queues a reference for future processing. 
     * @param crawlData  the reference to eventually be processed
     */
    void queue(ICrawlData crawlData);

    /**
     * Whether there are any references to process in the queue.
     * @return <code>true</code> if the queue is empty
     */
    boolean isQueueEmpty();
    
    /**
     * Gets the size of the reference queue (number of 
     * references left to process).
     * @return queue size
     */
    int getQueueSize();

    /**
     * Whether the given reference is in the queue or not 
     * (waiting to be processed).
     * @param reference the reference 
     * @return <code>true</code> if the reference is in the queue
     */
    boolean isQueued(String reference);
    
    /**
     * Returns the next reference to be processed from the queue and marks it as 
     * being "active" (i.e. currently being processed).  The returned reference 
     * is effectively removed from the queue.
     * @return next reference 
     */
    ICrawlData nextQueued();
    
    /**
     * Whether the given reference is currently being processed (i.e. active).
     * @param reference the reference
     * @return <code>true</code> if active
     */
    boolean isActive(String reference);

    /**
     * Gets the number of active references (currently being processed).
     * @return number of active references.
     */
    int getActiveCount();
    
    /**
     * Gets the cached reference from previous time crawler was run
     * (e.g. for comparison purposes).
     * @param cacheReference reference cached from previous run
     * @return crawl data
     */
    ICrawlData getCached(String cacheReference);
    
    /**
     * Whether there are any references the the cache from a previous crawler 
     * run.
     * @return <code>true</code> if the cache is empty
     */
    boolean isCacheEmpty();

    /**
     * Marks this reference as processed.  Processed references will not be 
     * processed again in the same crawl run.
     * @param crawlData processed reference
     */
    void processed(ICrawlData crawlData);

    /**
     * Whether the given reference has been processed.
     * @param reference the reference
     * @return <code>true</code> if processed
     */
    boolean isProcessed(String reference);

    /**
     * Gets the number of references processed.
     * @return number of references processed.
     */
    int getProcessedCount();

    /**
     * Gets the cache iterator.
     * @return cache iterator
     */
    Iterator<ICrawlData> getCacheIterator();
    
    /**
     * Closes a database connection. This method gets called a the end
     * of a crawling job to give a change to close the underlying connection
     * properly, if applicable.
     */
    void close();
}
