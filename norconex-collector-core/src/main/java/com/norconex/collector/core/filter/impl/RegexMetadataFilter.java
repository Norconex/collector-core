/* Copyright 2014 Norconex Inc.
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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.handler.filter.AbstractOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;
/**
 * Accepts or rejects a reference using regular expression to match 
 * a metadata field value.
 * <p>
 * XML configuration usage:
 * </p>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.RegexMetadataFilter"
 *          onMatch="[include|exclude]" 
 *          caseSensitive="[false|true]" &gt;
 *          field="(metadata field to holding the value to match)"
 *      (regular expression of value to match)
 *  &lt;/filter&gt;
 * </pre>
 * @author Pascal Essiembre
 */
public class RegexMetadataFilter extends AbstractOnMatchFilter
        implements IMetadataFilter, IDocumentFilter, IXMLConfigurable {

    //TODO use Importer RegexMetadataFilter here?  Catching import exception

    private boolean caseSensitive;
    private String field;
    private String regex;
    private Pattern pattern;

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
    }
    public final void setField(String header) {
        this.field = header;
    }
    public final void setRegex(String regex) {
        this.regex = regex;
        if (regex != null) {
            int flags = Pattern.DOTALL;
            if (!caseSensitive) {
                flags = flags | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
            }
            this.pattern = Pattern.compile(regex, flags);            
        } else {
            this.pattern = Pattern.compile(".*");
        }
    }

    @Override
    public boolean acceptMetadata(String reference, Properties metadata) {
        if (StringUtils.isBlank(regex)) {
            return getOnMatch() == OnMatch.INCLUDE;
        }
        Collection<String> values = metadata.getStrings(field);
        for (Object value : values) {
            String strVal = Objects.toString(value, StringUtils.EMPTY);
            if (pattern.matcher(strVal).matches()) {
                return getOnMatch() == OnMatch.INCLUDE;
            }
        }
        return getOnMatch() == OnMatch.EXCLUDE;
    }

    @Override
    public boolean acceptDocument(ImporterDocument document) {
        if (document == null) {
            return getOnMatch() == OnMatch.INCLUDE;
        }
        return acceptMetadata(document.getReference(), document.getMetadata());
    }
    
    @Override
    public void loadFromXML(Reader in) {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        setField(xml.getString("[@field]"));
        setRegex(xml.getString(""));
        super.loadFromXML(xml);
        setCaseSensitive(xml.getBoolean("[@caseSensitive]", false));
    }
    @Override
    public void saveToXML(Writer out) throws IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartElement("filter");
            writer.writeAttribute("class", getClass().getCanonicalName());
            super.saveToXML(writer);
            writer.writeAttribute("caseSensitive", 
                    Boolean.toString(caseSensitive));
            writer.writeCharacters(regex == null ? "" : regex);
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
    }
    

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
            .appendSuper(super.toString())
            .append("caseSensitive", caseSensitive)
            .append("field", field)
            .append("regex", regex)
            .toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RegexMetadataFilter)) {
            return false;
        }
        RegexMetadataFilter other = (RegexMetadataFilter) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(caseSensitive, other.caseSensitive)
            .append(field, other.field)
            .append(regex, other.regex)
            .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(caseSensitive)
            .append(field)
            .append(regex)
            .toHashCode();
    }
}

