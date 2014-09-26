/**
 * 
 */
package com.norconex.collector.core.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Reference processing status.
 * @author Pascal Essiembre
 */
public class CrawlState implements Serializable {

    private static final Map<String, CrawlState> STATUSES = 
            new HashMap<>();
    
    private static final long serialVersionUID = 6542269270632505768L;
    public static final CrawlState NEW = new CrawlState("NEW");
    public static final CrawlState MODIFIED = 
            new CrawlState("MODIFIED");
    public static final CrawlState UNMODIFIED = 
            new CrawlState("UNMODIFIED");
    public static final CrawlState ERROR = new CrawlState("ERROR");
    public static final CrawlState REJECTED = 
            new CrawlState("REJECTED");
    
    private final String state;
    
    /**
     * Constructor.
     * @param state state code
     */
    protected CrawlState(String state) {
        this.state = state;
        STATUSES.put(state, this);
    }

    /**
     * Returns whether a reference should be considered "good" (the
     * corresponding document is not in a "bad" state, such as being rejected
     * or produced an error.  
     * This implementation will consider valid these reference statuses:
     * {@link #NEW}, {@link #MODIFIED}, {@link #UNMODIFIED}.
     * This method can be overridden to provide different logic for a valid
     * reference.
     * @return <code>true</code> if status is valid.
     */
    public boolean isGoodState() {
        return isOneOf(NEW, MODIFIED, UNMODIFIED);
    }

    public boolean isCommittable() {
        return isOneOf(NEW, MODIFIED);
    }

    
    public boolean isOneOf(CrawlState... states) {
        if (ArrayUtils.isEmpty(states)) {
            return false;
        }
        for (CrawlState crawlState : states) {
            if (equals(crawlState)) {
                return true;
            }
        }
        return false;
    }
    
    public static synchronized CrawlState valueOf(String state) {
        CrawlState refState = STATUSES.get(state);
        if (refState == null) {
            refState = new CrawlState(state);
        }
        return refState;
    }
    
    /**
     * Returns the status code.
     * @return status code
     */
    @Override
    public String toString() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (!(obj instanceof CrawlState)) {
            return false;
        }
        CrawlState other = (CrawlState) obj;
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        return true;
    }
}
