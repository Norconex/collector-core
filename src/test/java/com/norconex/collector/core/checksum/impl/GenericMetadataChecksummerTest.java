/* Copyright 2017-2021 Norconex Inc.
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

import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Test;

import com.norconex.collector.core.TestUtil;
import com.norconex.commons.lang.map.PropertySetter;
import com.norconex.commons.lang.text.TextMatcher;
import com.norconex.commons.lang.xml.XML;


/**
 * @author Pascal Essiembre
 */
public class GenericMetadataChecksummerTest {

    @Test
    @Deprecated
    public void testWriteRead() {
        GenericMetadataChecksummer c = new GenericMetadataChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setToField("myToField");
        c.setFieldMatcher(TextMatcher.csv("field1,field2"));
        c.setOnSet(PropertySetter.OPTIONAL);
        XML.assertWriteRead(c, "metadataChecksummer");
    }
    @Test
    public void testValidation() throws IOException {
        TestUtil.testValidation(getClass());
    }

    @Test
    @Deprecated
    public void testWriteReadDeprecated() {
        GenericMetadataChecksummer c = new GenericMetadataChecksummer();
        c.setDisabled(true);
        c.setKeep(true);
        c.setSourceFields("field1","field2");
        c.setSourceFieldsRegex("field.*");
        c.setTargetField("target");
        c.setOnSet(PropertySetter.OPTIONAL);
        XML.assertWriteRead(c, "metadataChecksummer");
    }
    @Test
    @Deprecated
    public void testValidationDeprecated() throws IOException {
        TestUtil.testValidation(getClass(),
                ClassUtils.getShortClassName(getClass()) + "Deprecated.xml");
    }
}
