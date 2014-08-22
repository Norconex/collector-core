/**
 * 
 */
package com.norconex.collector.core.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.norconex.collector.core.CollectorException;

/**
 * @author Pascal Essiembre
 * @param <T> pipeline context type
 */
//TODO move to Norconex Commons Lang?
//TODO make it implement List?
public class Pipeline<T> implements IPipelineStage<T> {

    private final List<IPipelineStage<T>> stages = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public Pipeline() {
    }
    public Pipeline(List<IPipelineStage<T>> stages) {
        this.stages.addAll(stages);
    }

    /**
     * Gets the pipeline stages.
     * @return the pipeline stages
     */
    public List<IPipelineStage<T>> getStages() {
        return Collections.unmodifiableList(stages);
    }

    /**
     * Adds stages to the pipeline.
     * @param stages pipeline stages to add
     * @return this instance, for chaining
     */
    public Pipeline<T> addStages(List<IPipelineStage<T>> stages) {
        this.stages.addAll(stages);
        return this;
    }
    /**
     * Adds a stage to the pipeline.
     * @param stage pipeline stage to add
     * @return this instance, for chaining
     */
    public Pipeline<T> addStage(IPipelineStage<T> stage) {
        this.stages.add(stage);
        return this;
    }

    public void clearStages() {
        stages.clear();
    }
    
    @Override
    public boolean process(T context) throws CollectorException {
        for (IPipelineStage<T> stage : stages) {
            if (!stage.process(context)) {
                return false;
            }
        }
        return true;
    }

}
