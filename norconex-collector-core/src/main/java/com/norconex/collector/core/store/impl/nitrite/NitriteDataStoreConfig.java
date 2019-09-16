/* Copyright 2019 Norconex Inc.
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
package com.norconex.collector.core.store.impl.nitrite;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * Nitrite configuration parameters.  For advanced use only.
 * </p>
 * @since 2.0.0
 * @author Pascal Essiembre
 */
public class NitriteDataStoreConfig {

    private Integer autoCommitBufferSize;
    private Boolean compress;
    private Boolean disableAutoCommit;
    private Boolean disableAutoCompact;
    private Boolean disableShutdownHook;

    public Boolean getCompress() {
        return compress;
    }
    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public Integer getAutoCommitBufferSize() {
        return autoCommitBufferSize;
    }
    public void setAutoCommitBufferSize(Integer autoCommitBufferSize) {
        this.autoCommitBufferSize = autoCommitBufferSize;
    }

    public Boolean getDisableAutoCommit() {
        return disableAutoCommit;
    }
    public void setDisableAutoCommit(Boolean disableAutoCommit) {
        this.disableAutoCommit = disableAutoCommit;
    }

    public Boolean getDisableAutoCompact() {
        return disableAutoCompact;
    }
    public void setDisableAutoCompact(Boolean disableAutoCompact) {
        this.disableAutoCompact = disableAutoCompact;
    }

    public Boolean getDisableShutdownHook() {
        return disableShutdownHook;
    }
    public void setDisableShutdownHook(Boolean disableShutdownHook) {
        this.disableShutdownHook = disableShutdownHook;
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
