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
package com.norconex.collector.core.filter.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.commons.lang.collection.CollectionUtil;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.Doc;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * <p>
 * Filters a reference based on a comma-separated list of extensions.
 * Extensions are typically the last characters of a file name, after the
 * last dot.
 * </p>
 *
 * {@nx.xml.usage
 * <filter class="com.norconex.collector.core.filter.impl.ExtensionReferenceFilter"
 *     onMatch="[include|exclude]"
 *     caseSensitive="[false|true]" >
 *   (comma-separated list of extensions)
 * </filter>
 * }
 *
 * {@nx.xml.example
 * <filter class="com.norconex.collector.core.filter.impl.ExtensionReferenceFilter">
 *   html,htm,php,asp
 * </filter>
 * }
 * <p>
 * The above example will only accept references with the following
 * extensions: .html, .htm, .php, and .asp.
 * </p>
 * @author Pascal Essiembre
 */
public class ExtensionReferenceFilter implements
        IOnMatchFilter,
        IReferenceFilter,
        IDocumentFilter,
        IMetadataFilter,
        IXMLConfigurable {

    private boolean caseSensitive;
    private final Set<String> extensions = new HashSet<>();
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

        if (extensions.isEmpty()) {
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

        for (String ext : extensions) {
            if (!isCaseSensitive() && ext.equalsIgnoreCase(refExtension)) {
                return safeOnMatch == OnMatch.INCLUDE;
            }
            if (isCaseSensitive() && ext.equals(refExtension)) {
                return safeOnMatch == OnMatch.INCLUDE;
            }
        }
        return safeOnMatch == OnMatch.EXCLUDE;
    }

    public Set<String> getExtensions() {
        return Collections.unmodifiableSet(extensions);
    }
    public void setExtensions(String... extensions) {
        CollectionUtil.setAll(this.extensions, extensions);
    }
    public void setExtensions(List<String> extensions) {
        CollectionUtil.setAll(this.extensions, extensions);
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public final void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public void loadFromXML(XML xml)  {
        setExtensions(xml.getDelimitedStringList("."));
        setOnMatch(xml.getEnum("@onMatch", OnMatch.class, onMatch));
        setCaseSensitive(xml.getBoolean("@caseSensitive", caseSensitive));
    }
    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("onMatch", onMatch);
        xml.setAttribute("caseSensitive", caseSensitive);
        xml.setTextContent(StringUtils.join(extensions, ','));
    }

    @Override
    public boolean acceptDocument(Doc document) {
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
