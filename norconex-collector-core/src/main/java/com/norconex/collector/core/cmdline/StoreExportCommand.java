/* Copyright 2019 Norconex Inc.
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

import java.nio.file.Path;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Export crawl store to specified file.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "storeexport",
    description = "Export crawl store to specified directory."
)
public class StoreExportCommand extends AbstractSubCommand {

    @Option(names = { "-d", "-dir" },
            description = "Export directory",
            required = true)
    private Path dir;
    //TODO add format?
    //TODO add compress?

    @Override
    public void runCommand() {
        getCollector().exportDataStore(dir);
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
