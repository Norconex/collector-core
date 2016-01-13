package com.norconex.collector.core.filter.impl;

import org.junit.Assert;
import org.junit.Test;

public class ExtensionReferenceFilterTest {

    @Test
    public void testExtensionSplitting() {
        ExtensionReferenceFilter filter = initFilter(
                "html,htm,\n"
              + "  xhtml , dhtml \n\n");

        Assert.assertArrayEquals(
                new String[] {
                    "html",
                    "htm",
                    "xhtml",
                    "dhtml"
                },
                filter.getExtensionParts());
    }

    private ExtensionReferenceFilter initFilter(String extensions) {
        ExtensionReferenceFilter filter = new ExtensionReferenceFilter();
        filter.setExtensions(extensions);
        return filter;
    }
}
