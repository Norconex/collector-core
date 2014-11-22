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
package com.norconex.collector.core.crawler;

import com.norconex.collector.core.ICollector;
import com.norconex.collector.core.crawler.event.CrawlerEventManager;
import com.norconex.importer.Importer;
import com.norconex.jef4.job.IJob;

/**
 * A document crawler.  Crawlers are part of a {@link ICollector} and 
 * are responsible for fetching, parsing, manipulating and sending data
 * to a target repository.  It typically does so with the help of 
 * Norconex Importer and Norconex Committer.  
 * @author Pascal Essiembre
 */
public interface ICrawler extends IJob {

    /**
     * Gets the crawler configuration
     * @return the crawler configuration
     */
    ICrawlerConfig getCrawlerConfig();
    
    /**
     * Gets the crawler events manager.
     * @return the events manager
     */
    CrawlerEventManager getCrawlerEventManager();
    
    /**
     * Gets the crawler Importer module.
     * @return the Importer
     */
    Importer getImporter();

}