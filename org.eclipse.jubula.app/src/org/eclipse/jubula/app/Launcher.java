/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jubula.app.core.JubulaWorkbenchAdvisor;
import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.ChooseWorkspaceDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the (standalone) launcher class - most of the content belongs to
 * <code>org.eclipse.ui.internal.ide.application.IDEApplication</code>
 * 
 * @author BREDEX GmbH
 * @created 24.03.2005
 */
public class Launcher implements IApplication {
    /** The name of the folder containing metadata information for the workspace. */
    public static final String METADATA_FOLDER = ".metadata"; //$NON-NLS-1$
    /** the version filename */
    private static final String VERSION_FILENAME = "version.ini"; //$NON-NLS-1$
    /** the workspace version key */
    private static final String WORKSPACE_VERSION_KEY = "org.eclipse.core.runtime"; //$NON-NLS-1$
    /** the workspace version value */
    private static final String WORKSPACE_VERSION_VALUE = "1"; //$NON-NLS-1$
    /** a proper exit code */
    private static final String PROP_EXIT_CODE = "eclipse.exitcode"; //$NON-NLS-1$

    /**
     * A special return code that will be recognized by the launcher and used to
     * restart the workbench.
     */
    private static final Integer EXIT_RELAUNCH = new Integer(24);

    /** for log messages */
    private static Logger log = LoggerFactory.getLogger(Launcher.class);

    /**
     * Creates a new Application.
     */
    public Launcher() {
        // do nothing
    }


    /**
     * Open a workspace selection dialog on the argument shell, populating the
     * argument data with the user's selection. Perform first level validation
     * on the selection by comparing the version information. This method does
     * not examine the runtime state (e.g., is the workspace already locked?).
     * 
     * @param shell the shell
     * @param launchData the launch data
     * @param force
     *            setting to true makes the dialog open regardless of the
     *            showDialog value
     * @return An URL storing the selected workspace or null if the user has
     *         canceled the launch operation.
     */
    private URL promptForWorkspace(Shell shell, ChooseWorkspaceData launchData,
            boolean force) {
        boolean doForce = force;
        URL url = null;
        do {
            // okay to use the shell now - this is the splash shell
            new ChooseWorkspaceDialog(shell, launchData, false, true)
                    .prompt(doForce);
            String instancePath = launchData.getSelection();
            if (instancePath == null) {
                return null;
            }

            // the dialog is not forced on the first iteration, but is on every
            // subsequent one -- if there was an error then the user needs to be
            // allowed to fix it
            doForce = true;

            // 70576: don't accept empty input
            if (instancePath.length() <= 0) {
                MessageDialog.openError(shell,
                    IDEWorkbenchMessages.IDEApplication_workspaceEmptyTitle,
                    IDEWorkbenchMessages.IDEApplication_workspaceEmptyMessage);
                continue;
            }

            // create the workspace if it does not already exist
            File workspace = new File(instancePath);
            if (!workspace.exists()) {
                workspace.mkdir();
            }

            try {
                // Don't use File.toURL() since it adds a leading slash that Platform does not
                // handle properly.  See bug 54081 for more details.  
                String path = workspace.getAbsolutePath().replace(
                        File.separatorChar, '/');
                url = new URL("file", null, path); //$NON-NLS-1$
            } catch (MalformedURLException e) {
                MessageDialog.openError(shell,
                    IDEWorkbenchMessages.IDEApplication_workspaceInvalidTitle,
                    IDEWorkbenchMessages
                        .IDEApplication_workspaceInvalidMessage);
                continue;
            }
        } while (!checkValidWorkspace(shell, url));

        return url;
    }

    /**
     * The version file is stored in the metadata area of the workspace. This
     * method returns an URL to the file or null if the directory or file does
     * not exist (and the create parameter is false).
     * @param create If the directory and file does not exist this parameter controls whether it will be created.
     * @param workspaceUrl The URL of the workspace.
     * @return An url to the file or null if the version file does not exist or could not be created.
     */
    private static File getVersionFile(URL workspaceUrl, boolean create) {
        if (workspaceUrl == null) {
            return null;
        }
        try {
            // make sure the directory exists
            URL metaUrl = new URL(workspaceUrl, METADATA_FOLDER);
            File metaDir = new File(metaUrl.getFile());
            if (!metaDir.exists() && (!create || !metaDir.mkdir())) {
                return null;
            }
            // make sure the file exists
            URL versionUrl = new URL(metaDir.toURL(), VERSION_FILENAME);
            File versionFile = new File(versionUrl.getFile());
            if (!versionFile.exists() 
                && (!create || !versionFile.createNewFile())) {
                return null;
            }
            return versionFile;
        } catch (IOException e) {
            // cannot log because instance area has not been set
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setInitializationData(IConfigurationElement config, 
        String propertyName, Object data) {
        // do nothing 
    }


    /**
     * Creates the display used by the application.
     * 
     * @return the display used by the application
     */
    protected Display createDisplay() {
        return PlatformUI.createDisplay();
    }
    
    /**
     * {@inheritDoc}
     */
    public Object start(IApplicationContext context) throws Exception {
        Display.setAppName(getAppName());
        Display display = createDisplay();

        try {

            // look and see if there's a splash shell we can parent off of
            Shell shell = WorkbenchPlugin.getSplashShell(display);
            if (shell != null) {
                // should should set the icon and message for this shell to be the 
                // same as the chooser dialog - this will be the guy that lives in
                // the task bar and without these calls you'd have the default icon 
                // with no message.
                shell.setText(ChooseWorkspaceDialog.getWindowTitle());
                shell.setImages(Window.getDefaultImages());
            }
           
            Object instanceLocationCheck = checkInstanceLocation(shell);
            if (instanceLocationCheck != null) {
                WorkbenchPlugin.unsetSplashShell(display);
                Platform.endSplash();
                return instanceLocationCheck;
            }

            Platform.addLogListener(new ILogListener() {
                public void logging(IStatus status, String pluginId) {
                    if (status.getException() instanceof RuntimeException) {
                        Plugin.getDefault().handleError(status.getException());
                    }
                }
            });
            
            // create the workbench with this advisor and run it until it exits
            // N.B. createWorkbench remembers the advisor, and also registers
            // the workbench globally so that all UI plug-ins can find it using
            // PlatformUI.getWorkbench() or AbstractUIPlugin.getWorkbench()
            int returnCode = PlatformUI.createAndRunWorkbench(display,
                    getWorkbenchAdvisor());

            // the workbench doesn't support relaunch yet (bug 61809) so
            // for now restart is used, and exit data properties are checked
            // here to substitute in the relaunch return code if needed
            if (returnCode != PlatformUI.RETURN_RESTART) {
                return EXIT_OK;
            }

            // if the exit code property has been set to the relaunch code, then
            // return that code now, otherwise this is a normal restart
            return EXIT_RELAUNCH.equals(
                    Integer.getInteger(PROP_EXIT_CODE)) ? EXIT_RELAUNCH
                            : EXIT_RESTART;
        } finally {
            if (display != null) {
                display.dispose();
            }
            Location instanceLoc = Platform.getInstanceLocation();
            if (instanceLoc != null) {
                instanceLoc.release();
            }
        }
    }

    /**
     * Look at the argument URL for the workspace's version information. Return
     * that version if found and null otherwise.
     * @param workspace the workspace
     * @return the workspace version
     */
    private static String readWorkspaceVersion(URL workspace) {
        File versionFile = getVersionFile(workspace, false);
        if (versionFile == null || !versionFile.exists()) {
            return null;
        }

        try {
            // Although the version file is not spec'ed to be a Java properties
            // file, it happens to follow the same format currently, so using
            // Properties to read it is convenient.
            Properties props = new Properties();
            FileInputStream is = new FileInputStream(versionFile);
            try {
                props.load(is);
            } finally {
                is.close();
            }

            return props.getProperty(WORKSPACE_VERSION_KEY);
        } catch (IOException e) {
            IDEWorkbenchPlugin.log("Could not read version file", new Status(//$NON-NLS-1$
                    IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                    IStatus.ERROR,
                    e.getMessage() == null ? "" : e.getMessage(), //$NON-NLS-1$, 
                    e));
            return null;
        }
    }

    /**
     * Return true if the argument directory is ok to use as a workspace and
     * false otherwise. A version check will be performed, and a confirmation
     * box may be displayed on the argument shell if an older version is
     * detected.
     * 
     * @param shell the shell to use
     * @param url the url to check
     * @return true if the argument URL is ok to use as a workspace and false
     *         otherwise.
     */
    private boolean checkValidWorkspace(Shell shell, URL url) {
        // a null url is not a valid workspace
        if (url == null) {
            return false;
        }

        String version = readWorkspaceVersion(url);

        // if the version could not be read, then there is not any existing
        // workspace data to trample, e.g., perhaps its a new directory that
        // is just starting to be used as a workspace
        if (version == null) {
            return true;
        }

        final int ideVersion = Integer.parseInt(WORKSPACE_VERSION_VALUE);
        int workspaceVersion = Integer.parseInt(version);

        // equality test is required since any version difference (newer
        // or older) may result in data being trampled
        if (workspaceVersion == ideVersion) {
            return true;
        }

        // At this point workspace has been detected to be from a version
        // other than the current ide version -- find out if the user wants
        // to use it anyhow.
        String title = IDEWorkbenchMessages.IDEApplication_versionTitle;
        String message = NLS.bind(
                IDEWorkbenchMessages.IDEApplication_versionMessage, 
                url.getFile());

        MessageBox mbox = new MessageBox(shell, SWT.OK | SWT.CANCEL
                | SWT.ICON_WARNING | SWT.APPLICATION_MODAL);
        mbox.setText(title);
        mbox.setMessage(message);
        return mbox.open() == SWT.OK;
    }
    
    /**
     * Write the version of the metadata into a known file overwriting any
     * existing file contents. Writing the version file isn't really crucial,
     * so the function is silent about failure
     */
    private static void writeWorkspaceVersion() {
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null || instanceLoc.isReadOnly()) {
            return;
        }

        File versionFile = getVersionFile(instanceLoc.getURL(), true);
        if (versionFile == null) {
            return;
        }

        OutputStream output = null;
        try {
            String versionLine = WORKSPACE_VERSION_KEY + '='
                    + WORKSPACE_VERSION_VALUE;

            output = new FileOutputStream(versionFile);
            output.write(versionLine.getBytes("UTF-8")); //$NON-NLS-1$
        } catch (IOException e) {
            IDEWorkbenchPlugin.log("Could not write version file", //$NON-NLS-1$
                    StatusUtil.newStatus(IStatus.ERROR, e.getMessage(), e));
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }
    
    /**
     * Return <code>null</code> if a valid workspace path has been set and an exit code otherwise.
     * Prompt for and set the path if possible and required.
     * 
     * @param shell the shell to use
     * @return <code>null</code> if a valid instance location has been set and an exit code
     *         otherwise
     */
    private Object checkInstanceLocation(Shell shell) {
        // -data @none was specified but an ide requires workspace
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null) {
            MessageDialog.openError(shell,
                IDEWorkbenchMessages.IDEApplication_workspaceMandatoryTitle,
                IDEWorkbenchMessages.IDEApplication_workspaceMandatoryMessage);
            return EXIT_OK;
        }
        // -data "/valid/path", workspace already set
        if (instanceLoc.isSet()) {
            // make sure the meta data version is compatible (or the user has
            // chosen to overwrite it).
            if (!checkValidWorkspace(shell, instanceLoc.getURL())) {
                return EXIT_OK;
            }
            // at this point its valid, so try to lock it and update the
            // metadata version information if successful
            try {
                if (instanceLoc.lock()) {
                    writeWorkspaceVersion();
                    return null;
                }
                // we failed to create the directory - two possibilities:
                // 1. directory is already in use
                // 2. directory could not be created
                File workspaceDirectory = 
                        new File(instanceLoc.getURL().getFile());
                handleError(shell, workspaceDirectory);
            } catch (IOException e) {
                IDEWorkbenchPlugin.log("Could not obtain lock for workspace location", //$NON-NLS-1$
                        e);             
                MessageDialog.openError(shell,
                        IDEWorkbenchMessages.InternalError,
                        e.getMessage());                
            }            
            return EXIT_OK;
        }
        // -data @noDefault or -data not specified, prompt and set
        ChooseWorkspaceData launchData = new ChooseWorkspaceData(instanceLoc
                .getDefault());
        boolean force = false;
        while (true) {
            URL workspaceUrl = promptForWorkspace(shell, launchData, force);
            if (workspaceUrl == null) {
                return EXIT_OK;
            }
            // if there is an error with the first selection, then force the
            // dialog to open to give the user a chance to correct
            force = true;
            try {
                // the operation will fail if the url is not a valid
                // instance data area, so other checking is unneeded
                if (instanceLoc.setURL(workspaceUrl, true)) {
                    launchData.writePersistedData();
                    writeWorkspaceVersion();
                    return null;
                }
            } catch (IllegalStateException e) {
                MessageDialog.openError(shell,
                    IDEWorkbenchMessages
                        .IDEApplication_workspaceCannotBeSetTitle,
                    IDEWorkbenchMessages
                        .IDEApplication_workspaceCannotBeSetMessage);
                return EXIT_OK;
            }
            // by this point it has been determined that the workspace is
            // already in use -- force the user to choose again
            MessageDialog.openError(shell, IDEWorkbenchMessages
                        .IDEApplication_workspaceInUseTitle, 
                    IDEWorkbenchMessages.IDEApplication_workspaceInUseMessage);
        }
    }


    /**
     * @param shell the shell to use
     * @param workspaceDirectory the workspace dir
     */
    protected void handleError(Shell shell, File workspaceDirectory) {
        if (workspaceDirectory.exists()) {
            MessageDialog.openError(shell,
                IDEWorkbenchMessages
                    .IDEApplication_workspaceCannotLockTitle,
                IDEWorkbenchMessages
                    .IDEApplication_workspaceCannotLockMessage);
        } else {
            MessageDialog.openError(shell, 
                IDEWorkbenchMessages
                    .IDEApplication_workspaceCannotBeSetTitle,
                IDEWorkbenchMessages
                    .IDEApplication_workspaceCannotBeSetMessage);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void stop() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return;
        }
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }
    
    /**
     * @return a new workbench advisor instance
     */
    protected WorkbenchAdvisor getWorkbenchAdvisor() {
        return new JubulaWorkbenchAdvisor();
    }


    /**
     * @return an human readable application name
     */
    protected String getAppName() {
        return "Jubula"; //$NON-NLS-1$
    }
}