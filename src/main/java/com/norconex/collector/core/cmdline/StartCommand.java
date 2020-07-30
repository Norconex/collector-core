/* Copyright 2019-2020 Norconex Inc.
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Start the Collector.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "start",
    description = "Start the Collector"
)
public class StartCommand extends AbstractSubCommand {

    @Option(names = { "-clean" },
            description = "Clean the Collector crawl store "
                    + "before start. Same as invoking the \"clean\" and "
                    + "\"start\" commands one after the other.",
            required = false)
    private boolean clean;

    @Override
    public void runCommand() {
        if (clean) {
            getCollector().clean();
        }
        getCollector().start();
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
