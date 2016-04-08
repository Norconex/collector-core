/* Copyright 2014-2016 Norconex Inc.
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
package com.norconex.collector.core.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.CollectorException;
import com.norconex.commons.lang.file.ContentType;


/**
 * A based implementation of {@link ICrawlData} with a default state of NEW.
 * @author Pascal Essiembre
 */
public class BaseCrawlData implements ICrawlData {

    private static final long serialVersionUID = 8711781555253202315L;

    private String reference;
    private String parentRootReference;
    private boolean isRootParentReference;
    private CrawlState state;
    private String metaChecksum;
    private String contentChecksum;
    private ContentType contentType;
    private Date crawlDate;
    
    /**
     * Constructor.
     */
    public BaseCrawlData() {
        super();
        setState(CrawlState.NEW);
    }
    
    public BaseCrawlData(String reference) {
        this();
        this.reference = reference;
    }
    
    @Override
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String getParentRootReference() {
        return parentRootReference;
    }
    public void setParentRootReference(String parentRootReference) {
        this.parentRootReference = parentRootReference;
    }

    @Override
    public boolean isRootParentReference() {
        return isRootParentReference;
    }
    public void setRootParentReference(boolean isRootParentReference) {
        this.isRootParentReference = isRootParentReference;
    }

    @Override
    public CrawlState getState() {
        return state;
    }
    public void setState(CrawlState state) {
        this.state = state;
    }
    
    @Override
    public String getMetaChecksum() {
        return metaChecksum;
    }
    public void setMetaChecksum(String metaChecksum) {
        this.metaChecksum = metaChecksum;
    }

    /**
     * Gets the content checksum.
     * @return the content checksum
     */
    @Override
    public String getContentChecksum() {
        return contentChecksum;
    }
    /**
     * Sets the content checksum.
     * @param contentChecksum content checksum
     */
    public void setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
    }
    /**
     * Sets the content checksum.
     * @param contentChecksum content checksum
     * @deprecated use {@link #setContentChecksum(String)}
     */
    @Deprecated
    public void setDocumentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
    }
    
    /**
     * Gets the content type.
     * @return content type
     * @since 1.5.0
     */
    @Override
    public ContentType getContentType() {
        return contentType;
    }
    /**
     * Sets the content type.
     * @param contentType the content type
     * @since 1.5.0
     */
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Gets the crawl date.
     * @return the crawl date
     * @since 1.5.0
     */
    @Override
    public Date getCrawlDate() {
        return crawlDate;
    }
    /**
     * Sets the crawl date.
     * @param crawlDate the crawl date
     */
    public void setCrawlDate(Date crawlDate) {
        this.crawlDate = crawlDate;
    }

    @Override
    public ICrawlData clone() {
        try {
            return (ICrawlData) BeanUtils.cloneBean(this);
        } catch (IllegalAccessException | InstantiationException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new CollectorException(
                    "Cannot clone HttpDocReference: " + this, e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("reference", reference)
                .append("parentRootReference", parentRootReference)
                .append("isRootParentReference", isRootParentReference)
                .append("state", state)
                .append("metaChecksum", metaChecksum)
                .append("contentChecksum", contentChecksum)
                .append("contentType", contentType)
                .append("crawlDate", crawlDate)
                .toString();
    }
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BaseCrawlData)) {
            return false;
        }
        BaseCrawlData castOther = (BaseCrawlData) other;
        return new EqualsBuilder().append(reference, castOther.reference)
                .append(parentRootReference, castOther.parentRootReference)
                .append(isRootParentReference, castOther.isRootParentReference)
                .append(state, castOther.state)
                .append(metaChecksum, castOther.metaChecksum)
                .append(contentChecksum, castOther.contentChecksum)
                .append(contentType, castOther.contentType)
                .append(crawlDate, castOther.crawlDate)
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(reference)
                .append(parentRootReference)
                .append(isRootParentReference)
                .append(state)
                .append(metaChecksum)
                .append(contentChecksum)
                .append(contentType)
                .append(crawlDate)
                .toHashCode();
    }
}
