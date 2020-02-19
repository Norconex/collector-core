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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.ImporterDocument;

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
 * <p>
 * <b>Since 1.9.0</b>, it is possible to use a combination of document content
 * and fields to create the checksum by setting
 * <code>combineFieldsAndContent</code> to <code>true</code>.
 * If you combine fields and content but you don't define any source fields,
 * it will be the equivalent of adding all fields.
 * If you do not combine the two, specifying one or more source fields
 * will ignore the content while specifying none will only use the content.
 * </p>
 *
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;documentChecksummer
 *      class="com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer"
 *      disabled="[false|true]"
 *      combineFieldsAndContent="[false|true]"
 *      keep="[false|true]"
 *      targetField="(optional metadata field to store the checksum)"&gt;
 *    &lt;sourceFields&gt;
 *        (optional coma-separated list fields used to create checksum)
 *    &lt;/sourceFields&gt;
 *    &lt;sourceFieldsRegex&gt;
 *      (regular expression matching fields used to create checksum)
 *    &lt;/sourceFieldsRegex&gt;
 *  &lt;/documentChecksummer&gt;
 * </pre>
 * <p>
 * <code>targetField</code> is ignored unless the <code>keep</code>
 * attribute is set to <code>true</code>.
 * </p>
 * <p>
 * This implementation can be disabled in your
 * configuration by specifying <code>disabled="true"</code>. When disabled,
 * the checksum returned is always <code>null</code>.
 * </p>
 *
 * <h4>Usage example:</h4>
 * <p>
 * The following uses the document body (default) to make the checksum.
 * </p>
 * <pre>
 *  &lt;documentChecksummer
 *      class="com.norconex.collector.core.checksum.impl.MD5DocumentChecksummer" /&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 */
public class MD5DocumentChecksummer extends AbstractDocumentChecksummer {

	private final List<String> sourceFields = new ArrayList<>();
	private String sourceFieldsRegex;
	private boolean disabled;
	private boolean combineFieldsAndContent;

    @Override
    public String doCreateDocumentChecksum(ImporterDocument document) {
        if (disabled) {
            return null;
        }

        // fields
        String fieldRegex = getSourceFieldsRegex();
        boolean isSourceFieldsSet = isSourceFieldsSet();
        if (isCombineFieldsAndContent() && !isSourceFieldsSet) {
            fieldRegex = ".*";
        }
        StringBuilder b = new StringBuilder();
        if (isSourceFieldsSet || isCombineFieldsAndContent()) {
            String checksum = ChecksumUtil.metadataChecksumMD5(
                    document.getMetadata(), fieldRegex, getSourceFields());
            if (checksum != null) {
                b.append(checksum);
                b.append('|');
            }
        }

        // document
        if (isCombineFieldsAndContent() || !isSourceFieldsSet) {
            try {
                b.append(ChecksumUtil.checksumMD5(
                        document.getInputStream()));
            } catch (IOException e) {
                throw new CollectorException(
                        "Cannot create document checksum on : "
                                + document.getReference(), e);
            }
        }

        return StringUtils.trimToNull(b.toString());
    }

    private boolean isSourceFieldsSet() {
        return !sourceFields.isEmpty()
                || StringUtils.isNotBlank(getSourceFieldsRegex());
    }

	/**
     * Gets the fields used to construct a MD5 checksum.
     * @return fields to use to construct the checksum
     * @since 1.2.0
     */
    public List<String> getSourceFields() {
        return Collections.unmodifiableList(sourceFields);
    }
    /**
     * Sets the fields used to construct a MD5 checksum.
     * @param sourceFields fields to use to construct the checksum
     * @since 1.2.0
     */
    public void setSourceFields(String... sourceFields) {
        setSourceFields(Arrays.asList(sourceFields));
    }
    /**
     * Sets the fields used to construct a MD5 checksum.
     * @param sourceFields fields to use to construct the checksum
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
        setSourceFields(
                xml.getDelimitedStringList("sourceFields", sourceFields));
        setSourceFieldsRegex(
                xml.getString("sourceFieldsRegex", sourceFieldsRegex));
    }
	@Override
	protected void saveChecksummerToXML(XML xml) {
        xml.setAttribute("disabled", disabled);
        xml.setAttribute("combineFieldsAndContent", combineFieldsAndContent);
        xml.addDelimitedElementList("sourceFields", sourceFields);
        if (sourceFieldsRegex != null) {
            xml.addElement("sourceFieldsRegex", sourceFieldsRegex);
        }
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
