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
package com.norconex.collector.core.crawler.event;

import com.norconex.collector.core.crawler.ICrawler;

/**
 * <p>Allows implementers to react to any crawler-specific events.</p>
 * <p>Keep in mind that if defined as part of crawler defaults, 
 * a single instance of this listener will be shared amongst crawlers
 * (unless overwritten).</p>
 * @author Pascal Essiembre
 */
public interface ICrawlerEventListener {
    
	//TODO add two new methods for headers and document checksum 
	//(i.e. modified vs not-modified.   Or rely on rejected flag?
	//TODO add documentDeleted
	//TODO add urlProcessed with CrawlStatus to help with reporting
    //TODO refactor even handling by having 1 method only accepting an 
    //event interface (or superclass).  More scalable that way.
    
//    void crawlerStarted(ICrawler crawler);
    void crawlerEvent(ICrawler crawler, CrawlerEvent event);
//    void crawlerFinished(ICrawler crawler);
}
