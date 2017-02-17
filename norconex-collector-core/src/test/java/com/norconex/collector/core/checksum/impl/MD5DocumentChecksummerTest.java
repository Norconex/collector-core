/* Copyright 2015-2017 Norconex Inc.
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

import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
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
        doc.getMetadata().addString("field1", "value1.1", "value1.2");
        doc.getMetadata().addString("field2", "value2");
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
                "Checksum for no matching fie3lds should have been null.",
                checksum3);

        is.dispose();
    }
    
    @Test
    public void testWriteRead() throws IOException {
        MD5DocumentChecksummer c = new MD5DocumentChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setSourceFields("field1","field2");
        c.setTargetField("target");
        System.out.println("Writing/Reading this: " + c);
        XMLConfigurationUtil.assertWriteRead(c);
    }
}
