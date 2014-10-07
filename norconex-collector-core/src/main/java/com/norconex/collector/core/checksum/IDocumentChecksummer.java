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
package com.norconex.collector.core.checksum;

import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Creates a checksum representing a a document. 
 * Checksums are used to quickly filter out documents that have already been 
 * processed or that have changed since a previous run.
 * <p />  
 * Two or more {@link ImporterDocument} can hold different values, but 
 * be deemed logically the same.
 * Such documents do not have to be <em>equal</em>, but they should return the 
 * same checksum.  An example of
 * this can be two different URLs pointing to the same document, where only a 
 * single instance should be kept. 
 * <p />
 * There are no strict rules that define what is equivalent or not.  
 * <p />
 * Classes implementing {@link IXMLConfigurable} should offer the following
 * XML configuration usage:
 * <pre>
 *  &lt;documentChecksummer 
 *      class="(class)"&gt;
 *      keep="[false|true]"
 *      targetField="(optional metadata field to store the checksum)" /&gt;
 * </pre>
 * <code>targetField</code> is ignored unless the <code>keep</code> 
 * attribute is set to <code>true</code>.
 * @author Pascal Essiembre
 * @see AbstractDocumentChecksummer
 */
public interface IDocumentChecksummer {

    /**
     * Creates a document checksum.
     * @param document an HTTP document
     * @return a checksum value
     */
	String createDocumentChecksum(ImporterDocument document);
	
}
