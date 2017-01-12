/* Copyright 2017 Norconex Inc.
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
package com.norconex.collector.core;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.crawler.event.ICrawlerEventListener;

/**
 * Listens to collector life-cycle events.  This listener only captures
 * the starting and finishing of the collector.  For crawler-specific 
 * events, refer to {@link ICrawlerEventListener}. For more advanced 
 * event monitoring, consider using JEF API listeners. 
 * @author Pascal Essiembre
 * @since 1.8.0
 * @see ICollectorConfig#getSuiteLifeCycleListeners()
 * @see ICollectorConfig#getJobLifeCycleListeners()
 * @see ICollectorConfig#getJobErrorListeners()
 * @see ICrawlerConfig#getCrawlerListeners()
 */
public interface ICollectorLifeCycleListener {

    /**
     * Invoked when the collector has been created and is just about to start.
     * @param collector the collector being started
     */
    void onCollectorStart(ICollector collector);

    /**
     * Invoked when the collector is finishing its execution. While an 
     * invocation of this method is not an indication of execution success,
     * no invocation definitely indicates a problem.
     * @param collector the collector finishing execution
     */
    void onCollectorFinish(ICollector collector); 
}
