/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.doccrawl;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

import com.norconex.collector.core.CollectorException;


/**
 * Create a new {@link IDocCrawl} with a default state of NEW.
 * @author Pascal Essiembre
 */
public class BasicDocCrawl implements IDocCrawl {

    private static final long serialVersionUID = 8711781555253202315L;

    private String reference;
    private String parentRootReference;
    private boolean isRootParentReference;
    private DocCrawlState state;
    private String metaChecksum;
    private String contentChecksum;
    
    /**
     * Constructor.
     */
    public BasicDocCrawl() {
        super();
        setState(DocCrawlState.NEW);
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
    public DocCrawlState getState() {
        return state;
    }
    public void setState(DocCrawlState state) {
        this.state = state;
    }
    
    public String getMetaChecksum() {
        return metaChecksum;
    }
    public void setMetaChecksum(String metaChecksum) {
        this.metaChecksum = metaChecksum;
    }

    public String getContentChecksum() {
        return contentChecksum;
    }
    public void setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
    }
    @Override
    public IDocCrawl safeClone() {
        try {
            return (IDocCrawl) BeanUtils.cloneBean(this);
        } catch (IllegalAccessException | InstantiationException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new CollectorException(
                    "Cannot clone HttpDocReference: " + this, e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((contentChecksum == null) ? 0 : contentChecksum.hashCode());
        result = prime * result + (isRootParentReference ? 1231 : 1237);
        result = prime * result
                + ((metaChecksum == null) ? 0 : metaChecksum.hashCode());
        result = prime
                * result
                + ((parentRootReference == null) ? 0 : parentRootReference
                        .hashCode());
        result = prime * result
                + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicDocCrawl)) {
            return false;
        }
        BasicDocCrawl other = (BasicDocCrawl) obj;
        if (contentChecksum == null) {
            if (other.contentChecksum != null) {
                return false;
            }
        } else if (!contentChecksum.equals(other.contentChecksum)) {
            return false;
        }
        if (isRootParentReference != other.isRootParentReference) {
            return false;
        }
        if (metaChecksum == null) {
            if (other.metaChecksum != null) {
                return false;
            }
        } else if (!metaChecksum.equals(other.metaChecksum)) {
            return false;
        }
        if (parentRootReference == null) {
            if (other.parentRootReference != null) {
                return false;
            }
        } else if (!parentRootReference.equals(other.parentRootReference)) {
            return false;
        }
        if (reference == null) {
            if (other.reference != null) {
                return false;
            }
        } else if (!reference.equals(other.reference)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return "BasicDocCrawlDetails [reference=" + reference
                + ", parentRootReference=" + parentRootReference
                + ", isRootParentReference=" + isRootParentReference
                + ", state=" + state + ", metaChecksum=" + metaChecksum
                + ", contentChecksum=" + contentChecksum + "]";
    }
    
    
}
