/* Copyright 2014-2020 Norconex Inc.
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

import com.norconex.collector.core.doc.CrawlDocMetadata;
import com.norconex.commons.lang.map.PropertySetter;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.Doc;

/**
 * <p>Abstract implementation of {@link IDocumentChecksummer} giving the option
 * to keep the generated checksum in a metadata field.
 * The checksum can be stored
 * in a target field name specified.  If no target field name is specified,
 * it stores it under the
 * metadata field name {@link CrawlDocMetadata#CHECKSUM_DOC}.
 * </p><p>
 * <b>Implementors do not need to store the checksum themselves, this abstract
 * class does it.</b>
 * </p><p>
 * Implementors should offer this XML configuration usage:
 * </p>
 * {@nx.xml #usage
 * <documentChecksummer
 *    class="(subclass)"
 *    keep="[false|true]"
 *    toField="(optional metadata field to store the checksum)"
 *    onSet="[append|prepend|replace|optional]" />
 * }
 * <p>
 * <code>toField</code> is ignored unless the <code>keep</code>
 * attribute is set to <code>true</code>.
 * </p>
 * @author Pascal Essiembre
 */
public abstract class AbstractDocumentChecksummer
        implements IDocumentChecksummer, IXMLConfigurable {

    private static final Logger LOG = LoggerFactory.getLogger(
			AbstractDocumentChecksummer.class);

	private boolean keep;
    private String toField = CrawlDocMetadata.CHECKSUM_DOC;
    private PropertySetter onSet;

    @Override
    public final String createDocumentChecksum(Doc document) {
        String checksum = doCreateDocumentChecksum(document);
        if (isKeep()) {
            String field = getToField();
            if (StringUtils.isBlank(field)) {
                field = CrawlDocMetadata.CHECKSUM_DOC;
            }
            PropertySetter.orAppend(onSet).apply(
                    document.getMetadata(), field, checksum);
            LOG.debug("Document checksum stored in {}", field);
        }
        return checksum;
    }

    protected abstract String doCreateDocumentChecksum(
            Doc document);

	/**
	 * Whether to keep the document checksum value as a new field in the
	 * document metadata.
	 * @return <code>true</code> to keep the checksum
	 */
	public boolean isKeep() {
        return keep;
    }
    /**
     * Sets whether to keep the document checksum value as a new field in the
     * document metadata.
     * @param keep <code>true</code> to keep the checksum
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    /**
     * Gets the metadata field to use to store the checksum value.
     * Defaults to {@link CrawlDocMetadata#CHECKSUM_METADATA}.
     * Only applicable if {@link #isKeep()} returns {@code true}
     * @return metadata field name
     * @deprecated Since 2.0.0, use {@link #getToField()}.
     */
    @Deprecated
    public String getTargetField() {
        return toField;
    }
    /**
     * Sets the metadata field name to use to store the checksum value.
     * @param targetField the metadata field name
     * @deprecated Since 2.0.0, use {@link #setToField(String)}.
     */
    @Deprecated
    public void setTargetField(String targetField) {
        this.toField = targetField;
    }

    /**
     * Gets the metadata field to use to store the checksum value.
     * Defaults to {@link CrawlDocMetadata#CHECKSUM_METADATA}.
     * Only applicable if {@link #isKeep()} returns {@code true}
     * @return metadata field name
     * @since 2.0.0
     */
    public String getToField() {
        return toField;
    }
    /**
     * Sets the metadata field name to use to store the checksum value.
     * @param toField the metadata field name
     * @since 2.0.0
     */
    public void setToField(String toField) {
        this.toField = toField;
    }

    /**
     * Gets the property setter to use when a value is set.
     * @return property setter
     * @since 2.0.0
     */
    public PropertySetter getOnSet() {
        return onSet;
    }
    /**
     * Sets the property setter to use when a value is set.
     * @param onSet property setter
     * @since 2.0.0
     */
    public void setOnSet(PropertySetter onSet) {
        this.onSet = onSet;
    }

    @Override
    public final void loadFromXML(XML xml) {
        setKeep(xml.getBoolean("@keep", keep));

        xml.checkDeprecated("@targetField", "@toField", false);
        setToField(xml.getString("@targetField", toField));
        setToField(xml.getString("@toField", toField)); // overwrites above line

        setOnSet(PropertySetter.fromXML(xml, onSet));
        loadChecksummerFromXML(xml);
    }
    protected abstract void loadChecksummerFromXML(XML xml);

    @Override
    public final void saveToXML(XML xml) {
        xml.setAttribute("keep", isKeep());
        xml.setAttribute("toField", getToField());
        PropertySetter.toXML(xml, getOnSet());
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
