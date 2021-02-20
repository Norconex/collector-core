package com.norconex.collector.core.tracker;

import java.io.Serializable;

import org.slf4j.event.Level;

public class Tracker<T> implements Serializable {

    //TODO maybe do not have type and leave it to concreate implementations
    // to define?  Would work if initial status are restored from crawl state
    // after collector/crawler/committer init is done, to give a chance
    // to have all trackers already registered.  A serialized tracking
    // value that is not registered will be ignored.

    private static final long serialVersionUID = 1L;
    private String id;
    private T value;
    private Class<T> type; // <-- remove and have to/from conversion method?
    boolean transientFlag;

    //TODO If we have listeners, have those logging entries
    // defined externally by said listener?
    private Level logLevel;  // null means off
    private long logInterval; // minimum elapsed time between each logging of the value

//    public static void fromObject(Tracker tracker, Object obj) {
//        Converter.defaultInstance().tot
//    }

    //TODO Have a builder, make id mandatory and type String by default

    //TODO make observable
    //TODO have method for serializable key and value (both strings)


//    public static class Builder {
//        private final String id;
//        private Object initialValue;
//        private Builder(String id) {
//            super();
//            this.id = id;
//        }
//        public Builder initialValue(Object value) {
//            this.initialValue = value;
//            return this;
//        }
//
//        public Builder build
//    }

}
