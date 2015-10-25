/* Copyright 2015 Norconex Inc.
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
package com.norconex.collector.core.checksum.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.collector.core.checksum.AbstractMetadataChecksummer;
import com.norconex.collector.core.checksum.IMetadataChecksummer;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * <p>
 * Generic implementation of {@link IMetadataChecksummer} that uses 
 * specified source field names and their values for the checksum. The name
 * and values are simply returned as is, joined using this format:
 * <code>fieldName=fieldValue;fieldName=fieldValue;...</code>.
 * </p>
 * <p>
 * XML configuration usage:
 * </p>
 * <pre>
 *  &lt;metadataChecksummer 
 *      class="com.norconex.collector.core.checksum.impl.HttpMetadataChecksummer"
 *      disabled="[false|true]"
 *      keep="[false|true]"
 *      targetField="(field to store checksum)"&gt;
 *    &lt;sourceFields&gt;
 *        (optional coma-separated list fields used to create checksum)
 *    &lt;/sourceFields&gt;
 *  &lt;/documentChecksummer&gt;
 * </pre>
 * @since 1.2.0
 * @author Pascal Essiembre
 */
public class GenericMetadataChecksummer extends AbstractMetadataChecksummer {

	private static final Logger LOG = LogManager.getLogger(
			GenericMetadataChecksummer.class);

    private String[] sourceFields = null;
    private boolean disabled;
	
    @Override
    protected String doCreateMetaChecksum(Properties metadata) {
        if (disabled || ArrayUtils.isEmpty(sourceFields)) {
            return null;
        }
        
        StringBuilder b = new StringBuilder();
        List<String> fields = 
                new ArrayList<String>(Arrays.asList(sourceFields));
        // Sort to make sure field order does not affect checksum.
        Collections.sort(fields);
        for (String field : fields) {
            List<String> values = metadata.getStrings(field);
            if (values != null) {
                for (String value : values) {
                    if (StringUtils.isNotBlank(value)) {
                        b.append(field).append('=');
                        b.append(value).append(';');
                    }
                }
            }
        }
        String checksum = b.toString();
        if (StringUtils.isNotBlank(checksum)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Metadata checksum from "
                        + StringUtils.join(sourceFields, ',')
                        + " : " + checksum);
            }
            return checksum;
        }
        return null;
    }

    /**
     * Gets the metadata fields used to construct a checksum.
     * @return fields to use for checksum
     */
    public String[] getSourceFields() {
        return sourceFields;
    }
    /**
     * Sets the metadata header fields used construct a checksum.  Specifying
     * <code>null</code> has the same effect as setting 
     * {@link #setDisabled(boolean)} to <code>true</code>.
     * @param fields fields to use for checksum
     */
    public void setSourceFields(String... fields) {
        this.sourceFields = fields;
    }
	
    /**
     * Whether this checksummer is disabled or not. When disabled, not
     * checksum will be created (the checksum will be <code>null</code>).
     * @return <code>true</code> if disabled
     */
    public boolean isDisabled() {
        return disabled;
    }
    /**
     * Sets whether this checksummer is disabled or not. When disabled, not
     * checksum will be created (the checksum will be <code>null</code>).
     * @param disabled <code>true</code> if disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }	
	
    @Override
    protected void loadChecksummerFromXML(XMLConfiguration xml) {
        setDisabled(xml.getBoolean("[@disabled]", disabled));
        String flds = xml.getString("sourceFields", null);
        if (StringUtils.isBlank(flds)) {
            sourceFields = null;
        } else {
            sourceFields = flds.split("\\s*,\\s*");
        }        
    }

    @Override
    protected void saveChecksummerToXML(EnhancedXMLStreamWriter writer)
            throws XMLStreamException {
        writer.writeAttributeBoolean("disabled", isDisabled());
        writer.writeElementString(
                "sourceFields", StringUtils.join(sourceFields, ','));
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GenericMetadataChecksummer)) {
            return false;
        }
        GenericMetadataChecksummer castOther = 
                (GenericMetadataChecksummer) other;
        return new EqualsBuilder()
                .appendSuper(super.equals(castOther))
                .append(disabled, castOther.disabled)
                .append(sourceFields, castOther.sourceFields)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(disabled)
                .append(sourceFields)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("disabled", disabled)
                .append("sourceFields", sourceFields)
                .toString();
    }
}
