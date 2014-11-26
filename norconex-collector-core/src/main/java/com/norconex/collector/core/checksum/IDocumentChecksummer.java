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
package com.norconex.collector.core.checksum;

import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Creates a checksum representing a a document. 
 * Checksums are used to quickly filter out documents that have already been 
 * processed or that have changed since a previous run.
 * <br><br>  
 * Two or more {@link ImporterDocument} can hold different values, but 
 * be deemed logically the same.
 * Such documents do not have to be <em>equal</em>, but they should return the 
 * same checksum.  An example of
 * this can be two different URLs pointing to the same document, where only a 
 * single instance should be kept. 
 * <br><br>
 * There are no strict rules that define what is equivalent or not.  
 * <br><br>
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
