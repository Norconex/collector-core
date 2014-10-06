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