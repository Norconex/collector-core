/**
 * 
 */
package com.norconex.collector.core.ref;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Reference processing status.
 * @author Pascal Essiembre
 */
public class ReferenceState implements Serializable {

    //TODO delete this after refactoring complete:
    public static final ReferenceState OK = new ReferenceState("OK");

    
    
    private static final long serialVersionUID = 6542269270632505768L;
    public static final ReferenceState UNPROCESSED = 
            new ReferenceState("UNPROCESSED");
    public static final ReferenceState NEW = new ReferenceState("NEW");
    public static final ReferenceState MODIFIED = 
            new ReferenceState("MODIFIED");
    public static final ReferenceState UNMODIFIED = 
            new ReferenceState("UNMODIFIED");
    public static final ReferenceState ERROR = new ReferenceState("ERROR");
    public static final ReferenceState REJECTED = 
            new ReferenceState("REJECTED");
    
    private static final Map<String, ReferenceState> STATUSES = 
            new HashMap<>();
    
    private final String state;
    
    /**
     * Constructor.
     */
    protected ReferenceState(String state) {
        this.state = state;
        STATUSES.put(state, this);
    }

    /**
     * Returns whether a reference should be considered valid (the
     * corresponding document is not in a "bad" state, such as being rejected
     * or produced an error.  
     * This implementation will consider valid these reference statuses:
     * {@link #NEW}, {@link #MODIFIED}, {@link #UNMODIFIED}.
     * This method can be overridden to provide different logic for a valid
     * reference.
     * @return <code>true</code> if status is valid.
     */
    public boolean isValid() {
        return isOneOf(NEW, MODIFIED, UNMODIFIED);
    }
    
    public boolean isOneOf(ReferenceState... states) {
        if (ArrayUtils.isEmpty(states)) {
            return false;
        }
        for (ReferenceState referenceState : states) {
            if (equals(referenceState)) {
                return true;
            }
        }
        return false;
    }
    
    public static synchronized ReferenceState valueOf(String state) {
        ReferenceState refState = STATUSES.get(state);
        if (refState == null) {
            refState = new ReferenceState(state);
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
        if (!(obj instanceof ReferenceState)) {
            return false;
        }
        ReferenceState other = (ReferenceState) obj;
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
