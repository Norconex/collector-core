/* Copyright 2020 Norconex Inc.
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
package com.norconex.collector.core.cmdline;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.commons.lang.xml.XML;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Resolve all includes and variables substitution and print the
 * resulting configuration to facilitate sharing.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "configrender",
    description = "Render effective configuration"
)
public class ConfigRenderCommand extends AbstractSubCommand {

    @Option(names = { "-o", "-output" },
            description = "Render to a file",
            required = false)
    private Path output;

    @Option(names = { "-i", "-indent" },
            description = "Number of spaces used for indentation (default: 2).",
            required = false)
    private int indent = 2;

    @Override
    public void runCommand() {
        XML xml = new XML("collector");
        getCollector().getCollectorConfig().saveToXML(xml);

        String renderedConfig = xml.toString(indent);

        if (output != null) {
            try {
                FileUtils.write(output.toFile(), renderedConfig, UTF_8);
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        } else {
            printOut(renderedConfig);
        }
        System.exit(0);
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }
}
