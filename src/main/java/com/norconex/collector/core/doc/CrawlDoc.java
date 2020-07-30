/* Copyright 2020 Norconex Inc.
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.importer.doc.Doc;
import com.norconex.importer.doc.DocInfo;

/**
 * A crawl document, which holds an additional {@link DocInfo} from cache
 * (if any).
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class CrawlDoc extends Doc {

    private final CrawlDocInfo cachedDocInfo;
    private final boolean orphan;

    public CrawlDoc(DocInfo docInfo, CachedInputStream content) {
        this(docInfo, null, content, false);
    }
    public CrawlDoc(
            DocInfo docInfo,
            CrawlDocInfo cachedDocInfo,
            CachedInputStream content) {
        this(docInfo, cachedDocInfo, content, false);
    }
    public CrawlDoc(
            DocInfo docInfo,
            CrawlDocInfo cachedDocInfo,
            CachedInputStream content, boolean orphan) {
        super(docInfo, content, null);
        this.cachedDocInfo = cachedDocInfo;
        this.orphan = orphan;
    }

    @Override
    public CrawlDocInfo getDocInfo() {
        return (CrawlDocInfo) super.getDocInfo();
    }

    public boolean isOrphan() {
        return orphan;
    }

    public CrawlDocInfo getCachedDocInfo() {
        return cachedDocInfo;
    }

    public boolean hasCache() {
        return cachedDocInfo != null;
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
