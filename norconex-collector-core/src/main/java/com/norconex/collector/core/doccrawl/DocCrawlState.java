/**
 * 
 */
package com.norconex.collector.core.doccrawl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Reference processing status.
 * @author Pascal Essiembre
 */
public class DocCrawlState implements Serializable {

    private static final Map<String, DocCrawlState> STATUSES = 
            new HashMap<>();
    
    //TODO delete this after refactoring complete:
//    public static final DocCrawlState OK = new DocCrawlState("OK");

    
    
    private static final long serialVersionUID = 6542269270632505768L;
//    public static final DocCrawlState UNPROCESSED = 
//            new DocCrawlState("UNPROCESSED");
    public static final DocCrawlState NEW = new DocCrawlState("NEW");
    public static final DocCrawlState MODIFIED = 
            new DocCrawlState("MODIFIED");
    public static final DocCrawlState UNMODIFIED = 
            new DocCrawlState("UNMODIFIED");
    public static final DocCrawlState ERROR = new DocCrawlState("ERROR");
    public static final DocCrawlState REJECTED = 
            new DocCrawlState("REJECTED");
    
    private final String state;
    
    /**
     * Constructor.
     * @param state state code
     */
    protected DocCrawlState(String state) {
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
        //return isOneOf(OK, NEW, MODIFIED, UNMODIFIED);
        return isOneOf(NEW, MODIFIED, UNMODIFIED);
    }

    public boolean isCommittable() {
        return isOneOf(NEW, MODIFIED);
    }

    
    public boolean isOneOf(DocCrawlState... states) {
        if (ArrayUtils.isEmpty(states)) {
            return false;
        }
        for (DocCrawlState docCrawlState : states) {
            if (equals(docCrawlState)) {
                return true;
            }
        }
        return false;
    }
    
    public static synchronized DocCrawlState valueOf(String state) {
        DocCrawlState refState = STATUSES.get(state);
        if (refState == null) {
            refState = new DocCrawlState(state);
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
        if (!(obj instanceof DocCrawlState)) {
            return false;
        }
        DocCrawlState other = (DocCrawlState) obj;
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
