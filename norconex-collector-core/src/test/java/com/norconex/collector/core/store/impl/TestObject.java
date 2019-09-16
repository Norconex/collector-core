package com.norconex.collector.core.store.impl;

import com.norconex.collector.core.reference.CrawlReference;

// Adds a few extra field types for testing
public class TestObject extends CrawlReference {

    private static final long serialVersionUID = 1L;
    private int count;
    private boolean valid;

    public TestObject() {
        super();
    }
    public TestObject(String reference, int count, String checksum,
            String parentReference) {
        super(reference);
        this.count = count;
        setContentChecksum(checksum);
        setParentRootReference(parentReference);
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
