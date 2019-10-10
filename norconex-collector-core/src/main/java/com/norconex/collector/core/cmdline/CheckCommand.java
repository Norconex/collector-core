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

import picocli.CommandLine.Command;

/**
 * Validate configuration file format and quit.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "check",
    description = "Validate configuration file format."
)
public class CheckCommand extends AbstractSubCommand {
    @Override
    public void runCommand() {
        // If it reaches here, that means no errors were found. Simply say so.
        printOut();
        printOut("No XML configuration errors detected.");
        System.exit(0);
    }
}
