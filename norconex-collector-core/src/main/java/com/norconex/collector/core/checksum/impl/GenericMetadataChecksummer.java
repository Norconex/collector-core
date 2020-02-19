/* Copyright 2015-2018 Norconex Inc.
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.checksum.AbstractMetadataChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.collector.core.checksum.IMetadataChecksummer;
import com.norconex.collector.core.doc.CrawlDocMetadata;
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * Generic implementation of {@link IMetadataChecksummer} that uses
 * specified source field names and their values for the checksum. The name
 * and values are simply returned as is, joined using this format:
 * <code>fieldName=fieldValue;fieldName=fieldValue;...</code>.
 * </p>
 * <p>
 * You have the option to keep the checksum as a document metadata field.
 * When {@link #setKeep(boolean)} is <code>true</code>, the checksum will be
 * stored in the target field name specified. If you do not specify any,
 * it stores it under the metadata field name
 * {@link CrawlDocMetadata#COLLECTOR_CHECKSUM_METADATA}.
 * </p>
 * <p>
 * <b>Since 1.9.0</b>, it is possible to use regular expressions to match
 * fields.
 * Use <code>sourceFields</code> to list all fields to use, separated by commas.
 * Use <code>sourceFieldsRegex</code> to match fields to use using a regular
 * expression.
 * Both <code>sourceFields</code> and <code>sourceFieldsRegex</code> can be used
 * together. Matching fields from both will be combined, in the order
 * provided/matched, starting with <code>sourceFields</code> entries.
 * </p>
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;metadataChecksummer
 *      class="com.norconex.collector.core.checksum.impl.GenericMetadataChecksummer"
 *      disabled="[false|true]"
 *      keep="[false|true]"
 *      targetField="(field to store checksum)"&gt;
 *    &lt;sourceFields&gt;
 *        (optional coma-separated list fields used to create checksum)
 *    &lt;/sourceFields&gt;
 *    &lt;sourceFieldsRegex&gt;
 *      (regular expression matching fields used to create checksum)
 *    &lt;/sourceFieldsRegex&gt;
 *  &lt;/metadataChecksummer&gt;
 * </pre>
 *
 * <h4>Usage example:</h4>
 * <p>
 * The following uses a combination of two (fictitious) fields called
 * "docLastModified" and "docSize" to make the checksum.
 * </p>
 * <pre>
 *  &lt;metadataChecksummer
 *      class="com.norconex.collector.core.checksum.impl.GenericMetadataChecksummer"&gt;
 *    &lt;sourceFields&gt;docLastModified,docSize&lt;/sourceFields&gt;
 *  &lt;/metadataChecksummer&gt;
 * </pre>
 * @since 1.2.0
 * @author Pascal Essiembre
 */
public class GenericMetadataChecksummer extends AbstractMetadataChecksummer {

    private final List<String> sourceFields = new ArrayList<>();
    private String sourceFieldsRegex;
    private boolean disabled;

    @Override
    protected String doCreateMetaChecksum(Properties metadata) {
        if (disabled) {
            return null;
        }
        return ChecksumUtil.metadataChecksumPlain(
                metadata, getSourceFieldsRegex(), getSourceFields());
    }

    /**
     * Gets the metadata fields used to construct a checksum.
     * @return fields to use for checksum
     */
    public List<String> getSourceFields() {
        return Collections.unmodifiableList(sourceFields);
    }
    /**
     * Sets the metadata header fields used construct a checksum.
     * @param sourceFields fields to use for checksum
     */
    public void setSourceFields(String... sourceFields) {
        setSourceFields(Arrays.asList(sourceFields));
    }
    /**
     * Sets the metadata header fields used construct a checksum.
     * @param sourceFields fields to use for checksum
     * @since 2.0.0
     */
    public void setSourceFields(List<String> sourceFields) {
        CollectionUtil.setAll(this.sourceFields, sourceFields);
    }

    /**
     * Gets the regular expression matching metadata fields used to construct
     * a checksum.
     * @return regular expression
     * @since 1.9.0
     */
    public String getSourceFieldsRegex() {
        return sourceFieldsRegex;
    }
    /**
     * Sets the regular expression matching metadata fields used construct
     * a checksum.
     * @param sourceFieldsRegex regular expression
     * @since 1.9.0
     */
    public void setSourceFieldsRegex(String sourceFieldsRegex) {
        this.sourceFieldsRegex = sourceFieldsRegex;
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
    protected void loadChecksummerFromXML(XML xml) {
        setDisabled(xml.getBoolean("@disabled", disabled));
        setSourceFields(
                xml.getDelimitedStringList("sourceFields", sourceFields));
        setSourceFieldsRegex(
                xml.getString("sourceFieldsRegex", getSourceFieldsRegex()));
    }

    @Override
    protected void saveChecksummerToXML(XML xml) {
        xml.setAttribute("disabled", isDisabled());
        xml.addDelimitedElementList("sourceFields", sourceFields);
        xml.addElement("sourceFieldsRegex", getSourceFieldsRegex());
    }

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
