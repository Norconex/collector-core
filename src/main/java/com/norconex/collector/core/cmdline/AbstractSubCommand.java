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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import com.norconex.collector.core.Collector;
import com.norconex.commons.lang.config.ConfigurationLoader;
import com.norconex.commons.lang.xml.ErrorHandlerCapturer;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

/**
 * Base class for subcommands.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public abstract class AbstractSubCommand implements Callable<Integer> {

    @ParentCommand
    private CollectorCommand parent;

    @Spec
    private CommandSpec spec;

    @Option(
        names = {"-c", "-config"},
        paramLabel = "FILE",
        description = "Path to Collector configuration file.",
        required = true
    )
    private Path configFile;
    @Option(
        names = {"-variables"},
        paramLabel = "FILE",
        description = "Path to variables file."
    )
    private Path variablesFile;
    @Option(
        names = {"-crawlers"},
        paramLabel = "<crawler>",
        description = "TO IMPLEMENT: Restrict the command to one or more "
                + "crawler (comma-separated).",
        split = ","
    )
    private final List<String> crawlers = new ArrayList<>();


    protected void printOut() {
        commandLine().getOut().println();
    }
    protected void printOut(String str) {
        commandLine().getOut().println(str);
    }
    protected void printErr() {
        commandLine().getErr().println();
    }
    protected void printErr(String str) {
        commandLine().getErr().println(str);
    }
    protected CommandLine commandLine() {
        return spec.commandLine();
    }
    protected Collector getCollector() {
        return parent.getCollector();
    }

    public Path getConfigFile() {
        return configFile;
    }
    public void setConfigFile(Path configFile) {
        this.configFile = configFile;
    }
    public Path getVariablesFile() {
        return variablesFile;
    }
    public void setVariablesFile(Path variablesFile) {
        this.variablesFile = variablesFile;
    }
    public List<String> getCrawlers() {
        return crawlers;
    }

    protected int loadConfig() {
        if (getConfigFile() == null || !getConfigFile().toFile().isFile()) {
            printErr("Configuration file does not exist or is not valid: "
                    + getConfigFile().toFile().getAbsolutePath());
            return -1;
//            System.exit(0);
        }
        ErrorHandlerCapturer eh = new ErrorHandlerCapturer(getClass());
        new ConfigurationLoader()
                .setVariablesFile(getVariablesFile())
                .loadFromXML(getConfigFile(),
                        getCollector().getCollectorConfig(), eh);
        if (!eh.getErrors().isEmpty()) {
            printErr();
            printErr(eh.getErrors().size()
                    + " XML configuration errors detected:");
            printErr();
            eh.getErrors().stream().forEach(er ->
                    printErr(er.getMessage()));
            return  -1;
//            System.exit(0);
        }
        return 0;
    }

    @Override
    public Integer call() throws Exception {
        int exitVal = loadConfig();
        if (exitVal != 0) {
            return exitVal;
        }
        try {
            runCommand();
            return 0;
        } catch (Exception e) {
            if (e instanceof NullPointerException && e.getMessage() == null) {
                //TODO maybe cache message and if blank always print
                //stacktrace regardless of exception type?
                printErr("ERROR: " + ExceptionUtils.getStackTrace(e));
            } else {
                // TODO Consider using Nx Lang ExceptionUtil
                MutableInt mi = new MutableInt();
                ExceptionUtils.getThrowableList(e).forEach(ex -> {
                    int i = mi.getAndIncrement();
                    String msg = ex.getLocalizedMessage();
                    if (i == 0) {
                        printErr("ERROR: " + msg);
                    } else {
                        printErr(StringUtils.repeat(' ', i * 2) + "→ " + msg);
                    }
                });
            }
            return -1;
        }
    }

    protected abstract void runCommand();

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
