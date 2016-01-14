/* Copyright 2016 Norconex Inc.
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
        
        Assert.assertTrue(
                filter.acceptReference("http://example.com/dir.com/file.com"));

        Assert.assertFalse(
                filter.acceptReference("http://example.com/dir.com/file.pdf"));

        Assert.assertTrue(
                filter.acceptReference("http://example.com/dir.pdf/file.com"));

        Assert.assertTrue(filter.acceptReference(
                "http://example.com/dir.pdf/file.com?param1=something.pdf"));

        Assert.assertFalse(filter.acceptReference(
                "http://example.com/dir.pdf/file.pdf?param1=something.com"));

        Assert.assertTrue(filter.acceptReference("C:\\example\\file.com"));

        Assert.assertFalse(
                filter.acceptReference("C:\\example\\dir.com\\file.pdf"));

        Assert.assertTrue(filter.acceptReference("/tmp/file.com"));

        Assert.assertFalse(filter.acceptReference("/tmp/dir.com/file.pdf"));

        Assert.assertTrue(filter.acceptReference("file.com"));

        Assert.assertFalse(filter.acceptReference("dir.com/file.pdf"));
    }

    private ExtensionReferenceFilter initFilter(String extensions) {
        ExtensionReferenceFilter filter = new ExtensionReferenceFilter();
        filter.setExtensions(extensions);
        return filter;
    }
}
