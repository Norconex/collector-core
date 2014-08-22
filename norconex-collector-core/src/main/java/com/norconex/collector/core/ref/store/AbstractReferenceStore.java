/**
 * 
 */
package com.norconex.collector.core.ref.store;

import com.norconex.collector.core.ref.IReference;
import com.norconex.collector.core.ref.ReferenceState;

/**
 * @author Pascal Essiembre
 */
public abstract class AbstractReferenceStore implements IReferenceStore {

    @Override
    public boolean isVanished(IReference reference) {
        IReference cachedReference = getCached(reference.getReference());
        if (cachedReference == null) {
            return false;
        }
        ReferenceState current = reference.getState();
        ReferenceState last = cachedReference.getState();
        return !current.isValid() && last.isValid();
    }


}
