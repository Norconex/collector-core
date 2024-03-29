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
package com.norconex.collector.core.checksum;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.text.TextMatcher;

/**
 * Checksum utility methods.
 * @author Pascal Essiembre
 * @since 1.9.0
 */
public final class ChecksumUtil {

    //TODO move to Importer and have checksum handlers?

    private static final Logger LOG =
            LoggerFactory.getLogger(ChecksumUtil.class);

    private ChecksumUtil() {
        super();
    }

    public static String checksumMD5(InputStream is) throws IOException {
        try (InputStream stream = is) {
            String checksum = DigestUtils.md5Hex(stream);
            if (LOG.isDebugEnabled()) {
                LOG.debug("MD5 checksum from input stream: {}", checksum);
            }
            return checksum;
        }
    }
    public static String checksumMD5(String text) {
        if (text == null) {
            return null;
        }
        String checksum = DigestUtils.md5Hex(text);
        if (LOG.isDebugEnabled()) {
            LOG.debug("MD5 checksum from string: {}", checksum);
        }
        return checksum;
    }

    public static String metadataChecksumMD5(
            Properties metadata, TextMatcher fieldMatcher) {
        String checksum =
                checksumMD5(metadataChecksumPlain(metadata, fieldMatcher));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Metadata checksum (MD5) from {} : \"{}\".",
                    fieldMatcher, checksum);
        }
        return checksum;
    }
    public static String metadataChecksumPlain(
            Properties metadata, TextMatcher fieldMatcher) {
        if (metadata == null || fieldMatcher == null
                || isBlank(fieldMatcher.getPattern())) {
            return null;
        }

        StringBuilder b = new StringBuilder();
        Properties props = metadata.matchKeys(fieldMatcher);
        if (!props.isEmpty()) {
            List<String> sortedFields = new ArrayList<>(props.keySet());
            // Sort to make sure field order does not affect checksum.
            Collections.sort(sortedFields);
            for (String field : sortedFields) {
                appendValues(b, field, metadata.getStrings(field));
            }
        }

        String checksum = b.toString();
        if (LOG.isDebugEnabled() && StringUtils.isNotBlank(checksum)) {
            LOG.debug("Metadata checksum (plain text) from {} : \"{}\".",
                    StringUtils.join(props.keySet(), ','), checksum);
        }
        return StringUtils.trimToNull(checksum);
    }

    @Deprecated
    public static String metadataChecksumMD5(
            Properties properties, String fieldsRegex, List<String> fields) {
        String checksum = checksumMD5(
                metadataChecksumPlain(properties, fieldsRegex, fields));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Metadata checksum (MD5) from {} : \"{}\".",
                    StringUtils.join(fields, ','), checksum);
        }
        return checksum;
    }
    @Deprecated
    public static String metadataChecksumPlain(
            Properties metadata, String fieldsRegex, List<String> fields) {

        if (CollectionUtils.isEmpty(fields)
                && StringUtils.isBlank(fieldsRegex)) {
            return null;
        }

        StringBuilder b = new StringBuilder();

        // From CSV
        if (CollectionUtils.isNotEmpty(fields)) {
            List<String> sortedFields = new ArrayList<>(fields);
            // Sort to make sure field order does not affect checksum.
            Collections.sort(sortedFields);
            for (String field : sortedFields) {
                appendValues(b, field, metadata.getStrings(field));
            }
        }
        // From Regex
        if (StringUtils.isNotBlank(fieldsRegex)) {
            Pattern p = Pattern.compile(fieldsRegex);
            for (Entry<String, List<String>> entry : metadata.entrySet()) {
                if (p.matcher(entry.getKey()).matches()) {
                    appendValues(b, entry.getKey(), entry.getValue());
                }
            }
        }

        String checksum = b.toString();
        if (LOG.isDebugEnabled() && StringUtils.isNotBlank(checksum)) {
            LOG.debug("Metadata checksum (plain text) from {} : \"{}\".",
                    StringUtils.join(fields, ','), checksum);
        }
        return StringUtils.trimToNull(checksum);
    }

    private static void appendValues(
            StringBuilder b, String field, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                b.append(field).append('=');
                b.append(value).append(';');
            }
        }
    }
}
