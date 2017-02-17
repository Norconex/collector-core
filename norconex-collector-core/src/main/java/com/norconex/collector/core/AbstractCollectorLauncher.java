/* Copyright 2014-2017 Norconex Inc.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.norconex.commons.lang.EqualsUtil;
import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.log.CountingConsoleAppender;

/**
 * Encapsulates most of the logic for launching a collector implementation
 * from its main method.
 * @author Pascal Essiembre
 */
public abstract class AbstractCollectorLauncher {

    private static final Logger LOG = 
            LogManager.getLogger(AbstractCollectorLauncher.class);
    
    public static final String ARG_ACTION = "action";
    public static final String ARG_ACTION_START = "start";
    public static final String ARG_ACTION_RESUME = "resume";
    public static final String ARG_ACTION_STOP = "stop";
    /**
     * @deprecated Since 1.8.0, use -k or --checkcfg argument instead.
     */
    @Deprecated
    public static final String ARG_ACTION_CHECKCONFIG = "checkcfg";
    public static final String ARG_CONFIG = "config";
    public static final String ARG_VARIABLES = "variables";
    public static final String ARG_CHECKCFG = "checkcfg";    
    
    /**
     * Constructor.
     */
    public AbstractCollectorLauncher() {
    }

    public void launch(String[] args) {
        CommandLine cmd = parseCommandLineArguments(args);
        String action = cmd.getOptionValue(ARG_ACTION);
        File configFile = new File(cmd.getOptionValue(ARG_CONFIG));
        File varFile = null;
        if (cmd.hasOption(ARG_VARIABLES)) {
            varFile = new File(cmd.getOptionValue(ARG_VARIABLES));
        }
        
        try {
            // Validate arguments
            if (!configFile.isFile()) {
                System.err.println("Invalid configuration file path: "
                        + configFile.getAbsolutePath());
                System.exit(-1);
            }
            if (varFile != null && !varFile.isFile()) {
                System.err.println("Invalid variable file path: "
                        + configFile.getAbsolutePath());
                System.exit(-1);
            }

            // Proceed
            ICollectorConfig config = 
                    loadCommandLineConfig(cmd, configFile, varFile);
            ICollector collector = createCollector(config);
            if (ARG_ACTION_START.equalsIgnoreCase(action)) {
                collector.start(false);
            } else if (ARG_ACTION_RESUME.equalsIgnoreCase(action)) {
                collector.start(true);
            } else if (ARG_ACTION_STOP.equalsIgnoreCase(action)) {
                collector.stop();
            } else if (ARG_ACTION_CHECKCONFIG.equalsIgnoreCase(action)) {
                System.out.println("--- CONFIGURATION CHECKED ---");
                System.out.println("Verify no errors were printed.");
                System.out.println("Please note that while your configuration "
                        + "may load without errors, it may ");
                System.out.println("still contain misconfiguration that "
                        + "will only show up at runtime.");
            }
        } catch (Exception e) {
            PrintStream err = System.err;
            File errorFile = new File(
                    "./error-" + System.currentTimeMillis() + ".log");
            err.println("\n\nAn ERROR occured:\n\n"
                  + e.getLocalizedMessage());
            err.println("\n\nDetails of the error has been stored at: "
                    + errorFile.getAbsolutePath() + "\n\n");
            try {
                PrintWriter w = new PrintWriter(errorFile, CharEncoding.UTF_8);
                e.printStackTrace(w);
                w.flush();
                w.close();
            } catch (FileNotFoundException | UnsupportedEncodingException e1) {
                err.println("\n\nCannot write error file. " 
                        + e1.getLocalizedMessage());
            }
            System.exit(-1);
        }
    }
    
    private ICollectorConfig loadCommandLineConfig(
            CommandLine cmd, File configFile, File varFile) {
        ICollectorConfig config = null;
        CountingConsoleAppender appender = null;
        try {
            if (cmd.hasOption(ARG_CHECKCFG)) {
                appender = new CountingConsoleAppender();
                appender.startCountingFor(
                        XMLConfigurationUtil.class, Level.WARN);
            }
            config = new CollectorConfigLoader(
                    getCollectorConfigClass()).loadCollectorConfig(
                            configFile, varFile);
            if (cmd.hasOption(ARG_CHECKCFG)) {
                if (!appender.isEmpty()) {
                    System.err.println("There were " + appender.getCount()
                            + " XML configuration error(s).");
                    System.exit(-1);
                } else if (cmd.hasOption(ARG_ACTION)) {
                    LOG.info("No XML configuration errors.");
                } else {
                    System.out.println("No XML configuration errors.");
                }
            }
        } catch (IOException e) {
            System.err.println("A problem occured loading configuration.");
            e.printStackTrace(System.err);
            System.exit(-1);
        } finally {
            if (appender != null) {
                appender.stopCountingFor(XMLConfigurationUtil.class);
            }
        }
        return config;

    }
    
    protected CommandLine parseCommandLineArguments(String[] args) {
        Options options = new Options();
        options.addOption("c", ARG_CONFIG, true, 
                "Required: Collector configuration file.");
        options.addOption("v", ARG_VARIABLES, true, 
                "Optional: variable file.");
        options.addOption("a", ARG_ACTION, true, 
                "Required: one of start|resume|stop");
        options.addOption("k", ARG_CHECKCFG, false,   
                "Validates XML configuration. When combined "
              + "with -a, prevents execution on configuration "
              + "error.");
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
            if(!cmd.hasOption(ARG_CONFIG)
                    || (!cmd.hasOption(ARG_CHECKCFG)
                            && (!cmd.hasOption(ARG_ACTION)
                                    || !isActionValid(cmd)))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("<collector-script>", options );
                System.exit(-1);
            }
        } catch (ParseException e) {
            System.err.println("Could not parse arguments.");
            e.printStackTrace(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("<collector-script>", options );
            System.exit(-1);
        }
        return cmd;
    }
    
    private boolean isActionValid (CommandLine cmd) {
        String action = cmd.getOptionValue(ARG_ACTION);
        if (ARG_ACTION_CHECKCONFIG.equals(action)) {
            System.err.println("-action checkcfg is deprecated, "
                    + "use -k or --checkcfg instead.");
            return false;
        }
        return EqualsUtil.equalsAny(action,
                ARG_ACTION_START, ARG_ACTION_RESUME, ARG_ACTION_STOP);
    }
    
    protected abstract Class<? extends AbstractCollectorConfig> 
            getCollectorConfigClass();
    protected abstract ICollector createCollector(
            ICollectorConfig config);
}
