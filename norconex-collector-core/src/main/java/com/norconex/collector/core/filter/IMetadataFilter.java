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
package com.norconex.collector.core.filter;

import com.norconex.commons.lang.map.Properties;

/**
 * Filter a reference based on the metadata that could be obtained for a 
 * document, before it was fetched, downloaded, or otherwise read or acquired
 * (e.g. HTTP headers, File properties, ...).
 * <p>
 * It is highly recommended to overwrite the <code>toString()</code> method
 * to representing this filter properly in human-readable form (e.g. logging).
 * It is a good idea to include specifics of this filter so crawler users 
 * can know exactly why documents got accepted/rejected rejected if need be.
 * </p>
 * @author Pascal Essiembre
 */
public interface IMetadataFilter {

    /**
     * Whether to accept the metadata.  
     * @param reference the reference associated with the metadata
     * @param metadata metadata associated with the reference
     * @return <code>true</code> if accepted, <code>false</code> otherwise
     */
    boolean acceptMetadata(String reference, Properties metadata);
}
