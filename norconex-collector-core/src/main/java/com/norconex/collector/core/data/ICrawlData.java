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
package com.norconex.collector.core.data;

import java.io.Serializable;

/**
 * A pointer that uniquely identifies a resource being processed (e.g. a 
 * URL, a file path, etc).  Implementors are strongly encourage to subclass
 * {@link BaseCrawlData} since some features of Norocnex Collector Core 
 * references it.
 * @author Pascal Essiembre
 * @see BaseCrawlData
 */
public interface ICrawlData extends Cloneable, Serializable {

    /**
     * Gets the unique identifier of this reference (e.g. URL, path, etc).
     * @return reference unique identifier
     */
    String getReference();
    
    /**
     * Gets this reference state.
     * @return state
     */
    CrawlState getState();
    
    /**
     * Clones this reference.
     * @return a copy of this instance
     */
    ICrawlData clone();
    
    
    boolean isRootParentReference();
    
    String getParentRootReference();
    
    
    String getMetaChecksum();

    String getContentChecksum();

}
