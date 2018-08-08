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
package com.norconex.collector.core.filter.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * <p>
 * Filters a reference based on a comma-separated list of extensions.
 * Extensions are typically the last characters of a file name, after the
 * last dot.
 * </p>
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.ExtensionReferenceFilter"
 *          onMatch="[include|exclude]"
 *          caseSensitive="[false|true]" &gt;
 *      (comma-separated list of extensions)
 *  &lt;/filter&gt;
 * </pre>
 *
 * <h4>Usage example:</h4>
 * <p>
 * The following example will only accept references with the following
 * extensions: .html, .htm, .php, and .asp.
 * </p>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.ExtensionReferenceFilter"&gt;
 *      html,htm,php,asp
 *  &lt;/filter&gt;
 * </pre>
 * @author Pascal Essiembre
 */
public class ExtensionReferenceFilter implements
        IOnMatchFilter,
        IReferenceFilter,
        IDocumentFilter,
        IMetadataFilter,
        IXMLConfigurable {

    private boolean caseSensitive;
    private String extensions;
    private String[] extensionParts;
    private OnMatch onMatch;

    public ExtensionReferenceFilter() {
        this(null, OnMatch.INCLUDE, false);
    }
    public ExtensionReferenceFilter(String extensions) {
        this(extensions, OnMatch.INCLUDE, false);
    }
    public ExtensionReferenceFilter(String extensions, OnMatch onMatch) {
        this(extensions, onMatch, false);
    }
    public ExtensionReferenceFilter(
            String extensions, OnMatch onMatch, boolean caseSensitive) {
        super();
        setExtensions(extensions);
        setOnMatch(onMatch);
        setCaseSensitive(caseSensitive);
    }

    @Override
    public OnMatch getOnMatch() {
        return onMatch;
    }
    public void setOnMatch(OnMatch onMatch) {
        this.onMatch = onMatch;
    }

    @Override
    public boolean acceptReference(String reference) {
        OnMatch safeOnMatch = OnMatch.includeIfNull(onMatch);

        if (StringUtils.isBlank(extensions)) {
            return safeOnMatch == OnMatch.INCLUDE;
        }
        String referencePath;
        try {
            URL referenceUrl = new URL(reference);
            referencePath = referenceUrl.getPath();
        } catch (MalformedURLException ex) {
            referencePath = reference;
        }

        String refExtension = FilenameUtils.getExtension(referencePath);

        for (String ext : extensionParts) {
            if (!isCaseSensitive() && ext.equalsIgnoreCase(refExtension)) {
                return safeOnMatch == OnMatch.INCLUDE;
            }
            if (isCaseSensitive() && ext.equals(refExtension)) {
                return safeOnMatch == OnMatch.INCLUDE;
            }
        }
        return safeOnMatch == OnMatch.EXCLUDE;
    }


    /**
     * @return the extensions
     */
    public String getExtensions() {
        return extensions;
    }
    public String[] getExtensionParts() {
        return extensionParts;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public final void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    public final void setExtensions(String extensions) {
        this.extensions = extensions;
        if (extensions != null) {
            this.extensionParts = extensions.split("\\s*,\\s*");
            for (int i = 0; i < this.extensionParts.length; i++) {
                this.extensionParts[i] = this.extensionParts[i].trim();
            }
        } else {
            this.extensionParts = ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }
    @Override
    public void loadFromXML(XML xml)  {
        setExtensions(xml.getString("."));
        setOnMatch(xml.getEnum(OnMatch.class, "@onMatch", onMatch));
        setCaseSensitive(xml.getBoolean("@caseSensitive", caseSensitive));
    }
    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("onMatch", onMatch);
        xml.setAttribute("caseSensitive", caseSensitive);
        xml.setTextContent(extensions);
    }

    @Override
    public boolean acceptDocument(ImporterDocument document) {
        return acceptReference(document.getReference());
    }
    @Override
    public boolean acceptMetadata(String reference, Properties metadata) {
        return acceptReference(reference);
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
