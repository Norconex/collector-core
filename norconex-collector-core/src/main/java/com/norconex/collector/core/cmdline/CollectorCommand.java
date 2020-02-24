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

import java.util.concurrent.Callable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.Collector;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.PicocliException;
import picocli.CommandLine.Spec;

//@Command(name = "fs", synopsisSubcommandLabel = "(list | add | delete)",
//subcommands = {List.class, Add.class, Delete.class}, mixinStandardHelpOptions = true)


/**
 * Encapsulates command line arguments when running the Collector from
 * a command prompt.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "<app>",
    description = "%nOptions:",
//    customSynopsis = "<collector> [OPTIONS] [COMMANDS]",
//    abbreviateSynopsis = true,
//    headerHeading = CollectorCommand.NORCONEX,
//    mixinStandardHelpOptions = true,
    sortOptions = false,
    separator = " ",
    commandListHeading = "%nCommands:%n",
    footerHeading = "%nExamples:%n",
    footer = "%n  Start the Collector:%n"
           + "%n    <collector> start -config=/path/to/config.xml%n"
           + "%n  Stop the Collector:%n"
           + "%n    <collector> stop -config=/path/to/config.xml%n"
           + "%n  Get usage help on \"check\" command:%n"
           + "%n    <collector> help check%n",
    subcommands = {
        HelpCommand.class,
        StartCommand.class,
        StopCommand.class,
        CheckCommand.class,
        CleanCommand.class,
        CommitCommand.class,
        StoreExportCommand.class,
        StoreImportCommand.class
    }
)
public class CollectorCommand
        implements Callable<Integer>, IExecutionExceptionHandler {

//    static final String NORCONEX =
//            " _   _  ___  ____   ____ ___  _   _ _______  __%n"
//          + "| \\ | |/ _ \\|  _ \\ / ___/ _ \\| \\ | | ____\\ \\/ /%n"
//          + "|  \\| | | | | |_) | |  | | | |  \\| |  _|  \\  / %n"
//          + "| |\\  | |_| |  _ <| |__| |_| | |\\  | |___ /  \\ %n"
//          + "|_| \\_|\\___/|_| \\_\\\\____\\___/|_| \\_|_____/_/\\_\\%n%n"
//          + "%n";

    private final Collector collector;

    @Option(
        names = {"-h", "-help"},
        usageHelp = true,
        description = "Show this help message and exit."
    )
    private boolean help;
    @Option(
        names = {"-v", "-version"},
        description = "Show the Collector version and exit."
    )
    private boolean version;

    @Spec
    private CommandSpec spec;

    public CollectorCommand(Collector collector) {
        super();
        this.collector = collector;
    }

    Collector getCollector() {
        return collector;
    }


//    public boolean isHelp() {
//        return help;
//    }
//    public void setHelp(boolean help) {
//        this.help = help;
//    }
//
//    public boolean isVersion() {
//        return version;
//    }
//    public void setVersion(boolean version) {
//        this.version = version;
//    }



    @Override
    public Integer call() throws Exception {
        if (version) {
            collector.getReleaseVersions().stream().forEach(
                    v -> spec.commandLine().getOut().println(v));
            System.exit(0);
        }

        //TODO use JSR Validation
        // Validate arguments
//        if (action != null && !isHelp() && !isVersion()) {
//            if (!configFile.toFile().isFile()) {
//                throw new IllegalArgumentException(String.format(
//                        "Configuration file does not exist or "
//                                + "path is invalid: '%s'.",
//                        configFile.toAbsolutePath()));
//            }
//            if (variablesFile != null && !variablesFile.toFile().isFile()) {
//                throw new IllegalArgumentException(String.format(
//                        "Variables file does not exist or path is "
//                                + "invalid: '%s'.",
//                        variablesFile.toAbsolutePath()));
//            }
//        }

//        if (action == null && !isHelp() && !isVersion()) {
//            throw new IllegalArgumentException("No arguments specified.");
//        }
        return 0;
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine,
            ParseResult parseResult) throws Exception {
        if (ex instanceof PicocliException
                || ex instanceof IllegalArgumentException) {
            commandLine.getErr().println(ex.getMessage());
            commandLine.getErr().println();
            commandLine.usage(commandLine.getErr());
            return -1;
        }
        throw ex;
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
