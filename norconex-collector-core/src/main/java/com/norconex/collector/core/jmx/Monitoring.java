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
package com.norconex.collector.core.jmx;

import com.norconex.collector.core.data.store.ICrawlDataStore;

public class Monitoring implements MonitoringMBean {

    private final ICrawlDataStore refStore;
    
    public Monitoring(ICrawlDataStore refStore) {
        this.refStore = refStore;
    }

    @Override
    public int getProcessedURLCount() {
        return refStore.getProcessedCount();
    }

    @Override
    public int getURLQueueSize() {
        return refStore.getQueueSize();
    }

}
