/* Copyright 2014-2016 Norconex Inc.
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
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.jef4.job.IJobErrorListener;
import com.norconex.jef4.job.IJobLifeCycleListener;
import com.norconex.jef4.suite.ISuiteLifeCycleListener;

/**
 * @author Pascal Essiembre
 *
 */
public interface ICollectorConfig extends IXMLConfigurable {

    /**
     * Gets this collector unique identifier.
     * @return unique identifier
     */
    String getId();

    /**
     * Gets the directory location where progress files (from JEF API)
     * are stored.
     * @return progress directory path
     */
    String getProgressDir();

    /**
     * Gets the directory location of generated log files.
     * @return logs directory path
     */
    String getLogsDir();

    /**
     * Gets collector life cycle listeners. 
     * @return collector life cycle listeners. 
     * @since 1.8.0
     */    
    ICollectorLifeCycleListener[] getCollectorListeners();
    
    /**
     * Gets JEF job life cycle listeners. A job typically represents a 
     * crawler instance. Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * This method can safely return <code>null</code>.  
     * @return JEF job life cycle listeners. 
     * @since 1.7.0
     */    
    IJobLifeCycleListener[] getJobLifeCycleListeners();
    /**
     * Gets JEF error listeners. Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * This method can safely return <code>null</code>.  
     * @return JEF job error listeners
     * @since 1.7.0
     */    
    IJobErrorListener[] getJobErrorListeners();
    /**
     * Gets JEF job suite life cycle listeners. 
     * A job suite typically represents a collector instance. 
     * Interacting directly
     * with the <a href="https://www.norconex.com/jef/api/">JEF API</a>
     * is normally reserved for more advanced use. 
     * This method can safely return <code>null</code>.  
     * @return JEF suite life cycle listeners 
     * @since 1.7.0
     */    
    ISuiteLifeCycleListener[] getSuiteLifeCycleListeners();
    
    /**
     * Gets all crawler configurations.
     * @return crawler configurations
     * @since 1.7.0
     */
    ICrawlerConfig[] getCrawlerConfigs();

}