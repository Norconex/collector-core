/**
 * 
 */
package com.norconex.collector.core;

import com.norconex.commons.lang.config.IXMLConfigurable;

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

}