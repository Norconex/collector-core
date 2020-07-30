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

import java.util.Objects;

import com.norconex.collector.core.Collector;

import picocli.CommandLine;

/**
 * Launches a collector implementation from a string array representing
 * command line arguments.
 * @author Pascal Essiembre
 * @version 2.0.0
 */
public class CollectorCommandLauncher {

    //TODO make static?
    public void launch(Collector collector, String[] args) {
        Objects.requireNonNull(collector, "'collector' must not be null.");

        CollectorCommand cmd = new CollectorCommand(collector);
        CommandLine cmdLine = new CommandLine(cmd);

        cmdLine.setExecutionExceptionHandler(cmd);

        if (args.length == 0) {
            cmdLine.getErr().println("No arguments provided.");
            cmdLine.usage(cmdLine.getOut());
            System.exit(0);
        }

        int exitCode = cmdLine.execute(args);
        if (exitCode != 0) {
            //TODO Have exit here or throw exception, or leave it to caller?
            System.exit(exitCode);
        }
    }
}
