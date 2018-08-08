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

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.handler.filter.IOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;
/**
 * <p>
 * Accepts or rejects a reference using regular expression to match
 * a metadata field value.
 * </p>
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.RegexMetadataFilter"
 *          onMatch="[include|exclude]"
 *          caseSensitive="[false|true]"
 *          field="(metadata field to holding the value to match)"&gt;
 *      (regular expression of value to match)
 *  &lt;/filter&gt;
 * </pre>
 *
 * <h4>Usage example:</h4>
 * <p>
 * Used in a web context, the following example filters out Zip documents base
 * on HTTP metadata "Content-Type".
 * </p>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.RegexMetadataFilter"
 *          onMatch="exclude" field="Content-Type"&gt;
 *      application/zip
 *  &lt;/filter&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 * @see Pattern
 */
public class RegexMetadataFilter implements IOnMatchFilter, IMetadataFilter,
        IDocumentFilter, IXMLConfigurable {

    //TODO use Importer RegexMetadataFilter here?  Catching import exception

    private boolean caseSensitive;
    private String field;
    private String regex;
    private Pattern cachedPattern;
    private OnMatch onMatch;

    public RegexMetadataFilter() {
        this(null, null, OnMatch.INCLUDE);
    }
    public RegexMetadataFilter(String header, String regex) {
        this(header, regex, OnMatch.INCLUDE);
    }
    public RegexMetadataFilter(String header, String regex, OnMatch onMatch) {
        this(header, regex, onMatch, false);
    }
    public RegexMetadataFilter(
            String header, String regex,
            OnMatch onMatch, boolean caseSensitive) {
        super();
        setCaseSensitive(caseSensitive);
        setField(header);
        setOnMatch(onMatch);
        setRegex(regex);
    }

    @Override
    public OnMatch getOnMatch() {
        return onMatch;
    }
    public void setOnMatch(OnMatch onMatch) {
        this.onMatch = onMatch;
    }

    public String getRegex() {
        return regex;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public String getField() {
        return field;
    }
    public final void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        cachedPattern = null;
    }
    public final void setField(String header) {
        this.field = header;
    }
    public final void setRegex(String regex) {
        this.regex = regex;
        cachedPattern = null;
    }

    @Override
    public boolean acceptMetadata(String reference, Properties metadata) {
        if (StringUtils.isBlank(regex)) {
            return getOnMatch() == OnMatch.INCLUDE;
        }
        Collection<String> values = metadata.getStrings(field);
        for (Object value : values) {
            String strVal = Objects.toString(value, StringUtils.EMPTY);
            if (getCachedPattern().matcher(strVal).matches()) {
                return getOnMatch() == OnMatch.INCLUDE;
            }
        }
        return getOnMatch() == OnMatch.EXCLUDE;
    }

    private synchronized Pattern getCachedPattern() {
        if (cachedPattern != null) {
            return cachedPattern;
        }
        Pattern p;
        if (regex == null) {
            p = Pattern.compile(".*");
        } else {
            int flags = Pattern.DOTALL;
            if (!caseSensitive) {
                flags = flags | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            }
            p = Pattern.compile(regex, flags);
        }
        cachedPattern = p;
        return p;
    }

    @Override
    public boolean acceptDocument(ImporterDocument document) {
        if (document == null) {
            return getOnMatch() == OnMatch.INCLUDE;
        }
        return acceptMetadata(document.getReference(), document.getMetadata());
    }

    @Override
    public void loadFromXML(XML xml) {
        setField(xml.getString("@field"));
        setOnMatch(xml.getEnum(OnMatch.class, "@onMatch", onMatch));
        setCaseSensitive(xml.getBoolean("@caseSensitive", caseSensitive));
        setRegex(xml.getString("."));
    }
    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("field", field);
        xml.setAttribute("onMatch", onMatch);
        xml.setAttribute("caseSensitive", caseSensitive);
        xml.setTextContent(regex);
        cachedPattern = null;
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other, "cachedPattern");
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "cachedPattern");
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE)
                .setExcludeFieldNames("cachedPattern")
                .toString();
    }
}

