/* Copyright 2017-2018 Norconex Inc.
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
package com.norconex.collector.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.lang3.ClassUtils;

import com.norconex.commons.lang.xml.XML;


/**
 * @author Pascal Essiembre
 * @since 1.8.0
 */
public final class TestUtil {

    private TestUtil() {
        super();
    }
    public static void testValidation(String xmlResource) throws IOException {
        testValidation(TestUtil.class.getResourceAsStream(xmlResource));

    }
    public static void testValidation(Class<?> clazz) throws IOException {
        testValidation(clazz, ClassUtils.getShortClassName(clazz) + ".xml");
    }
    public static void testValidation(Class<?> clazz, String xmlResource)
            throws IOException {
        testValidation(clazz.getResourceAsStream(xmlResource));

    }
    public static void testValidation(
            InputStream xmlStream) throws IOException {

        try (Reader r = new InputStreamReader(xmlStream)) {
            assertEquals("Validation warnings/errors were found.",
                    0, new XML(r).validate().size());
        }
    }
}
