/**
 * 
 */
package com.norconex.collector.core.pipeline;

import com.norconex.collector.core.CollectorException;

/**
 * @author Pascal Essiembre
 * @param <T> pipeline context type
 *
 */
//TODO move to Norconex Commons Lang?
public interface IPipelineStage<T> {

    //TODO throw PipelineException
    boolean process(T context) throws CollectorException;
}
