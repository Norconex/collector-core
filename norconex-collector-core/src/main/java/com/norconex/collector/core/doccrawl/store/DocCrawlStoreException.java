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
package com.norconex.collector.core.doccrawl.store;

import com.norconex.collector.core.CollectorException;

public class DocCrawlStoreException extends CollectorException {

    
    private static final long serialVersionUID = 5416591514078326431L;

    public DocCrawlStoreException() {
        super();
    }

    public DocCrawlStoreException(String message) {
        super(message);
    }

    public DocCrawlStoreException(Throwable cause) {
        super(cause);
    }

    public DocCrawlStoreException(String message, Throwable cause) {
        super(message, cause);
    }

}
