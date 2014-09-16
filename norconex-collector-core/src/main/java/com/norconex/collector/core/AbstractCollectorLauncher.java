/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.norconex.commons.lang.EqualsUtil;

/**
 * @author Pascal Essiembre
 *
 */
public abstract class AbstractCollectorLauncher {

    public static final String ARG_ACTION = "action";
    public static final String ARG_ACTION_START = "start";
    public static final String ARG_ACTION_RESUME = "resume";
    public static final String ARG_ACTION_STOP = "stop";
    public static final String ARG_CONFIG = "config";
    public static final String ARG_VARIABLES = "variables";
    
    
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
            ICollectorConfig config = new CollectorConfigLoader(
                    getCollectorConfigClass()).loadCollectorConfig(
                            configFile, varFile);
            ICollector collector = createCollector(config);
            if (ARG_ACTION_START.equalsIgnoreCase(action)) {
                collector.start(false);
            } else if (ARG_ACTION_RESUME.equalsIgnoreCase(action)) {
                collector.start(true);
            } else if (ARG_ACTION_STOP.equalsIgnoreCase(action)) {
                collector.stop();
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
                PrintWriter w = new PrintWriter(errorFile);
                e.printStackTrace(w);
                w.flush();
                w.close();
            } catch (FileNotFoundException e1) {
                err.println("\n\nCannot write error file. " 
                        + e1.getLocalizedMessage());
            }
        }
    }
    
    protected CommandLine parseCommandLineArguments(String[] args) {
        Options options = new Options();
        options.addOption("c", ARG_CONFIG, true, 
                "Required: HTTP Collector configuration file.");
        options.addOption("v", ARG_VARIABLES, true, 
                "Optional: variable file.");
        options.addOption("a", ARG_ACTION, true, 
                "Required: one of start|resume|stop");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
            if(!cmd.hasOption(ARG_CONFIG) || !cmd.hasOption(ARG_ACTION)
                    || EqualsUtil.equalsNone(cmd.getOptionValue(ARG_ACTION),
                        ARG_ACTION_START, ARG_ACTION_RESUME, ARG_ACTION_STOP)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "collector-http[.bat|.sh]", options );
                System.exit(-1);
            }
        } catch (ParseException e) {
            PrintStream err = System.err;
            err.println("Could not parse arguments.");
            e.printStackTrace(System.err);
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "collector-http[.bat|.sh]", options );
            System.exit(-1);
        }
        return cmd;
    }
    
    protected abstract Class<? extends AbstractCollectorConfig> 
            getCollectorConfigClass();
    protected abstract ICollector createCollector(
            ICollectorConfig config);
}
