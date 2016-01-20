/* Copyright 2014,2016 Norconex Inc.
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
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.filter.IDocumentFilter;
import com.norconex.collector.core.filter.IMetadataFilter;
import com.norconex.collector.core.filter.IReferenceFilter;
import com.norconex.commons.lang.config.ConfigurationUtil;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.map.Properties;
import com.norconex.importer.doc.ImporterDocument;
import com.norconex.importer.handler.filter.AbstractOnMatchFilter;
import com.norconex.importer.handler.filter.OnMatch;

/**
 * Filters a reference based on a comma-separated list of extensions.
 * Extensions are typically the last characters of a file name, after the 
 * last dot.
 * <p>
 * XML configuration usage:
 * </p>
 * <pre>
 *  &lt;filter class="com.norconex.collector.core.filter.impl.ExtensionReferenceFilter"
 *          onMatch="[include|exclude]" 
 *          caseSensitive="[false|true]" &gt;
 *      (comma-separated list of extensions)
 *  &lt;/filter&gt;
 * </pre>
 * @author Pascal Essiembre
 */
@SuppressWarnings("nls")
public class ExtensionReferenceFilter extends AbstractOnMatchFilter implements 
        IReferenceFilter, IDocumentFilter, IMetadataFilter, IXMLConfigurable {

    private boolean caseSensitive;
    private String extensions;
    private String[] extensionParts;

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
    public boolean acceptReference(String reference) {
        if (StringUtils.isBlank(extensions)) {
            return getOnMatch() == OnMatch.INCLUDE;
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
                return getOnMatch() == OnMatch.INCLUDE;
            } else if (isCaseSensitive() && ext.equals(refExtension)) {
                return getOnMatch() == OnMatch.INCLUDE;
            }
        }
        return getOnMatch() == OnMatch.EXCLUDE;
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
            for (int i = 0; i < this.extensionParts.length; i++)
                this.extensionParts[i] = this.extensionParts[i].trim();
        } else {
            this.extensionParts = ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }
    @Override
    public void loadFromXML(Reader in)  {
        XMLConfiguration xml = ConfigurationUtil.newXMLConfiguration(in);
        setExtensions(xml.getString(""));
        loadFromXML(xml);
        setCaseSensitive(xml.getBoolean("[@caseSensitive]", false));
    }
    @Override
    public void saveToXML(Writer out) throws IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            writer.writeStartElement("filter");
            writer.writeAttribute("class", getClass().getCanonicalName());
            saveToXML(writer);
            writer.writeAttribute("caseSensitive", 
                    Boolean.toString(caseSensitive));
            writer.writeCharacters(extensions);
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }
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
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .appendSuper(super.toString())
            .append("extensions", extensions)
            .append("caseSensitive", caseSensitive)
            .toString();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(caseSensitive)
            .append(extensions)
            .toHashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExtensionReferenceFilter)) {
            return false;
        }
        ExtensionReferenceFilter other = (ExtensionReferenceFilter) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(caseSensitive, other.caseSensitive)
            .append(extensions, other.extensions)
            .isEquals();
    }
}
