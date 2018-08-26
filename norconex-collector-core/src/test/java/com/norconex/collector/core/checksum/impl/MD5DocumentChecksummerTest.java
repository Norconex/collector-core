/* Copyright 2015-2018 Norconex Inc.
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
package com.norconex.collector.core.checksum.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.ImporterDocument;

/**
 * @author Pascal Essiembre
 * @since 1.2.0
 */
public class MD5DocumentChecksummerTest {

    @Test
    public void testCreateDocumentChecksumFromContent() throws IOException {
        // Simply should not fail and return something.
        String content = "Some content";
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream(content);
        ImporterDocument doc = new ImporterDocument("N/A", is);
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();
        String checksum = cs.createDocumentChecksum(doc);
        is.dispose();
        Assert.assertTrue("No checksum was generated.",
                StringUtils.isNotBlank(checksum));
    }

    @Test
    public void testCreateDocumentChecksumFromMeta() throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream();
        ImporterDocument doc = new ImporterDocument("N/A", is);
        doc.getMetadata().add("field1", "value1.1", "value1.2");
        doc.getMetadata().add("field2", "value2");
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();

        // 2 matching fields
        cs.setSourceFields("field1", "field2");
        String checksum1 = cs.createDocumentChecksum(doc);
        Assert.assertTrue("No checksum was generated for two matching fields.",
                StringUtils.isNotBlank(checksum1));

        // 1 out of 2 matching fields
        cs.setSourceFields("field1", "field3");
        String checksum2 = cs.createDocumentChecksum(doc);
        Assert.assertTrue(
                "No checksum was generated for 1 of two matching fields.",
                StringUtils.isNotBlank(checksum2));

        // No matching fields
        cs.setSourceFields("field4", "field5");
        String checksum3 = cs.createDocumentChecksum(doc);
        Assert.assertNull(
                "Checksum for no matching fields should have been null.",
                checksum3);

        // Fields + Regex
        cs.setSourceFieldsRegex("field.*");
        String checksum4 = cs.createDocumentChecksum(doc);
        Assert.assertTrue("No checksum was generated.",
                StringUtils.isNotBlank(checksum4));

        // Regex only
        cs.setSourceFields();
        cs.setSourceFieldsRegex("field.*");
        String checksum5 = cs.createDocumentChecksum(doc);
        Assert.assertTrue("No checksum was generated.",
                StringUtils.isNotBlank(checksum5));

        // Regex only no match
        cs.setSourceFieldsRegex("NOfield.*");
        String checksum6 = cs.createDocumentChecksum(doc);
        Assert.assertNull(
                "Checksum for no matching regex should have been null.",
                checksum6);

        is.dispose();
    }

    // https://github.com/Norconex/collector-http/issues/388
    @Test
    public void testCombineFieldsAndContent() throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream("Content");
        ImporterDocument doc = new ImporterDocument("N/A", is);
        doc.getMetadata().add("field1", "value1.1", "value1.2");
        doc.getMetadata().add("field2", "value2");
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();

        // With no source fields, should use content only.
        String contentChecksum = cs.createDocumentChecksum(doc);

        // With source fields, should use fields only.
        cs.setSourceFieldsRegex("field.*");
        String fieldsChecksum = cs.createDocumentChecksum(doc);

        // When combining, should use both fields and content.
        cs.setCombineFieldsAndContent(true);
        String combinedChecksum = cs.createDocumentChecksum(doc);

        // The 3 checksums should be non-null, but different.
        Assert.assertNotNull("Null content checksum.", contentChecksum);
        Assert.assertNotNull("Null fields checksum.", fieldsChecksum);
        Assert.assertNotNull("Null combined checksum.", combinedChecksum);

        Assert.assertNotEquals(contentChecksum, fieldsChecksum);
        Assert.assertNotEquals(fieldsChecksum, combinedChecksum);

        is.dispose();
    }

    @Test
    public void testWriteRead() throws IOException {
        MD5DocumentChecksummer c = new MD5DocumentChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setSourceFields("field1","field2");
        c.setSourceFieldsRegex("field.*");
        c.setCombineFieldsAndContent(true);
        c.setTargetField("target");
        XML.assertWriteRead(c, "documentChecksummer");
    }
}
