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
package com.norconex.collector.core.store.impl.mvstore;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
* <p>
* MVStore configuration parameters.  For advanced use only.
* </p>
* @since 1.10.0
* @author Pascal Essiembre
*/
public class MVStoreDataStoreConfig {

   private Integer pageSplitSize;
   private Integer compress;
   private Integer cacheConcurrency;
   private Integer cacheSize;
   private Integer autoCompactFillRate;
   private Integer autoCommitBufferSize;
   private Integer autoCommitDelay;

   public Integer getPageSplitSize() {
       return pageSplitSize;
   }
   public void setPageSplitSize(Integer pageSplitSize) {
       this.pageSplitSize = pageSplitSize;
   }

   public Integer getCompress() {
       return compress;
   }
   public void setCompress(Integer compress) {
       this.compress = compress;
   }

   public Integer getCacheConcurrency() {
       return cacheConcurrency;
   }
   public void setCacheConcurrency(Integer cacheConcurrency) {
       this.cacheConcurrency = cacheConcurrency;
   }

   public Integer getCacheSize() {
       return cacheSize;
   }
   public void setCacheSize(Integer cacheSize) {
       this.cacheSize = cacheSize;
   }

   public Integer getAutoCompactFillRate() {
       return autoCompactFillRate;
   }
   public void setAutoCompactFillRate(Integer autoCompactFillRate) {
       this.autoCompactFillRate = autoCompactFillRate;
   }

   public Integer getAutoCommitBufferSize() {
       return autoCommitBufferSize;
   }
   public void setAutoCommitBufferSize(Integer autoCommitBufferSize) {
       this.autoCommitBufferSize = autoCommitBufferSize;
   }

   public Integer getAutoCommitDelay() {
       return autoCommitDelay;
   }
   public void setAutoCommitDelay(Integer autoCommitDelay) {
       this.autoCommitDelay = autoCommitDelay;
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