/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core;

/**
 * Runtime exception for most unrecoverable issues thrown by Collector
 * classes.
 * @author Pascal Essiembre
 */
public class CollectorException extends RuntimeException {

    private static final long serialVersionUID = -805913995358009121L;

    public CollectorException() {
        super();
    }

    public CollectorException(String message) {
        super(message);
    }

    public CollectorException(Throwable cause) {
        super(cause);
    }

    public CollectorException(String message, Throwable cause) {
        super(message, cause);
    }

}
