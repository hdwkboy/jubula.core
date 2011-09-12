/*******************************************************************************
 * Copyright (c) 2004, 2011 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.app.autrun;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jubula.tools.constants.AutConfigConstants;
import org.eclipse.jubula.tools.constants.StringConstants;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.jubula.tools.registration.AutIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Starts an AUT and registers the AUT with an AUT Agent. In order to terminate 
 * at the right time (not too early, not too late) this application assumes 
 * that the following JVM parameters (or equivalent) are used:<ul>
 * <li>osgi.noShutdown=true</li> 
 * <li>eclipse.jobs.daemon=true</li> 
 * <li>eclipse.enableStateSaver=false</li> 
 * </ul>
 * These parameters are required because the original application was designed 
 * to run outside of an OSGi context, i.e. the application should end only 
 * when no non-daemon threads are active.
 * 
 * @author BREDEX GmbH
 * @created Dec 9, 2009
 */
public class AutRunApplication implements IApplication {
    
    /** the logger */
    private static final Logger LOG = 
        LoggerFactory.getLogger(AutRunApplication.class);
    
    /**
     * <code>LAUNCHER_NAME</code>
     */
    private static final String LAUNCHER_NAME = "autrun"; //$NON-NLS-1$

    /** <code>TOOLKIT_RCP</code> */
    private static final String TK_RCP = "rcp"; //$NON-NLS-1$

    /** <code>TK_SWT</code> */
    private static final String TK_SWT = "swt"; //$NON-NLS-1$

    /** <code>TK_SWING</code> */
    private static final String TK_SWING = "swing"; //$NON-NLS-1$

    /** <code>DEFAULT_NAME_TECHNICAL_COMPONENTS</code> */
    private static final boolean DEFAULT_NAME_TECHNICAL_COMPONENTS = true;

    /** <code>DEFAULT_AUT_AGENT_PORT</code> */
    private static final int DEFAULT_AUT_AGENT_PORT = 60000;

    /** <code>DEFAULT_AUT_AGENT_HOST</code> */
    private static final String DEFAULT_AUT_AGENT_HOST = "localhost"; //$NON-NLS-1$

    // - Command line options - Start //
    /** port number for the Aut Agent with which to register */
    private static final String OPT_AUT_AGENT_PORT = "p"; //$NON-NLS-1$

    /** port number for the Aut Agent with which to register */
    private static final String OPT_AUT_AGENT_PORT_LONG = "autagentport"; //$NON-NLS-1$
    
    /** hostname for the Aut Agent with which to register */
    private static final String OPT_AUT_AGENT_HOST = "a"; //$NON-NLS-1$
    
    /** hostname for the Aut Agent with which to register */
    private static final String OPT_AUT_AGENT_HOST_LONG = "autagenthost"; //$NON-NLS-1$
    
    /** help option */
    private static final String OPT_HELP = "h"; //$NON-NLS-1$
    
    /** hostname for the Aut Agent with which to register */
    private static final String OPT_HELP_LONG = "help"; //$NON-NLS-1$
    
    /** name of the AUT to register */
    private static final String OPT_AUT_ID = "i"; //$NON-NLS-1$
    
    /** name of the AUT to register */
    private static final String OPT_AUT_ID_LONG = "autid"; //$NON-NLS-1$
    
    /** flag for name generation for certain technical components */
    private static final String OPT_NAME_TECHNICAL_COMPONENTS = "g"; //$NON-NLS-1$

    /** flag for name generation for certain technical components */
    private static final String OPT_NAME_TECHNICAL_COMPONENTS_LONG = "generatenames"; //$NON-NLS-1$
    
    /** keyboard layout */
    private static final String OPT_KEYBOARD_LAYOUT = "k"; //$NON-NLS-1$

    /** keyboard layout */
    private static final String OPT_KEYBOARD_LAYOUT_LONG = "kblayout"; //$NON-NLS-1$

    /** AUT working directory */
    private static final String OPT_WORKING_DIR = "w"; //$NON-NLS-1$

    /** AUT working directory */
    private static final String OPT_WORKING_DIR_LONG = "workingdir"; //$NON-NLS-1$
    
    /** executable file used to start the AUT */
    private static final String OPT_EXECUTABLE = "e"; //$NON-NLS-1$
    
    /** executable file used to start the AUT */
    private static final String OPT_EXECUTABLE_LONG = "exec"; //$NON-NLS-1$
    // - Command line options - End //

    /**
     * prints help options
     * @param pe a parse Execption - may also be null
     */
    private static void printHelp(ParseException pe) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new OptionComparator());
        if (pe != null) {
            formatter.printHelp(
                    LAUNCHER_NAME,
                    StringConstants.EMPTY,
                    createCmdLineOptions(), 
                    "\n" + pe.getLocalizedMessage(),  //$NON-NLS-1$
                    true);
        } else {
            formatter.printHelp(LAUNCHER_NAME,
                    createCmdLineOptions(), true);
        }
    }

    /**
     * Creates and returns settings for starting an AUT based on the given
     * command line.
     *  
     * @param cmdLine Provides the settings for the AUT configuration.
     * @return new settings for starting an AUT.
     */
    private static Map<String, Object> createAutConfig(CommandLine cmdLine) {
        Map<String, Object> autConfig = new HashMap<String, Object>();
        if (cmdLine.hasOption(OPT_WORKING_DIR)) {
            autConfig.put(AutConfigConstants.WORKING_DIR, cmdLine
                    .getOptionValue(OPT_WORKING_DIR));
        } else {
            autConfig.put(AutConfigConstants.WORKING_DIR, System
                    .getProperty("user.dir")); //$NON-NLS-1$
        }
        
        if (cmdLine.hasOption(OPT_NAME_TECHNICAL_COMPONENTS)) {
            autConfig.put(AutConfigConstants.NAME_TECHNICAL_COMPONENTS, Boolean
                    .valueOf(cmdLine
                            .getOptionValue(OPT_NAME_TECHNICAL_COMPONENTS)));
        } else {
            autConfig.put(AutConfigConstants.NAME_TECHNICAL_COMPONENTS,
                    DEFAULT_NAME_TECHNICAL_COMPONENTS);
        }
        autConfig.put(AutConfigConstants.EXECUTABLE, cmdLine
                .getOptionValue(OPT_EXECUTABLE));
        
        if (cmdLine.hasOption(OPT_KEYBOARD_LAYOUT)) {
            autConfig.put(AutConfigConstants.KEYBOARD_LAYOUT, 
                    cmdLine.getOptionValue(OPT_KEYBOARD_LAYOUT));
        }
        
        String[] autArguments = cmdLine.getOptionValues(OPT_EXECUTABLE);
        if (autArguments.length > 1) {
            autConfig.put(AutConfigConstants.AUT_RUN_AUT_ARGUMENTS, 
                    ArrayUtils.subarray(autArguments, 1, autArguments.length));
        }
        
        return autConfig;
    }

    /**
     * @return the command line options available when invoking the main method. 
     */
    @SuppressWarnings("nls")
    private static Options createCmdLineOptions() {
        Options options = new Options();
        Option autAgentHostOption = 
            new Option(OPT_AUT_AGENT_HOST, true,
                "AUT Agent Host"
                    + " (default \"" + DEFAULT_AUT_AGENT_HOST + "\")");
        autAgentHostOption.setLongOpt(OPT_AUT_AGENT_HOST_LONG);
        autAgentHostOption.setArgName("hostname");
        options.addOption(autAgentHostOption);

        Option autAgentPortOption = 
            new Option(OPT_AUT_AGENT_PORT, true,
                "AUT Agent Port between 1024 and 65536"
                    + " (default \"" + DEFAULT_AUT_AGENT_PORT + "\")");
        autAgentPortOption.setLongOpt(OPT_AUT_AGENT_PORT_LONG);
        autAgentPortOption.setArgName("port");
        options.addOption(autAgentPortOption);

        OptionGroup autToolkitOptionGroup = new OptionGroup();
        autToolkitOptionGroup.addOption(
                new Option(TK_SWING, "AUT uses Swing toolkit"));
        autToolkitOptionGroup.addOption(
                new Option(TK_SWT,   "AUT uses SWT toolkit"));
        autToolkitOptionGroup.addOption(
                new Option(TK_RCP,   "AUT uses RCP toolkit"));
        autToolkitOptionGroup.setRequired(true);
        options.addOptionGroup(autToolkitOptionGroup);
        
        Option autIdOption =
            new Option(OPT_AUT_ID, true, "AUT ID");
        autIdOption.setLongOpt(OPT_AUT_ID_LONG);
        autIdOption.setArgName("id");
        autIdOption.setRequired(true);
        options.addOption(autIdOption);
        
        Option nameTechnicalComponentsOption =
            new Option(OPT_NAME_TECHNICAL_COMPONENTS, 
                true, "Generate Names for Technical Components (true / false)");
        nameTechnicalComponentsOption
            .setLongOpt(OPT_NAME_TECHNICAL_COMPONENTS_LONG);
        nameTechnicalComponentsOption.setArgName("true / false");
        options.addOption(nameTechnicalComponentsOption);
        
        Option keyboardLayoutOption =
            new Option(OPT_KEYBOARD_LAYOUT, 
                true, "Keyboard Layout");
        keyboardLayoutOption
            .setLongOpt(OPT_KEYBOARD_LAYOUT_LONG);
        keyboardLayoutOption.setArgName("locale");
        options.addOption(keyboardLayoutOption);
        
        Option workingDirOption = new Option(OPT_WORKING_DIR, 
                true, "AUT Working Directory");
        workingDirOption.setLongOpt(OPT_WORKING_DIR_LONG);
        workingDirOption.setArgName("directory");
        options.addOption(workingDirOption);
        
        Option helpOption = new Option(OPT_HELP, 
                false, "Displays this help");
        helpOption.setLongOpt(OPT_HELP_LONG);
        options.addOption(helpOption);
        
        OptionBuilder.hasArgs();
        Option autExecutableFileOption = OptionBuilder.create(OPT_EXECUTABLE);
        autExecutableFileOption.setDescription("AUT Executable File");
        autExecutableFileOption.setLongOpt(OPT_EXECUTABLE_LONG);
        autExecutableFileOption.setRequired(true);
        autExecutableFileOption.setArgName("command");
        options.addOption(autExecutableFileOption);
        
        return options;
    }
    
    /**
     * This class implements the <code>Comparator</code> interface for comparing
     * Options.
     */
    private static class OptionComparator implements Comparator {
        /** {@inheritDoc} */
        public int compare(Object o1, Object o2) {
            Option opt1 = (Option)o1;
            Option opt2 = (Option)o2;
            // always list -exec as last option
            if (opt1.getOpt().equals(OPT_EXECUTABLE)) {
                return 1;
            }
            if (opt2.getOpt().equals(OPT_EXECUTABLE)) {
                return -1;
            }
            return 0;
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(
                IApplicationContext.APPLICATION_ARGS);
        if (args == null) {
            args = new String[0];
        }

        Options options = createCmdLineOptions();
        Parser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args, false);
        } catch (ParseException pe) {
            printHelp(pe);
        }
        
        if (cmdLine != null && !cmdLine.hasOption(OPT_HELP)) {
            String toolkit = StringConstants.EMPTY;
            if (cmdLine.hasOption(TK_SWING)) {
                toolkit = "Swing"; //$NON-NLS-1$
            } else if (cmdLine.hasOption(TK_SWT)) {
                toolkit = "Swt"; //$NON-NLS-1$
            } else if (cmdLine.hasOption(TK_RCP)) {
                toolkit = "Rcp"; //$NON-NLS-1$
            }
            
            int autAgentPort = DEFAULT_AUT_AGENT_PORT;
            if (cmdLine.hasOption(OPT_AUT_AGENT_PORT)) {
                try {
                    autAgentPort = Integer.parseInt(cmdLine
                            .getOptionValue(OPT_AUT_AGENT_PORT));
                } catch (NumberFormatException nfe) {
                    // use default
                }
            }
            String autAgentHost = DEFAULT_AUT_AGENT_HOST;
            if (cmdLine.hasOption(OPT_AUT_AGENT_HOST)) {
                autAgentHost = cmdLine.getOptionValue(OPT_AUT_AGENT_HOST);
            }
            
            InetSocketAddress agentAddr = 
                new InetSocketAddress(autAgentHost, autAgentPort);
            AutIdentifier autId = new AutIdentifier(cmdLine
                    .getOptionValue(OPT_AUT_ID));
            
            Map<String, Object> autConfiguration = createAutConfig(cmdLine);
            
            AutRunner runner = new AutRunner(
                    toolkit, autId, agentAddr, autConfiguration);
            try {
                runner.run();
            } catch (ConnectException ce) {
                LOG.info("Could not connect to AUT Agent.", ce); //$NON-NLS-1$
                System.err.println(I18n.getString("InfoDetail.connGuiDancerServerFailed")); //$NON-NLS-1$
            }
        } else {
            printHelp(null);
        }

        return IApplication.EXIT_OK;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void stop() {
        // no-op
    }
}