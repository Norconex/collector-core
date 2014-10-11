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
package com.norconex.collector.core.checksum.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;
import com.norconex.importer.doc.ImporterDocument;

/**
 * Implementation of {@link IDocumentChecksummer} which 
 * returns a MD5 checksum value of the extracted document content unless
 * a given field is specified.  If a field is specified, the MD5 checksum
 * value is constructed from that field.  This checksum is normally 
 * performed right after the document has been imported.
 * <p/>
 * You can optionally have the checksum value stored with the
 * document under the field name 
 * {@link CollectorMetadata#COLLECTOR_CHECKSUM_DOC}
 * or one you specify.
 * <p>
 * XML configuration usage:
 * </p>
 * <pre>
 *  &lt;documentChecksummer 
 *      class="com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer"&gt;
 *      sourceField="(optional field used to create checksum)"
 *      keep="[false|true]"
 *      targetField="(optional metadata field to store the checksum)" /&gt;
 * </pre>
 * <code>targetField</code> is ignored unless the <code>keep</code> 
 * attribute is set to <code>true</code>.
 * @author Pascal Essiembre
 */
public class MD5DocumentChecksummer extends AbstractDocumentChecksummer {

	private static final Logger LOG = LogManager.getLogger(
			MD5DocumentChecksummer.class);
    
	private String sourceField = null;
	
    @Override
    public String doCreateDocumentChecksum(ImporterDocument document) {
		// If field is not specified, perform checksum on whole text file.
		if (StringUtils.isNotBlank(sourceField)) {
    		String value = document.getMetadata().getString(sourceField);
    		if (StringUtils.isNotBlank(value)) {
    			String checksum = DigestUtils.md5Hex(value);
    			if (LOG.isDebugEnabled()) {
                    LOG.debug("Document checksum from " + sourceField 
                            + " : " + checksum);
    			}
    			return checksum;
    		}
    		return null;
    	}
		try {
		    InputStream is = document.getContent();
	    	String checksum = DigestUtils.md5Hex(is);
			LOG.debug("Document checksum from content: " + checksum);
	    	is.close();
	    	return checksum;
		} catch (IOException e) {
			throw new CollectorException("Cannot create document checksum on : " 
			        + document.getReference(), e);
		}
    }

	/**
	 * Gets the specific field to construct a MD5 checksum on.  Default
	 * is <code>null</code> (checksum is performed on entire content).
	 * @return field to perform checksum on
	 */
	public String getSourceField() {
		return sourceField;
	}
    /**
     * Sets the specific field to construct a MD5 checksum on.  Specifying
     * <code>null</code> means all content will be used.
     * @param field field to perform checksum on
     */
	public void setSourceField(String field) {
		this.sourceField = field;
	}

	@Override
	protected void loadChecksummerFromXML(XMLConfiguration xml) {
        setSourceField(xml.getString("[@sourceField]", sourceField));
    }
	@Override
	protected void saveChecksummerToXML(EnhancedXMLStreamWriter writer)
	        throws XMLStreamException {
        writer.writeAttribute("sourceField", getSourceField());
    }

}
