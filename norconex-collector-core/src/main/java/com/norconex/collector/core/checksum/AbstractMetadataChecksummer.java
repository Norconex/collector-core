/* Copyright 2010-2014 Norconex Inc.
 * 
 * This file is part of Norconex HTTP Collector.
 * 
 * Norconex HTTP Collector is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex HTTP Collector is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex HTTP Collector. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.checksum;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * Abstract implementation of {@link IMetadataChecksummer} giving the option
 * to keep the generated checksum.  The checksum can be stored
 * in a target field name specified.  If no target field name is specified,
 * it stores it under the 
 * metadata field name {@link CollectorMetadata#COLLECTOR_CHECKSUM_META}. 
 * <p/>
 * Implementors do not need to store the checksum themselves, this abstract
 * class does it.
 * <p/>
 * Implementors should offer this XML configuration usage:
 * <pre>
 *  &lt;metadataChecksummer 
 *      class="(subclass)"&gt;
 *      keep="[false|true]"
 *      targetField="(optional metadata field to store the checksum)" /&gt;
 * </pre>
 * <code>targetField</code> is ignored unless the <code>keep</code> 
 * attribute is set to <code>true</code>.
 * @author Pascal Essiembre
 */
public abstract class AbstractMetadataChecksummer 
        implements IMetadataChecksummer, IXMLConfigurable {

    private static final long serialVersionUID = -3821511734751759664L;

    private static final Logger LOG = LogManager.getLogger(
			AbstractMetadataChecksummer.class);
    
	private boolean keep;
    private String targetField = CollectorMetadata.COLLECTOR_CHECKSUM_META;
	
    @Override
    public final String createMetadataChecksum(Properties metadata) {
        String checksum = doCreateMetaChecksum(metadata);
        if (isKeep()) {
            String field = targetField;
            if (StringUtils.isBlank(field)) {
                field = CollectorMetadata.COLLECTOR_CHECKSUM_META;
            }
            metadata.addString(getTargetField(), checksum);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Meta checksum stored in " + targetField);
            }
        }
        return checksum;
    }
    
    protected abstract String doCreateMetaChecksum(Properties metadata);

	/**
	 * Whether to keep the metadata checksum value as a new metadata field.
	 * @return <code>true</code> to keep the checksum
	 */
	public boolean isKeep() {
        return keep;
    }
    /**
     * Sets whether to keep the metadata checksum value as a new metadata field.
     * @param keep <code>true</code> to keep the checksum
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /**
     * Gets the metadata field to use to store the checksum value.
     * Defaults to {@link CollectorMetadata#COLLECTOR_CHECKSUM_META}.  
     * Only applicable if {@link #isKeep()} returns {@code true}
     * @return metadata field name
     */
    public String getTargetField() {
        return targetField;
    }
    /**
     * Sets the metadata field name to use to store the checksum value.
     * @param targetField the metadata field name
     */
    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    @Override
    public final void loadFromXML(Reader in) {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        setKeep(xml.getBoolean("[@keep]", keep));
        setTargetField(xml.getString("[@targetField]", targetField));
        loadChecksummerFromXML(xml);
    }
    protected abstract void loadChecksummerFromXML(XMLConfiguration xml);
    
    @Override
    public final void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("metadataChecksummer");
            writer.writeAttributeString("class", getClass().getCanonicalName());
            writer.writeAttributeBoolean("keep", isKeep());
            writer.writeAttributeString("targetField", getTargetField());

            saveChecksummerToXML(writer);
            
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }        
    }
    protected abstract void saveChecksummerToXML(
            EnhancedXMLStreamWriter writer) throws XMLStreamException;

}
