/* Copyright 2019-2020 Norconex Inc.
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
package com.norconex.collector.core.doc;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.store.Id;
import com.norconex.collector.core.store.Index;
import com.norconex.importer.doc.DocInfo;

/**
 * @author Pascal Essiembre
 */
@Id("reference")
public class CrawlDocInfo extends DocInfo {

    //TODO create @ignore metadata to prevent storing some fields?

    private static final long serialVersionUID = 1L;

    public enum Stage {
        QUEUED, ACTIVE, PROCESSED /*, CACHED*/;

        public boolean is(Stage stage) {
            return stage != null && stage == this;
        }

    } //TODO add NONE?

    @ToStringExclude
    private String parentRootReference;
    private CrawlState state;
    @ToStringExclude
    private String metaChecksum;
    @ToStringExclude
    private String contentChecksum;
    private ZonedDateTime crawlDate;

    @Index
    private Stage processingStage;

    public CrawlDocInfo() {
        super();
    }

    public CrawlDocInfo(String reference) {
        super(reference);
    }
    /**
     * Copy constructor.
     * @param docDetails document details to copy
     */
    public CrawlDocInfo(DocInfo docDetails) {
        super(docDetails);
    }


//------ parent root reference is not used..........................
    public String getParentRootReference() {
        return parentRootReference;
    }
    public void setParentRootReference(String parentRootReference) {
        this.parentRootReference = parentRootReference;
    }

    public CrawlState getState() {
        return state;
    }
    public void setState(CrawlState state) {
        this.state = state;
    }

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
     * Gets the crawl date.
     * @return the crawl date
     * @since 1.5.0
     */
    public ZonedDateTime getCrawlDate() {
        return crawlDate;
    }
    /**
     * Sets the crawl date.
     * @param crawlDate the crawl date
     */
    public void setCrawlDate(ZonedDateTime crawlDate) {
        this.crawlDate = crawlDate;
    }

    public Stage getProcessingStage() {
        return processingStage;
    }

    public void setProcessingStage(Stage processingStage) {
        this.processingStage = processingStage;
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
        ReflectionToStringBuilder b = new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE);
        b.setExcludeNullValues(true);
        return b.toString();

    }
}
