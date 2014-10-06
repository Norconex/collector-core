/**
 * 
 */
package com.norconex.collector.core.checksum;

import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Creates a checksum representing a document based on document metadata
 * values obtained prior to fetching that document (e.g. HTTP header
 * values form an HTTP HEAD call, file properties, etc.). 
 * Checksums are used to quickly filter out documents that have already been 
 * processed or that have changed since a previous run.
 * <p/>  
 * Two or more {@link ImporterDocument} can hold different values, but 
 * be deemed logically the same.
 * Such documents do not have to be <em>equal</em>, but they should return the 
 * same checksum.  An example of
 * this can be two different URLs pointing to the same document, where only a 
 * single instance should be kept. 
 * <p/>
 * There are no strict rules that define what is equivalent or not.  
 * <p/>
 * Classes implementing {@link IXMLConfigurable} should offer the following
 * XML configuration usage:
 * <pre>
 *  &lt;metadataChecksummer 
 *      class="(class)"&gt;
 *      keep="[false|true]"
 *      targetField="(optional metadata field to store the checksum)" /&gt;
 * </pre>
 * <code>targetField</code> is ignored unless the <code>keep</code> 
 * attribute is set to <code>true</code>.
 * 
 * @author Pascal Essiembre
 * @see AbstractMetadataChecksummer
 */
public interface IMetadataChecksummer {


    /**
     * Creates a metadata checksum.
     * @param metadata all metadata values
     * @return a checksum value
     */
    String createMetadataChecksum(Properties metadata);
}
