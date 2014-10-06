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

import java.io.Serializable;

/**
 * Filter a document based on its reference, before its properties or content
 * gets read or otherwise acquired.
 * <p />
 * It is highly recommended to overwrite the <code>toString()</code> method
 * to representing this filter properly in human-readable form 
 * (e.g. for logging by a crawler).
 * It is a good idea to include specifics of this filter so crawler users 
 * can know exactly why documents got accepted/rejected rejected if need be.
 * <p />
 * Implementors also implementing IXMLConfigurable must name their XML tag
 * <code>filter</code> to ensure it gets loaded properly.</p>
 * @author Pascal Essiembre
 */
public interface IReferenceFilter extends Serializable {

    /**
     * Whether to accept this reference.  
     * @param reference the reference to accept/reject
     * @return <code>true</code> if accepted, <code>false</code> otherwise
     */
    boolean acceptReference(String reference);
    
}
