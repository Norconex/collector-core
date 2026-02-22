/* Copyright 2021 Norconex Inc.
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
package com.norconex.collector.core.stop;

import com.norconex.collector.core.Collector;

/**
 * <p>
 * Responsible for shutting down a Collector upon explicit invocation
 * of {@link #fireStopRequest(Collector)} or when specific conditions are met.
 * See concrete implementation for what those conditions could be.
 * </p>
 * <p>
 * A stop request can typically be triggered from another JVM (see concrete
 * implementation details).
 * </p>
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public interface ICollectorStopper {

    /**
     * Setup and/or start the stopper, which can be terminated
     * by invoking stop in the same or different JVM (see concrete
     * implementation for details).
     * @param collector the Collector
     * @throws CollectorStopperException could not setup Collector stopper.
     */
    void listenForStopRequest(Collector collector)
            throws CollectorStopperException;
    /**
     * Destroys resources allocated with this stopper.
     * Called at the end of a Collector execution upon completion.
     * @throws CollectorStopperException could not destroy Collector stopper.
     */
    void destroy() throws CollectorStopperException;

    /**
     * Stops a currently running Collector. The supplied collector
     * may or may not be the same instance has the one to be stopped.
     * Implementors should account for an <b><i>non-initialized</i></b>
     * instance (where configuration for crawlers is set, but no crawlers
     * were yet created with those configurations).
     * @param collector collector instance
     * @return <code>true</code> if the Collector was running and successfully
     *         stopped or <code>false</code> if the Collector was not running.
     * @throws CollectorStopperException could not stop running Collector.
     */
    boolean fireStopRequest(Collector collector)
            throws CollectorStopperException;
}
