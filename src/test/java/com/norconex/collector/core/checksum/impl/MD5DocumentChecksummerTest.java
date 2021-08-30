/* Copyright 2015-2020 Norconex Inc.
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.map.PropertySetter;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.Doc;

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
        Doc doc = new Doc("N/A", is);
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();
        String checksum = cs.createDocumentChecksum(doc);
        is.dispose();
        Assertions.assertTrue(StringUtils.isNotBlank(checksum),
                "No checksum was generated.");
    }

    @Test
    public void testCreateDocumentChecksumFromMeta() throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream();
        Doc doc = new Doc("N/A", is);
        doc.getMetadata().add("field1", "value1.1", "value1.2");
        doc.getMetadata().add("field2", "value2");
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();

        // 2 matching fields
        cs.setFieldMatcher(TextMatcher.csv("field1,field2"));
        String checksum1 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(
                StringUtils.isNotBlank(checksum1),
                "No checksum was generated for two matching fields.");

        // 1 out of 2 matching fields
        cs.setFieldMatcher(TextMatcher.csv("field1,field3"));
        String checksum2 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(
                StringUtils.isNotBlank(checksum2),

                "No checksum was generated for 1 of two matching fields.");

        // No matching fields
        cs.setFieldMatcher(TextMatcher.csv("field4,field5"));
        String checksum3 = cs.createDocumentChecksum(doc);
        Assertions.assertNull(checksum3,
                "Checksum for no matching fields should have been null.");

        // Regex
        cs.setFieldMatcher(TextMatcher.regex("field.*"));
        String checksum4 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(StringUtils.isNotBlank(checksum4),
                "No checksum was generated.");


        // Regex only no match
        cs.setFieldMatcher(TextMatcher.regex("NOfield.*"));
        String checksum5 = cs.createDocumentChecksum(doc);
        Assertions.assertNull(checksum5,
                "Checksum for no matching regex should have been null.");

        is.dispose();
    }

    // https://github.com/Norconex/collector-http/issues/388
    @Test
    public void testCombineFieldsAndContent() throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream("Content");
        Doc doc = new Doc("N/A", is);
        doc.getMetadata().add("field1", "value1.1", "value1.2");
        doc.getMetadata().add("field2", "value2");
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();

        // With no source fields, should use content only.
        String contentChecksum = cs.createDocumentChecksum(doc);

        // With source fields, should use fields only.
        cs.setFieldMatcher(TextMatcher.regex("field.*"));
        String fieldsChecksum = cs.createDocumentChecksum(doc);

        // When combining, should use both fields and content.
        cs.setCombineFieldsAndContent(true);
        String combinedChecksum = cs.createDocumentChecksum(doc);

        // The 3 checksums should be non-null, but different.
        Assertions.assertNotNull( contentChecksum,
                "Null content checksum.");
        Assertions.assertNotNull( fieldsChecksum,
                "Null fields checksum.");
        Assertions.assertNotNull( combinedChecksum,
                "Null combined checksum.");

        Assertions.assertNotEquals(contentChecksum, fieldsChecksum);
        Assertions.assertNotEquals(fieldsChecksum, combinedChecksum);

        is.dispose();
    }

    @Test
    @Deprecated
    public void testWriteRead() {
        MD5DocumentChecksummer c = new MD5DocumentChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setToField("myToField");
        c.setFieldMatcher(TextMatcher.csv("field1,field2"));
        c.setOnSet(PropertySetter.PREPEND);
        c.setCombineFieldsAndContent(true);
        XML.assertWriteRead(c, "documentChecksummer");
    }

    @Test
    @Deprecated
    public void testCreateDocumentChecksumFromMetaDeprecated()
            throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream();
        Doc doc = new Doc("N/A", is);
        doc.getMetadata().add("field1", "value1.1", "value1.2");
        doc.getMetadata().add("field2", "value2");
        MD5DocumentChecksummer cs = new MD5DocumentChecksummer();

        // 2 matching fields
        cs.setSourceFields("field1", "field2");
        String checksum1 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(
                StringUtils.isNotBlank(checksum1),
                "No checksum was generated for two matching fields.");

        // 1 out of 2 matching fields
        cs.setSourceFields("field1", "field3");
        String checksum2 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(
                StringUtils.isNotBlank(checksum2),

                "No checksum was generated for 1 of two matching fields.");

        // No matching fields
        cs.setSourceFields("field4", "field5");
        String checksum3 = cs.createDocumentChecksum(doc);
        Assertions.assertNull(checksum3,
                "Checksum for no matching fields should have been null.");

        // Fields + Regex
        cs.setSourceFieldsRegex("field.*");
        String checksum4 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(StringUtils.isNotBlank(checksum4),
                "No checksum was generated.");

        // Regex only
        cs.setSourceFields();
        cs.setSourceFieldsRegex("field.*");
        String checksum5 = cs.createDocumentChecksum(doc);
        Assertions.assertTrue(StringUtils.isNotBlank(checksum5),
                "No checksum was generated.");

        // Regex only no match
        cs.setSourceFieldsRegex("NOfield.*");
        String checksum6 = cs.createDocumentChecksum(doc);
        Assertions.assertNull(checksum6,
                "Checksum for no matching regex should have been null.");

        is.dispose();
    }

    // https://github.com/Norconex/collector-http/issues/388
    @Test
    @Deprecated
    public void testCombineFieldsAndContentDeprecated() throws IOException {
        // Simply should not fail and return something.
        CachedInputStream is =
                new CachedStreamFactory(1024, 1024).newInputStream("Content");
        Doc doc = new Doc("N/A", is);
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
        Assertions.assertNotNull( contentChecksum,
                "Null content checksum.");
        Assertions.assertNotNull( fieldsChecksum,
                "Null fields checksum.");
        Assertions.assertNotNull( combinedChecksum,
                "Null combined checksum.");

        Assertions.assertNotEquals(contentChecksum, fieldsChecksum);
        Assertions.assertNotEquals(fieldsChecksum, combinedChecksum);

        is.dispose();
    }

    @Test
    @Deprecated
    public void testWriteReadDeprecated() {
        MD5DocumentChecksummer c = new MD5DocumentChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setSourceFields("field1","field2");
        c.setSourceFieldsRegex("field.*");
        c.setCombineFieldsAndContent(true);
        c.setTargetField("target");
        c.setOnSet(PropertySetter.PREPEND);
        XML.assertWriteRead(c, "documentChecksummer");
    }
}
