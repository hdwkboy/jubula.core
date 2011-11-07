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
package org.eclipse.jubula.client.ui.rcp.businessprocess;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jubula.client.core.businessprocess.compcheck.CompletenessGuard;
import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
import org.eclipse.jubula.client.core.events.DataEventDispatcher;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.tools.constants.DebugConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author BREDEX GmbH
 * @created 12.03.2007
 */
public class CompletenessBP implements IProjectLoadedListener,
    ILanguageChangedListener {
    /** for log messages */
    private static Logger log = LoggerFactory.getLogger(CompletenessBP.class);
    
    /** this instance */
    private static CompletenessBP instance; 

    /**
     * private constructor
     */
    private CompletenessBP() {
        DataEventDispatcher ded = DataEventDispatcher.getInstance();
        ded.addLanguageChangedListener(this, false);
        ded.addProjectLoadedListener(this, false);
        ICommandService commandService = (ICommandService) PlatformUI
                .getWorkbench().getService(ICommandService.class);

        IExecutionListener saveListener = new IExecutionListener() {
            /** {@inheritDoc} */
            public void preExecute(String commandId, ExecutionEvent event) {
                // empty is ok
            }
            /** {@inheritDoc} */
            public void postExecuteSuccess(String commandId, 
                    Object returnValue) {
                completeProjectCheck();
            }
            /** {@inheritDoc} */
            public void postExecuteFailure(String commandId,
                    ExecutionException exception) {
                completeProjectCheck();
            }
            /** {@inheritDoc} */
            public void notHandled(String commandId, 
                NotHandledException exception) {
                // empty is ok
            }
        };
        commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE)
                .addExecutionListener(saveListener);
        commandService.getCommand(IWorkbenchCommandConstants.FILE_SAVE_ALL)
            .addExecutionListener(saveListener);
    }

    /**
     * @return the ComponentNamesList
     */
    public static CompletenessBP getInstance() {
        if (instance == null) {
            instance = new CompletenessBP();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public void handleProjectLoaded() {
        /* At this time he data is not completely loaded from the database.
         * Since Persistence (JPA / EclipseLink) is not threadsafe the checks (which will load
         * all data) can't be run in a job, so checkProject must be called
         * with runInJob==false.
         */
        completeProjectCheck(); 
    }

    /**
     * checks the project regardless of user preferences
     */
    public void completeProjectCheck() {
        final INodePO root = GeneralStorage.getInstance().getProject();
        if (root != null) {
            Plugin.startLongRunning(Messages
                    .CompletenessCheckRunningOperation);
            try {
                PlatformUI.getWorkbench().getProgressService().run(true, false,
                        new CompletnessCheckOperation());
            } catch (InvocationTargetException e) {
                log.error(DebugConstants.ERROR, e);
            } catch (InterruptedException e) {
                log.error(DebugConstants.ERROR, e);
            } finally {
                Plugin.stopLongRunning();
            }
        }
    }
    
    /**
     * @author Markus Tiede
     * @created 07.11.2011
     */
    public static class CompletnessCheckOperation implements
            IRunnableWithProgress {

        /** {@inheritDoc} */
        public void run(IProgressMonitor monitor) {

            monitor.beginTask(Messages.CompletenessCheckRunningOperation,
                    IProgressMonitor.UNKNOWN);

            ProgressMonitorTracker.getInstance().setProgressMonitor(monitor);

            try {
                final INodePO root = GeneralStorage.getInstance().getProject();
                final Locale wl = WorkingLanguageBP.getInstance()
                        .getWorkingLanguage();
                CompletenessGuard.checkAll(wl, root);
            } finally {
                ProgressMonitorTracker.getInstance().setProgressMonitor(null);
                fireCompletenessCheckFinished();
                monitor.done();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void handleLanguageChanged(Locale locale) {
        INodePO root = GeneralStorage.getInstance().getProject();
        Locale wl = WorkingLanguageBP.getInstance().getWorkingLanguage();
        CompletenessGuard.checkTD(wl, root);
        fireCompletenessCheckFinished();
    }
    
    /**
     * Notifies that the check is finished.
     */
    private static void fireCompletenessCheckFinished() {
        DataEventDispatcher.getInstance().fireCompletenessCheckFinished();
    }
}