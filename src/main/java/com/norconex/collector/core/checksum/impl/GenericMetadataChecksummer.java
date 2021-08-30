/* Copyright 2015-2021 Norconex Inc.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.checksum.AbstractMetadataChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.collector.core.checksum.IMetadataChecksummer;
import com.norconex.collector.core.doc.CrawlDocMetadata;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.text.TextMatcher.Method;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * Generic implementation of {@link IMetadataChecksummer} that uses
 * specified field names and their values to create a checksum. The name
 * and values are simply returned as is, joined using this format:
 * <code>fieldName=fieldValue;fieldName=fieldValue;...</code>.
 * </p>
 * <p>
 * You have the option to keep the checksum as a document metadata field.
 * When {@link #setKeep(boolean)} is <code>true</code>, the checksum will be
 * stored in the target field name specified. If you do not specify any,
 * it stores it under the metadata field name
 * {@link CrawlDocMetadata#CHECKSUM_METADATA}.
 * </p>
 * {@nx.xml.usage
 * <metadataChecksummer
 *     class="com.norconex.collector.core.checksum.impl.GenericMetadataChecksummer"
 *     keep="[false|true]"
 *     toField="(optional field to store the checksum)">
 *
 *   <fieldMatcher {@nx.include com.norconex.commons.lang.text.TextMatcher#matchAttributes}>
 *     (expression matching fields used to create the checksum)
 *   </fieldMatcher>
 * </metadataChecksummer>
 * }
 *
 * <p>
 * <code>toField</code> is ignored unless the <code>keep</code>
 * attribute is set to <code>true</code>.
 * </p>
 *
 * {@nx.xml.example
 * <metadataChecksummer class="GenericMetadataChecksummer">
 *   <fieldMatcher method="csv">docLastModified,docSize</fieldMatcher>
 * </metadataChecksummer>
 * }
 * <p>
 * The above example uses a combination of two (fictitious) fields called
 * "docLastModified" and "docSize" to make the checksum.
 * </p>
 *
 * <p>
 * <b>Since 2.0.0</b>, a self-closing
 * <code>&lt;metadataChecksummer/&gt;</code> tag without any attributes
 * is used to disable checksum generation.
 * </p>
 *
 * @since 1.2.0
 * @author Pascal Essiembre
 */
@SuppressWarnings("javadoc")
public class GenericMetadataChecksummer extends AbstractMetadataChecksummer {

    private final TextMatcher fieldMatcher = new TextMatcher();

    @Override
    protected String doCreateMetaChecksum(Properties metadata) {
        return ChecksumUtil.metadataChecksumPlain(metadata, fieldMatcher);
    }

    /**
     * Gets the field matcher.
     * @return field matcher
     * @since 2.0.0
     */
    public TextMatcher getFieldMatcher() {
        return fieldMatcher;
    }
    /**
     * Sets the field matcher.
     * @param fieldMatcher field matcher
     * @since 2.0.0
     */
    public void setFieldMatcher(TextMatcher fieldMatcher) {
        this.fieldMatcher.copyFrom(fieldMatcher);
    }

    /**
     * Gets the metadata fields used to construct a checksum.
     * @return fields to use for checksum
     * @deprecated Since 2.0.0, use {@link #getFieldMatcher()}.
     */
    @Deprecated
    public List<String> getSourceFields() {
        if (fieldMatcher.getMethod() == Method.CSV
                && StringUtils.isNotBlank(fieldMatcher.getPattern())) {
            return Arrays.asList(
                    fieldMatcher.getPattern().split("(?<!\\\\)\\|"));
        }
        return Collections.emptyList();
    }
    /**
     * Sets the metadata header fields used construct a checksum.
     * @param sourceFields fields to use for checksum
     * @deprecated Since 2.0.0, use {@link #setFieldMatcher(TextMatcher)}.
     */
    @Deprecated
    public void setSourceFields(String... sourceFields) {
        setSourceFields(Arrays.asList(sourceFields));
    }
    /**
     * Sets the metadata header fields used construct a checksum.
     * @param sourceFields fields to use for checksum
     * @deprecated Since 2.0.0, use {@link #setFieldMatcher(TextMatcher)}.
     */
    @Deprecated
    public void setSourceFields(List<String> sourceFields) {
        if (sourceFields != null) {
            fieldMatcher.setMethod(Method.CSV);
            fieldMatcher.setPattern(
                    sourceFields.stream().collect(Collectors.joining(",")));
        }
    }

    /**
     * Gets the regular expression matching metadata fields used to construct
     * a checksum.
     * @return regular expression
     * @since 1.9.0
     * @deprecated Since 2.0.0, use {@link #getFieldMatcher()}.
     */
    @Deprecated
    public String getSourceFieldsRegex() {
        if (fieldMatcher.getMethod() == Method.REGEX
                && StringUtils.isNotBlank(fieldMatcher.getPattern())) {
            return fieldMatcher.getPattern();
        }
        return null;
    }
    /**
     * Sets the regular expression matching metadata fields used construct
     * a checksum.
     * @param sourceFieldsRegex regular expression
     * @since 1.9.0
     * @deprecated Since 2.0.0, use {@link #setFieldMatcher(TextMatcher)}.
     */
    @Deprecated
    public void setSourceFieldsRegex(String sourceFieldsRegex) {
        fieldMatcher.setMethod(Method.REGEX);
        fieldMatcher.setPattern(sourceFieldsRegex);
    }

    /**
     * Deprecated.
     * @return always <code>false</code>
     * @deprecated Since 2.0.0, not having a checksummer defined or
     * setting one explicitly to <code>null</code> effectively disables
     * it.
     */
    @Deprecated
    public boolean isDisabled() {
        return false;
    }
    /**
     * Deprecated. Invoking this method has no effect
     * @param disabled argument is ignored
     * @deprecated Since 2.0.0, not having a checksummer defined or
     * setting one explicitly to <code>null</code> effectively disable
     * it.
     */
    @Deprecated
    public void setDisabled(boolean disabled) {
        //NOOP
    }

    @Override
    protected void loadChecksummerFromXML(XML xml) {
        xml.checkDeprecated("@disabled",
                "Use self-closing <metadataChecksummer/>", false);
        xml.checkDeprecated("sourceFields", "fieldMatcher", false);
        xml.checkDeprecated("sourceFieldsRegex", "fieldMatcher", false);
        setSourceFields(xml.getDelimitedStringList("sourceFields"));
        setSourceFieldsRegex(xml.getString("sourceFieldsRegex"));
        fieldMatcher.loadFromXML(xml.getXML("fieldMatcher"));
    }

    @Override
    protected void saveChecksummerToXML(XML xml) {
        fieldMatcher.saveToXML(xml.addElement("fieldMatcher"));
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
