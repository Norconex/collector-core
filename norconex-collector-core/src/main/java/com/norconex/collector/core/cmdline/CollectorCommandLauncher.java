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

//    private static final String NORCONEX =
//              " _   _  ___  ____   ____ ___  _   _ _______  __%n"
//            + "| \\ | |/ _ \\|  _ \\ / ___/ _ \\| \\ | | ____\\ \\/ /%n"
//            + "|  \\| | | | | |_) | |  | | | |  \\| |  _|  \\  / %n"
//            + "| |\\  | |_| |  _ <| |__| |_| | |\\  | |___ /  \\ %n"
//            + "|_| \\_|\\___/|_| \\_\\\\____\\___/|_| \\_|_____/_/\\_\\%n%n"
//            + "%s%n";
    //TODO make static?
    public void launch(Collector collector, String[] args) {
        Objects.requireNonNull(collector, "'collector' must not be null.");

        CollectorCommand cmd = new CollectorCommand(collector);
        CommandLine cmdLine = new CommandLine(cmd);

//        cmdLine.getOut().println(header(
//                VersionUtil.getDetailedVersion(collector.getClass())));
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
//
//        parseCommandLineArguments(cmdLine, cmd, args);

//        cmdLine.addSubcommand(new CheckCommand());

                //parseCommandLineArguments(cmd, args);
//        cmdLine.


        //TODO use LOG where it makes sense?
        //TODO create class/lambda for each action

        //TODO Have -check instead of command (since it does not "do" anything)?
        //TODO Have -action ACTION instead of commands?

        // command is already validated at this point.

//        if (cmd.isHelp()) {
//            cmdLine.usage(cmdLine.getOut());
//            System.exit(0);
//        }
//        if (cmd.isVersion()) {
//            collector.getReleaseVersions().stream().forEach(
//                    v -> cmdLine.getOut().println(" " + v));
//            System.exit(0);
//        }

        //For testing:
///        cmdLine.usage(cmdLine.getOut());


//        Action action = cmd.getAction();

//        if (action.isCheckConfig()) {
//            try {
//                loadConfig(collector, cmd);
//                cmdLine.getOut().println();
//                cmdLine.getOut().println(
//                        " No XML configuration errors detected.");
//            } catch (CollectorException e) {
//                Throwable t = ExceptionUtils.getRootCause(e);
//                if (t instanceof XMLValidationException) {
//                    List<XMLValidationError> errors =
//                            ((XMLValidationException) t).getErrors();
//                    cmdLine.getOut().println();
//                    cmdLine.getOut().println(errors.size()
//                            + " XML configuration errors detected:");
//                    cmdLine.getOut().println();
//                    errors.stream().forEach(er ->
//                            cmdLine.getOut().println("  " + er.getMessage()));
//                } else {
//                    throw e;
//                }
//            }
//            System.exit(0);
//        }
//
//        // From this point on, config might be needed, so load it
//        boolean hasConfig = loadConfig(collector, cmd);
//
////        if (action.isCheckConfig()) {
////            System.out.println("TODO: Check Config");
////        }
//
//        //TODO do the following only if no config error when check is specified (or if not specified).
//
//        if (action.isClean()) {
//            System.out.println("TODO: Clean");
//        }
//        if (action.isStart()) {
//            System.out.println("TODO: Start");
//        }
//
//
//        System.out.println("CFG FILE: " + cmd.getConfigFile());
//        System.out.println("VAR FILE: " + cmd.getVariablesFile());
//        System.out.println("CHECKCFG: " + cmd.getAction().isCheckConfig());
//        System.out.println("   CLEAN: " + cmd.getAction().isClean());
//        System.out.println("   START: " + cmd.getAction().isStart());
//        System.out.println("    STOP: " + cmd.getAction().isStop());
//        System.out.println("    HELP: " + cmd.isHelp());
//        System.out.println(" VERSION: " + cmd.isVersion());


//        CommandLine cmd = parseCommandLineArguments(args);
//        String action = cmd.getOptionValue(ARG_ACTION);
//        Path configFile = Paths.get(cmd.getOptionValue(ARG_CONFIG));
//        Path varFile = null;
//        if (cmd.hasOption(ARG_VARIABLES)) {
//            varFile = Paths.get(cmd.getOptionValue(ARG_VARIABLES));
//        }
//
//        try {
//            // Validate arguments
//            if (!configFile.toFile().isFile()) {
//                System.err.println("Invalid configuration file path: "
//                        + configFile.toAbsolutePath());
//                System.exit(-1);
//            }
//            if (varFile != null && !varFile.toFile().isFile()) {
//                System.err.println("Invalid variable file path: "
//                        + varFile.toAbsolutePath());
//                System.exit(-1);
//            }
//
//            // Proceed
//            CollectorConfig config =
//                    loadCommandLineConfig(cmd, configFile, varFile);
//            Collector collector = createCollector(config);
//            if (ARG_ACTION_START.equalsIgnoreCase(action)) {
//                collector.start(false);
//            } else if (ARG_ACTION_RESUME.equalsIgnoreCase(action)) {
//                collector.start(true);
//            } else if (ARG_ACTION_STOP.equalsIgnoreCase(action)) {
//                collector.stop();
////            } else if (ARG_ACTION_CHECKCONFIG.equalsIgnoreCase(action)) {
////                System.out.println("--- CONFIGURATION CHECKED ---");
////                System.out.println("Verify no errors were printed.");
////                System.out.println("Please note that while your configuration "
////                        + "may load without errors, it may ");
////                System.out.println("still contain misconfiguration that "
////                        + "will only show up at runtime.");
//            }
//        } catch (Exception e) {
//            PrintStream err = System.err;
//            File errorFile = new File(
//                    "./error-" + System.currentTimeMillis() + ".log");
//            err.println("\n\nAn ERROR occured:\n\n"
//                  + e.getLocalizedMessage());
//            err.println("\n\nDetails of the error has been stored at: "
//                    + errorFile.getAbsolutePath() + "\n\n");
//            try {
//                PrintWriter w = new PrintWriter(
//                        errorFile, StandardCharsets.UTF_8.toString());
//                e.printStackTrace(w);
//                w.flush();
//                w.close();
//            } catch (FileNotFoundException | UnsupportedEncodingException e1) {
//                err.println("\n\nCannot write error file. "
//                        + e1.getLocalizedMessage());
//            }
//            System.exit(-1);
//        }
    }

//    // returns whether a config file was specified (and loaded)
//    private boolean loadConfig(Collector collector, CollectorCommand cmd) {
//        // Collector config can't be null
//        // Config file can be null if we asked for version.
//        if (cmd.getConfigFile() != null) {
//            collector.getCollectorConfig().loadFromXML(new ConfigurationLoader()
//                    .setVariablesFile(cmd.getVariablesFile())
//                    .loadXML(cmd.getConfigFile()));
//            return true;
//        }
//        return false;
//    }

//    private String header(String version) {
//        String vline = "--[ " + substringBeforeLast(
//                removeStart(version.toUpperCase(), "NORCONEX "), " (") + " ]--";
//        int pad = Math.max(0, 48 - vline.length()) / 2;
//        vline = repeat('-', pad) + vline + repeat('-', pad);
//        return String.format(NORCONEX, vline);
//    }

//    protected void parseCommandLineArguments(
//            CommandLine cmdLine,
//            CollectorCommand cmd, String[] args) {
//        //CommandLine cmdLine = new CommandLine(cmd);
//        cmdLine.setExecutionExceptionHandler(cmd);
//        int exitCode = cmdLine.execute(args);
//        if (exitCode != 0) {
//            //TODO Have exit here or throw exception, or leave it to caller?
//            System.exit(exitCode);
//        }
////        return cmdLine;
//    }

}
