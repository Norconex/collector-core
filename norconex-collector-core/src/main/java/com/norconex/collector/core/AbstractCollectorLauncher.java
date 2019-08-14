/* Copyright 2014-2018 Norconex Inc.
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
package com.norconex.collector.core;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.commons.lang.EqualsUtil;
import com.norconex.commons.lang.xml.XMLValidationError;

/**
 * Encapsulates most of the logic for launching a collector implementation
 * from its main method.
 * @author Pascal Essiembre
 */
public abstract class AbstractCollectorLauncher {

    private static final Logger LOG =
            LoggerFactory.getLogger(AbstractCollectorLauncher.class);

    public static final String ARG_ACTION = "action";
    public static final String ARG_ACTION_START = "start";
    public static final String ARG_ACTION_RESUME = "resume";
    public static final String ARG_ACTION_STOP = "stop";

    public static final String ARG_CONFIG = "config";
    public static final String ARG_VARIABLES = "variables";
    public static final String ARG_CHECKCFG = "checkcfg";

    //TODO have a factory/data class passed as argument that hold the logic
    // specific to a single collector as opposed to have abstract methods.

    /**
     * Constructor.
     */
    public AbstractCollectorLauncher() {
    }

    public void launch(String[] args) {
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

    //TODO have a public method that accepts files as well (when embedding)
    private CollectorConfig loadCommandLineConfig(
            CommandLine cmd, Path configFile, Path varFile) {
        CollectorConfig config = null;

        List<XMLValidationError> errors = null;
//        try {
//            config = getCollectorConfigClass().newInstance();
//            errors = new ConfigurationLoader()
//                    .setVariablesFile(varFile)
//                    .loadXML(configFile)
//                    .populate(config);
//        } catch (InstantiationException | IllegalAccessException e) {
//            //TODO use logging instead?
//            System.err.println("A problem occured loading configuration.");
//            e.printStackTrace(System.err);
//            System.exit(-1);
//        }

        if (cmd.hasOption(ARG_CHECKCFG)) {
            if (CollectionUtils.isNotEmpty(errors)) {
                System.err.println("There were " + errors.size()
                        + " XML configuration error(s).");
                System.exit(-1);
            } else if (cmd.hasOption(ARG_ACTION)) {
                LOG.info("No XML configuration errors.");
            } else {
                System.out.println("No XML configuration errors.");
            }
        }
        return config;
    }

//    protected CommandLine parseCommandLineArguments(String[] args) {
//
//        CollectorCommand cmd = new CollectorCommand();
//        picocli.CommandLine cmdLine = new picocli.CommandLine(cmd);
//        cmdLine.setExecutionExceptionHandler(cmd);
//        int exitCode = cmdLine.execute(args);
//System.out.println("EXIT: " + exitCode);
//        if (exitCode != 0) {
//            cmdLine.usage(System.out);
//        }
//
////        CollectorCommand cmd = cmdLine.getCommand();
//
//        System.out.println("CFG FILE: " + cmd.getConfigFile());
//        System.out.println("VAR FILE: " + cmd.getVariablesFile());
//        System.out.println("CHECKCFG: " + cmd.getAction().isCheckConfig());
//        System.out.println("   CLEAN: " + cmd.getAction().isClean());
//        System.out.println("   START: " + cmd.getAction().isStart());
//        System.out.println("    STOP: " + cmd.getAction().isStop());
//        System.out.println("    HELP: " + cmd.isHelp());
//        System.out.println(" VERSION: " + cmd.isVersion());
//
//
//
//
////        if (exitCode)
////
////
////        picocli.CommandLine commandLine = new picocli.CommandLine(new CollectorCmdLineOptions());
////        commandLine.parseArgs(args);
////        if (commandLine.isUsageHelpRequested()) {
////           commandLine.usage(System.out);
////        } else if (commandLine.isVersionHelpRequested()) {
////           commandLine.printVersionHelp(System.out);
////        }
//
//        System.exit(exitCode);
//
//
////        Options options = new Options();
////        options.addOption("c", ARG_CONFIG, true,
////                "Required: Collector configuration file.");
////        options.addOption("v", ARG_VARIABLES, true,
////                "Optional: variable file.");
////        options.addOption("a", ARG_ACTION, true,
////                "Required: one of start|resume|stop");
////        options.addOption("k", ARG_CHECKCFG, false,
////                "Validates XML configuration. When combined "
////              + "with -a, prevents execution on configuration "
////              + "error.");
////
////        CommandLineParser parser = new DefaultParser();
////        CommandLine cmd = null;
////        try {
////            cmd = parser.parse(options, args);
////            if(!cmd.hasOption(ARG_CONFIG)
////                    || (!cmd.hasOption(ARG_CHECKCFG)
////                            && (!cmd.hasOption(ARG_ACTION)
////                                    || !isActionValid(cmd)))) {
////                HelpFormatter formatter = new HelpFormatter();
////                formatter.printHelp("<command>", options );
////                System.exit(-1);
////            }
////        } catch (ParseException e) {
////            System.err.println("Could not parse arguments.");
////            e.printStackTrace(System.err);
////            HelpFormatter formatter = new HelpFormatter();
////            formatter.printHelp("<collector-script>", options );
////            System.exit(-1);
////        }
////        return cmd;
//        return null;
//    }

    private boolean isActionValid (CommandLine cmd) {
        String action = cmd.getOptionValue(ARG_ACTION);
//        if (ARG_ACTION_CHECKCONFIG.equals(action)) {
//            System.err.println("-action checkcfg is deprecated, "
//                    + "use -k or --checkcfg instead.");
//            return false;
//        }
        return EqualsUtil.equalsAny(action,
                ARG_ACTION_START, ARG_ACTION_RESUME, ARG_ACTION_STOP);
    }

    protected abstract Class<? extends CollectorConfig>
            getCollectorConfigClass();
    protected abstract Collector createCollector(
            CollectorConfig config);
}

/*

--- new usage: ----------------

Humm... -start should always resume, unless clean is provided?
Humm... should distinguish between clearing a stopped session and clearing the cache (starting form scratch)

//  -start <type>  incremental|full|resume
// CHANGE: we can no longer just "start" an incomplete/stopped job instead of "resuming" it since that means dealing with a partial cache, so several deltas would be missing/wrong.

  -config <path>      Required
  -variables <path>   Optional (default check if one of same name)
  -start              (if no clean... always resumes if incomplete, always incremental)
  -stop
  -clean              start from scratch (clears cache + last progress -- Maybe have options distinguishing types of cleaning?)
  -checkcfg           (validate configuration "checkconfig" or "checkcfg" ?)
  -version
  -crawler <crawlerId>  restrict all command options to this specific crawler (can repeat? or have multiple args?)

At least one of start, stop, clean, checkcfg, or version must be specified.

Technically, -start|-stop are mutually exclusive, maybe use an OptionGroup for them?  The rest is not.


--- all options: ----------------
start incremental
start full (from scratch, cleaning first)
stop
resume (only if a previous job was stopped prematuraly)
checkcfg
clean
crawlstore export
crawlstore import
commit <dir> (commit the content of a directory).
---------------------------------


NEW:
        usage:
            -action                Required. start|resume|stop
            -c,--config <arg>      Required: Collector configuration file.
            -k,--checkcfg          Validates XML configuration. When combined with
                                   -a, prevents execution on configuration error.
            -v,--variables <arg>   Optional: variable file.

OLD:
        usage:
            -a,--action <arg>      Required: one of start|resume|stop
            -c,--config <arg>      Required: Collector configuration file.
            -k,--checkcfg          Validates XML configuration. When combined with
                                   -a, prevents execution on configuration error.
            -v,--variables <arg>   Optional: variable file.

*/
