package com.norconex.collector.core.filter.impl;

import org.junit.Assert;
import org.junit.Test;

public class ExtensionReferenceFilterTest {

    @Test
    public void testOnlyDetectExtensionsInLastPathSegment() {
        ExtensionReferenceFilter filter = initFilter("com,subtype.xml");

        Assert.assertFalse(
                filter.acceptReference("http://example.com"));

        Assert.assertFalse(
                filter.acceptReference("http://example.com/file"));

        Assert.assertFalse(
                filter.acceptReference("http://example.com/dir.com/file"));

        Assert.assertTrue(
                filter.acceptReference("http://example.com/file.com"));

        Assert.assertTrue(
                filter.acceptReference("http://example.de/file.com"));

        Assert.assertFalse(
                filter.acceptReference("http://example.com/file.xml"));

        Assert.assertTrue(
                filter.acceptReference("http://example.com/file.subtype.xml"));
    }

    private ExtensionReferenceFilter initFilter(String extensions) {
        ExtensionReferenceFilter filter = new ExtensionReferenceFilter();
        filter.setExtensions(extensions);
        return filter;
    }
}
