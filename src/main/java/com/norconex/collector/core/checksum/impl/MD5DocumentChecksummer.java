/* Copyright 2014-2018 Norconex Inc.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.CollectorException;
import com.norconex.collector.core.checksum.AbstractDocumentChecksummer;
import com.norconex.collector.core.checksum.ChecksumUtil;
import com.norconex.collector.core.checksum.IDocumentChecksummer;
import com.norconex.collector.core.doc.CrawlDocMetadata;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.text.TextMatcher.Method;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.Doc;

/**
 * <p>Implementation of {@link IDocumentChecksummer} which
 * returns a MD5 checksum value of the extracted document content unless
 * one or more given source fields are specified, in which case the MD5
 * checksum value is constructed from those fields.  This checksum is normally
 * performed right after the document has been imported.
 * </p>
 * <p>
 * You have the option to keep the checksum as a document metadata field.
 * When {@link #setKeep(boolean)} is <code>true</code>, the checksum will be
 * stored in the target field name specified. If you do not specify any,
 * it stores it under the metadata field name
 * {@link CrawlDocMetadata#CHECKSUM_METADATA}.
 * </p>
 * <p>
 * <p>
 * <b>Since 1.9.0</b>, it is possible to use a combination of document content
 * and fields to create the checksum by setting
 * <code>combineFieldsAndContent</code> to <code>true</code>.
 * If you combine fields and content but you don't define a field matcher,
 * it will be the equivalent of adding all fields.
 * If you do not combine the two, specifying a field matcher
 * will ignore the content while specifying none will only use the content.
 * </p>
 *
 * {@nx.xml.usage
 * <documentChecksummer
 *     class="com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer"
 *     disabled="[false|true]"
 *     combineFieldsAndContent="[false|true]"
 *     keep="[false|true]"
 *     toField="(optional metadata field to store the checksum)">
 *
 *   <fieldMatcher {@nx.include com.norconex.commons.lang.text.TextMatcher#matchAttributes}>
 *     (expression matching fields used to create the checksum)
 *   </fieldMatcher>
 * </documentChecksummer>
 * }
 * <p>
 * <code>toField</code> is ignored unless the <code>keep</code>
 * attribute is set to <code>true</code>.
 * </p>
 * <p>
 * This implementation can be disabled in your
 * configuration by specifying <code>disabled="true"</code>. When disabled,
 * the checksum returned is always <code>null</code>.
 * </p>
 *
 * {@nx.xml.example
 * <documentChecksummer class="MD5DocumentChecksummer" />
 * }
 * <p>
 * The above example uses the document body (default) to make the checksum.
 * </p>
 *
 * @author Pascal Essiembre
 */
@SuppressWarnings("javadoc")
public class MD5DocumentChecksummer extends AbstractDocumentChecksummer {

    private final TextMatcher fieldMatcher = new TextMatcher();
	private boolean disabled;
	private boolean combineFieldsAndContent;

    @Override
    public String doCreateDocumentChecksum(Doc document) {
        if (disabled) {
            return null;
        }

        // fields
        TextMatcher fm = new TextMatcher(fieldMatcher);
        boolean isSourceFieldsSet = isFieldMatcherSet();
        if (isCombineFieldsAndContent() && !isSourceFieldsSet) {
            fm.setMethod(Method.REGEX);
            fm.setPattern(".*");
        }
        StringBuilder b = new StringBuilder();
        if (isSourceFieldsSet || isCombineFieldsAndContent()) {
            String checksum = ChecksumUtil.metadataChecksumMD5(
                    document.getMetadata(), fieldMatcher);
            if (checksum != null) {
                b.append(checksum);
                b.append('|');
            }
        }

        // document
        if (isCombineFieldsAndContent() || !isSourceFieldsSet) {
            try {
                b.append(ChecksumUtil.checksumMD5(document.getInputStream()));
            } catch (IOException e) {
                throw new CollectorException(
                        "Cannot create document checksum on : "
                                + document.getReference(), e);
            }
        }

        return StringUtils.trimToNull(b.toString());
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

    private boolean isFieldMatcherSet() {
        return StringUtils.isNotBlank(fieldMatcher.getPattern());
    }

	/**
     * Gets the fields used to construct a MD5 checksum.
     * @return fields to use to construct the checksum
     * @since 1.2.0
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
     * Sets the fields used to construct a MD5 checksum.
     * @param sourceFields fields to use to construct the checksum
     * @since 1.2.0
     * @deprecated Since 2.0.0, use {@link #setFieldMatcher(TextMatcher)}.
     */
    @Deprecated
    public void setSourceFields(String... sourceFields) {
        setSourceFields(Arrays.asList(sourceFields));
    }
    /**
     * Sets the fields used to construct a MD5 checksum.
     * @param sourceFields fields to use to construct the checksum
     * @since 2.0.0
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
     * @deprecated Since 2.0.0, use {@link #setFieldMatcher(TextMatcher)}.
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

    /**
     * Gets whether we are combining the fields and content checksums.
     * @return <code>true</code> if combining fields and content checksums
     * @since 1.9.0
     */
    public boolean isCombineFieldsAndContent() {
        return combineFieldsAndContent;
    }
    /**
     * Sets whether to combine the fields and content checksums.
     * @param combineFieldsAndContent <code>true</code> if combining fields
     *        and content checksums
     * @since 1.9.0
     */
    public void setCombineFieldsAndContent(boolean combineFieldsAndContent) {
        this.combineFieldsAndContent = combineFieldsAndContent;
    }

    @Override
	protected void loadChecksummerFromXML(XML xml) {
        setDisabled(xml.getBoolean("@disabled", disabled));
        setCombineFieldsAndContent(xml.getBoolean(
                "@combineFieldsAndContent", combineFieldsAndContent));
        xml.checkDeprecated("sourceFields", "fieldMatcher", false);
        xml.checkDeprecated("sourceFieldsRegex", "fieldMatcher", false);
        setSourceFields(xml.getDelimitedStringList("sourceFields"));
        setSourceFieldsRegex(xml.getString("sourceFieldsRegex"));
        fieldMatcher.loadFromXML(xml.getXML("fieldMatcher"));
    }
	@Override
	protected void saveChecksummerToXML(XML xml) {
        xml.setAttribute("disabled", disabled);
        xml.setAttribute("combineFieldsAndContent", combineFieldsAndContent);
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
