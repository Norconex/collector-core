/* Copyright 2014-20178 Norconex Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.doc.CollectorMetadata;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;

/**
 * Abstract implementation of {@link IMetadataChecksummer} giving the option
 * to keep the generated checksum.  The checksum can be stored
 * in a target field name specified.  If no target field name is specified,
 * it stores it under the
 * metadata field name {@link CollectorMetadata#COLLECTOR_CHECKSUM_METADATA}.
 * <br><br>
 * <b>Implementors do not need to store the checksum themselves, this abstract
 * class does it.</b>
 * <br><br>
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

    private static final Logger LOG = LoggerFactory.getLogger(
			AbstractMetadataChecksummer.class);

	private boolean keep;
    private String targetField = CollectorMetadata.COLLECTOR_CHECKSUM_METADATA;

    @Override
    public final String createMetadataChecksum(Properties metadata) {
        String checksum = doCreateMetaChecksum(metadata);
        if (isKeep()) {
            String field = getTargetField();
            if (StringUtils.isBlank(field)) {
                field = CollectorMetadata.COLLECTOR_CHECKSUM_METADATA;
            }
            metadata.addString(field, checksum);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Meta checksum stored in {}", field);
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
     * Defaults to {@link CollectorMetadata#COLLECTOR_CHECKSUM_METADATA}.
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
    public final void loadFromXML(XML xml) {
        setKeep(xml.getBoolean("@keep", keep));
        setTargetField(xml.getString("@targetField", targetField));
        loadChecksummerFromXML(xml);
    }
    protected abstract void loadChecksummerFromXML(XML xml);

    @Override
    public final void saveToXML(XML xml) {
        xml.setAttribute("keep", isKeep());
        xml.setAttribute("targetField", getTargetField());
        saveChecksummerToXML(xml);
    }
    protected abstract void saveChecksummerToXML(XML xml);

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
