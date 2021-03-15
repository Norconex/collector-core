/* Copyright 2019-2021 Norconex Inc.
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

import picocli.CommandLine.Command;

/**
 * Stop the Collector.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "stop",
    description = "Stop the Collector"
)
public class StopCommand extends AbstractSubCommand {
    @Override
    public void runCommand() {
        // Because the collector we stop may be running in a separate JVM
        // instance, we do not call "close()" directly.  We issue the request
        // instead and the running Collector will react to the request.
        getCollector().fireStopRequest();
    }
}
