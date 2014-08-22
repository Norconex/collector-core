/**
 * 
 */
package com.norconex.collector.core.pipeline;

/**
 * @author Pascal Essiembre
 *
 */

//TODO similar to importerstatus, return this class instead of 
// a boolean to figure out if processing should stop, and wy it was stopped.

//TODO Maybe return reference state instead of this class?

//TODO Maybe keep the boolean and leave it to the pipeline context object to hold
//  a reference to what went wrong or other type of statuses.
public class PipelineStatus {

    
    
    /**
     * Constructor.
     */
    public PipelineStatus() {
    }

}
