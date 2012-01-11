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
package org.eclipse.jubula.client.core.events;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jubula.client.core.businessprocess.TestExecutionEvent;
import org.eclipse.jubula.client.core.errorhandling.ErrorMessagePresenter;
import org.eclipse.jubula.client.core.i18n.Messages;
import org.eclipse.jubula.client.core.model.IPersistentObject;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.persistence.GeneralStorage.IDataModifiedListener;
import org.eclipse.jubula.client.core.persistence.GeneralStorage.IReloadedSessionListener;
import org.eclipse.jubula.client.core.utils.IGenericListener;
import org.eclipse.jubula.client.core.utils.ListenerManager;
import org.eclipse.jubula.tools.messagehandling.MessageIDs;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * class to manage events caused of data modifications
 * @author BREDEX GmbH
 * @created 10.11.2005
 */
@SuppressWarnings("synthetic-access")
public class DataEventDispatcher implements IReloadedSessionListener, 
    IDataModifiedListener {
    
    /**
     * Operation for performing the non-cancelable actions that come at the
     * end of opening a project.
     *
     * @author BREDEX GmbH
     * @created Jan 9, 2008
     */
    public class LoadProjectDataOperation implements IRunnableWithProgress {

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) {
            
            // model updates
            final Set<IProjectLoadedListener> stableListeners = 
                new HashSet<IProjectLoadedListener>(
                    m_projectLoadedListeners);
            for (IProjectLoadedListener l : stableListeners) {
                if (monitor.isCanceled()) {
                    return;
                }
                try {
                    l.handleProjectLoaded();
                } catch (OperationCanceledException oce) {
                    // Operation was canceled. Do nothing.
                } catch (NestableRuntimeException nre) {
                    Throwable rootCause = 
                        nre.getThrowable(nre.getThrowableCount() - 1);
                    if (!(rootCause instanceof OperationCanceledException)) {
                        // Only need to log an error if the root cause
                        // is not that the operation was canceled.
                        LOG.error(Messages.UnhandledExceptionWhileCallListeners,
                                nre);
                    }
                } catch (Throwable t) {
                    LOG.error(
                            Messages.UnhandledExceptionWhileCallListeners, 
                            t);
                }
            }        

            // gui updates
            final Set<IProjectLoadedListener> stableListenersPost = 
                new HashSet<IProjectLoadedListener>(
                    m_projectLoadedListenersPost);
            for (IProjectLoadedListener l : stableListenersPost) {
                if (monitor.isCanceled()) {
                    return;
                }
                try {
                    l.handleProjectLoaded();
                } catch (OperationCanceledException oce) {
                    // Operation was canceled. Do nothing.
                } catch (NestableRuntimeException nre) {
                    Throwable rootCause = 
                        nre.getThrowable(nre.getThrowableCount() - 1);
                    if (!(rootCause instanceof OperationCanceledException)) {
                        // Only need to log an error if the root cause
                        // is not that the operation was canceled.
                        LOG.error(
                                Messages.UnhandledExceptionWhileCallListeners, 
                                nre);
                    }
                } catch (Throwable t) {
                    LOG.error(
                            Messages.UnhandledExceptionWhileCallListeners, t);
                }
            }
            
        }

    }

    /** standard logging */
    private static final Logger LOG = 
        LoggerFactory.getLogger(DataEventDispatcher.class);
    
    /**
     * clients interested in partial changes to the data in the master session
     *
     */
    public interface IDataChangedListener {
        /**
         * @param events an array of data changed events
         */
        public void handleDataChanged(DataChangedEvent... events);
    }
    
    /**
     * to notify the DatasetView about modifications in Properties View
     *
     */
    public interface IParamChangedListener {
        
        /**
         * callback method
         */
        public void handleParamChanged();
    }
    
    /**
     * to notify the PropertiesView about part closing
     *
     */
    public interface IPartClosedListener {
        
        /**
         * callback method
         * @param part the part that was closed.
         */
        public void handlePartClosed(IWorkbenchPart part);
    }
    
    /**
     * to notify the Editor when properties changed in propView or dataSetView
     */
    public interface IPropertyChangedListener {
        
        /**
         * callback method
         * @param isCompNameChanged flag to set true, if compNameChanged
         */
        public void handlePropertyChanged(boolean isCompNameChanged);
    }
    
    /**
     * to notify if the working language has changed.
     */
    public interface ILanguageChangedListener {
        /**
         * @param locale the new Locale
         */
        public void handleLanguageChanged(Locale locale);
    }
    
    /**
     * 
     * to notify clients, that a project is loaded
     */
    public interface IProjectLoadedListener {
        
        /**
         * callback method
         */
        public void handleProjectLoaded();
    }
    
    /**
     * 
     * to notify clients, that a project is created (new project, import project,
     * create new version, save project as)
     */
    public interface IProjectCreatedListener {
        
        /**
         * callback method
         */
        public void handleProjectCreated();
    }
    
    /**
     * to notify clients, that the project properties are modified
     */
    public interface IProjectPropertiesModifyListener {
        /**
         * callback method
         */
        public void handleProjectPropsChanged();
        
    }
    
    /**
     * 
     * to notify clients, that the connection to server is established
     */
    public interface IServerConnectionListener {
        
        /**
         * @param state connection state from server
         */
        public void handleServerConnStateChanged(ServerState state);
    }
    
    /** to notify the clients when the completeness check is finished */
    public interface ICompletenessCheckListener {
        
        /**
         * Will be called when the completeness check is finished
         */
        public void completenessCheckFinished();
    }
    
   /** to notify clients about changes of AUT state */
    public interface IAutStateListener {
        /**
         * @param state state from AUT
         */
        public void handleAutStateChanged(AutState state);
    }
    
    /** to notify clients about changes of Testsuite state */
    public interface ITestSuiteStateListener {
        
        /**
         * @param event occured event
         */
        public void handleTSStateChanged(TestExecutionEvent event);
    }
    
    /** to notify clients about modification of OM-Mode */
    public interface IOMStateListener {
        
        /**
         * @param state current state of OM Mode
         */
        public void handleOMStateChanged(OMState state);
    }
    
    /** to notify clients about modification of Record Mode */
    public interface IRecordModeStateListener {
        
        /**
         * @param state current state of Record mode
         */
        public void handleRecordModeStateChanged(RecordModeState state);
    }
    
    /** to notify clients about modification of server preferences */
    public interface IServerPrefListener {
        /**
         * callback method
         */
        public void handlePrefServerChanged();
    }
    
    /** to notify dialogs about button status */
    public interface IDialogStatusListener extends IGenericListener {
        // do nothing
    }
    
    /** to notify clients about changes of testresults */
    public interface ITestresultChangedListener {
        /**
         * handle testresult changes
         * @param state TestresultState (clear or refresh) 
         */
        public void handleTestresultChanged(TestresultState state);
    }
    /**to notify that project is opened */
    public interface IProjectOpenedListener {
        /**
         * 
         */
        public void handleProjectOpened();
        
    }
    
    /**
     * <code>m_instance</code> singleton
     */
    private static DataEventDispatcher instance = null;
    
    /** Specifies the result of a db version validation */
    public enum DataState {
        /** object was modified in master session */
         Renamed,
        /** object was added to master session */
         Added ,
        /** object was deleted in master session */
         Deleted,
        /** structure of object was modified, including their data*/
         StructureModified,
        /** current object has modification regarding to reuse */
         ReuseChanged
    }
     
    /** specifies the connection state between client and server */
    public enum ServerState {
        /** client is connected with server */
        Connected,
        /** client is disconnected from server */
        Disconnected,
        /** connection is beeing established */
        Connecting
    }
    
    /** specifies the current action for test result summary view */
    public enum TestresultState {
        /** view should be cleared */
        Clear, 
        /** view should be refreshed */
        Refresh
    }
    
    /** specifies the current state of AUT */
    public enum AutState {
        /** AUT is started */
        running, 
        /** AUT is not running (stopped / aborted) */
        notRunning
    }
    
    /** specifies the parts to update */
    public enum UpdateState {
        /** update only relevant for editor(s) */
        onlyInEditor,
        /** update not relevant for editor */
        notInEditor,
        /** update for all editors/views relevant */
        all
    }
    
    /** specifies the state of OM-Mode */
    public enum OMState {
        /** Object Mapping Mode is running */
        running, 
        /** Object Mapping Mode is not running */
        notRunning
    }
    
    /** specifies the state of Record-Mode */
    public enum RecordModeState {
        /** Record Mode is running */
        running, 
        /** Record Mode is not running */
        notRunning
    }
    
    /** listeners to observe button status of a dialog or wizard page */
    private ListenerManager<IDialogStatusListener> m_dialogStatusListenerMgr = 
        new ListenerManager<IDialogStatusListener>();
    
    /**
     * <code>m_dataChangedListeners</code> listener for partial changes to the data in the master session 
     */
    private Set<IDataChangedListener> m_dataChangedListeners = 
        new HashSet<IDataChangedListener>();
    /**
     * <code>m_dataChangedListeners</code> listener for partial changes to the data in the master session 
     *  POST-Event for gui updates 
     */
    private Set<IDataChangedListener> m_dataChangedListenersPost = 
        new HashSet<IDataChangedListener>();
    
    /**
     * listener of language changed events.
     */
    private Set<ILanguageChangedListener> m_langChangedListeners = 
        new HashSet<ILanguageChangedListener>();
    /**
     * listener of language changed events.
     *  POST-Event for gui updates 
     */
    private Set<ILanguageChangedListener> m_langChangedListenersPost = 
        new HashSet<ILanguageChangedListener>();
    
    /**
     * <code>m_partClosedListeners</code> listener for editor close events
     */
    private Set<IPartClosedListener> m_partClosedListeners = 
        new HashSet<IPartClosedListener>();
    /**
     * <code>m_partClosedListeners</code> listener for editor close events
     *  POST-Event for gui updates 
     */
    private Set<IPartClosedListener> m_partClosedListenersPost = 
        new HashSet<IPartClosedListener>();
    
    /**
     * <code>m_partClosedListeners</code> listener for editor close events
     */
    private Set<IPropertyChangedListener> m_propertyChangedListeners = 
        new HashSet<IPropertyChangedListener>();
    /**
     * <code>m_partClosedListeners</code> listener for editor close events
     *  POST-Event for gui updates 
     */
    private Set<IPropertyChangedListener> m_propertyChangedListenersPost = 
        new HashSet<IPropertyChangedListener>();
    
    /**
     * <code>m_paramChangedListeners</code> listener for changes to the parameter
     */
    private Set<IParamChangedListener> m_paramChangedListeners = 
        new HashSet<IParamChangedListener>();
    /**
     * <code>m_paramChangedListeners</code> listener for changes to the parameter
     *  POST-Event for gui updates 
     */
    private Set<IParamChangedListener> m_paramChangedListenersPost = 
        new HashSet<IParamChangedListener>();
    
    /**
     * <code>m_projectLoadedListeners</code> listener for load of a project
     */
    private Set<IProjectLoadedListener> m_projectLoadedListeners = 
        new HashSet<IProjectLoadedListener>();
    /**
     * <code>m_projectLoadedListeners</code> listener for load of a project
     *  POST-Event for gui updates 
     */
    private Set<IProjectLoadedListener> m_projectLoadedListenersPost = 
        new HashSet<IProjectLoadedListener>();
    
    /**
     * <code>m_projectCreatedListeners</code>listener for creation of a project 
     * (not available in database before)
     */
    private Set<IProjectCreatedListener> m_projectCreatedListeners = 
        new HashSet<IProjectCreatedListener>();
    
    /**
     * <code>m_projectCreatedListenersPost</code> listener for a new project
     *  POST-Event for gui updates 
     */
    private Set<IProjectCreatedListener> m_projectCreatedListenersPost = 
        new HashSet<IProjectCreatedListener>();
    
    
    /**
     * <code>m_projectPropertiesModifyListeners</code> listener for modification 
     *       of project properties
     */
    private Set<IProjectPropertiesModifyListener> 
    m_projectPropertiesModifyListeners = 
            new HashSet<IProjectPropertiesModifyListener>();
    /**
     * <code>m_projectPropertiesModifyListeners</code> listener for modification 
     *       of project properties
     *  POST-Event for gui updates 
     */
    private Set<IProjectPropertiesModifyListener> 
    m_projectPropertiesModifyListenersPost = 
            new HashSet<IProjectPropertiesModifyListener>();

    
    /**
     * <code>m_serverConnectionListeners</code>listener for state of server connection
     */
    private Set<IServerConnectionListener> m_serverConnectionListeners =
        new HashSet<IServerConnectionListener>();
    /**
     * <code>m_serverConnectionListeners</code>listener for state of server connection
     *  POST-Event for gui updates 
     */
    private Set<IServerConnectionListener> m_serverConnectionListenersPost =
        new HashSet<IServerConnectionListener>();

    /**
     * listeners for state of AUT Server connection
     */
    private Set<IServerConnectionListener> m_autServerConnectionListeners =
        new HashSet<IServerConnectionListener>();
    /**
     * listeners for state of AUT Server connection
     *  POST-Event for gui updates 
     */
    private Set<IServerConnectionListener> m_autServerConnectionListenersPost =
        new HashSet<IServerConnectionListener>();
    
    /**
     * <code>m_autStateListeners</code>listener for state of AUT
     */
    private Set<IAutStateListener> m_autStateListeners =
        new HashSet<IAutStateListener>();
    /**
     * <code>m_autStateListeners</code>listener for state of AUT
     *  POST-Event for gui updates 
     */
    private Set<IAutStateListener> m_autStateListenersPost =
        new HashSet<IAutStateListener>();
    
    /**
     * <code>ITestresultChangedListener</code>listener for changes of
     *  test result
     */
    private Set<ITestresultChangedListener>
    m_testresultListeners =
        new HashSet<ITestresultChangedListener>();
    /**
     * <code>ITestresultChangedListener</code>listener for changes of
     * test result POST-Event for gui updates 
     */
    private Set<ITestresultChangedListener>
    m_testresultListenersPost =
        new HashSet<ITestresultChangedListener>();
    
    /**
     * <code>m_testSuiteStateListeners</code> listener for state modification of testsuites
     */
    private Set<ITestSuiteStateListener> m_testSuiteStateListeners =
        new HashSet<ITestSuiteStateListener>();
    /**
     * <code>m_testSuiteStateListeners</code> listener for state modification of testsuites
     *  POST-Event for gui updates 
     */
    private Set<ITestSuiteStateListener> m_testSuiteStateListenersPost =
        new HashSet<ITestSuiteStateListener>();
    
    /**
     * <code>m_omStateListeners</code> listener for state of OM Mode
     */
    private Set<IOMStateListener> m_omStateListeners =
        new HashSet<IOMStateListener>();
    /**
     * <code>m_omStateListeners</code> listener for state of OM Mode
     *  POST-Event for gui updates 
     */
    private Set<IOMStateListener> m_omStateListenersPost =
        new HashSet<IOMStateListener>();
    
    /**
     * <code>m_recordModeStateListeners</code> listener for state of Record Mode
     */
    private Set<IRecordModeStateListener> m_recordModeStateListeners =
        new HashSet<IRecordModeStateListener>();
    /**
     * <code>m_recordModeStateListeners</code> listener for state of Record Mode
     *  POST-Event for gui updates 
     */
    private Set<IRecordModeStateListener> m_recordModeStateListenersPost =
        new HashSet<IRecordModeStateListener>();

    /**
     * <code>m_serverPrefListeners</code>listener for modification of server preferences
     */
    private Set<IServerPrefListener> m_serverPrefListeners =
        new HashSet<IServerPrefListener>();
    /**
     * <code>m_serverPrefListeners</code>listener for modification of server preferences
     *  POST-Event for gui updates 
     */
    private Set<IServerPrefListener> m_serverPrefListenersPost =
        new HashSet<IServerPrefListener>();
    
    /** The ICompletenessCheckListener Listeners */
    private Set<ICompletenessCheckListener> m_completenessCheckListeners = 
        new HashSet<ICompletenessCheckListener>();
    
    /** The IProjectOpenedListener */
    private Set<IProjectOpenedListener> m_projectOpenedListeners =
        new HashSet<IProjectOpenedListener>();
    
    /**
     * private constructor
     */
    private DataEventDispatcher() {
        
        // the DataEventDispatcher wrappes events from GeneralStorage
        GeneralStorage.getInstance().addReloadedSessListener(this);
        GeneralStorage.getInstance().addDataModifiedListener(this);
    }
    
    /**
     * @return the single instance
     */
    public static synchronized DataEventDispatcher getInstance() {
        if (instance == null) {
            instance = new DataEventDispatcher();
        }
        return instance;
    }
    
    /**
     * @param l listener for partial changes to the data in the master session
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addDataChangedListener(IDataChangedListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_dataChangedListenersPost.add(l);
        } else {
            m_dataChangedListeners.add(l);
        }
    }
    
    /**
     * @param l listener for changed working language.
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addLanguageChangedListener(ILanguageChangedListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_langChangedListenersPost.add(l);
        } else {
            m_langChangedListeners.add(l);
        }
    }
    /**
     * @param l listener for partial changes to the data in the master session 
     */
    public void removeDataChangedListener(IDataChangedListener l) {
        m_dataChangedListeners.remove(l);
        m_dataChangedListenersPost.remove(l);
    }    

    /**
     * @param l the ILanguageChengedListener to remove.
     */
    public void removeLanguageChangedListener(ILanguageChangedListener l) {
        m_langChangedListeners.remove(l);
        m_langChangedListenersPost.remove(l);
    }
    
    /**
     * @param po
     *            changed persistent object
     * @param dataState
     *            kind of modification
     * @param updateState
     *            determines the parts to update
     */
    public void fireDataChangedListener(IPersistentObject po,
            DataState dataState, UpdateState updateState) {
        fireDataChangedListener(
                new DataChangedEvent(po, dataState, updateState));
    }
    
    /**
     * @param events data changed events
     */
    public void fireDataChangedListener(final DataChangedEvent... events) {
        // model updates
        final Set<IDataChangedListener> stableListeners = 
            new HashSet<IDataChangedListener>(m_dataChangedListeners);
        for (final IDataChangedListener l : stableListeners) {
            try {
                l.handleDataChanged(events);
            } catch (Throwable t) {
                if (t instanceof EntityNotFoundException) {
                    String msg = Messages.DataEventDispatcherReopenProject;
                    ErrorMessagePresenter
                            .getPresenter()
                            .showErrorMessage(
                                    MessageIDs.E_PERSISTENCE_LOAD_FAILED,
                                    null,
                                    new String[] { msg });
                }
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }

        // gui updates
        final Set<IDataChangedListener> stableListenersPost = 
            new HashSet<IDataChangedListener>(m_dataChangedListenersPost);
        for (IDataChangedListener l : stableListenersPost) {
            try {
                l.handleDataChanged(events);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }
    }
 
    /**
     * @param l listener for parameter changes
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addParamChangedListener(
        IParamChangedListener l, boolean guiMode) {
        if (guiMode) {
            m_paramChangedListenersPost.add(l);
        } else {
            m_paramChangedListeners.add(l);
        }
    }
    
    /**
     * @param l listener for parameter changes
     */
    public void removeParamChangedListener(IParamChangedListener l) {
        m_paramChangedListeners.remove(l);
        m_paramChangedListenersPost.remove(l);
    }
    
    /**
     * notify listener about param modification
     */
    public void fireParamChangedListener() {        
        // model updates
        final Set<IParamChangedListener> stableListeners = 
            new HashSet<IParamChangedListener>(m_paramChangedListeners);
        for (IParamChangedListener l : stableListeners) {
            try {
                l.handleParamChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }

        // gui updates
        final Set<IParamChangedListener> stableListenersPost = 
            new HashSet<IParamChangedListener>(m_paramChangedListenersPost);
        for (IParamChangedListener l : stableListenersPost) {
            try {
                l.handleParamChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }
    }
    
    /**
     * notifies the Listener that the Completeness Check is finished.
     */
    public void fireCompletenessCheckFinished() {
        for (ICompletenessCheckListener l : m_completenessCheckListeners) {
            l.completenessCheckFinished();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reloadData(IProgressMonitor monitor) {
        fireProjectLoadedListener(monitor);
    }

    /**
     * @param l listener for close parts
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addPartClosedListener(
        IPartClosedListener l, boolean guiMode) {
        if (guiMode) {
            m_partClosedListenersPost.add(l);
        } else {
            m_partClosedListeners.add(l);
        }
    }

    
    /**
     * @param l listener for close parts
     */
    public void removePartClosedListener(IPartClosedListener l) {
        m_partClosedListeners.remove(l);
        m_partClosedListenersPost.remove(l);
    }

    /**
     * @param l listener for for reacting on changes in PropView or DataSetView
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addPropertyChangedListener(IPropertyChangedListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_propertyChangedListenersPost.add(l);
        } else {
            m_propertyChangedListeners.add(l);
        }
    }

    
    /**
     * @param l listener for reacting on changes in PropView or DataSetView
     */
    public void removePropertyChangedListener(IPropertyChangedListener l) {
        m_propertyChangedListeners.remove(l);
        m_propertyChangedListenersPost.remove(l);
    }
    
    /**
     * notify listener about part closing
     * @param part the part that was closed
     */
    public void firePartClosed(IWorkbenchPart part) {        
        // model updates
        final Set<IPartClosedListener> stableListeners = 
            new HashSet<IPartClosedListener>(m_partClosedListeners);
        for (IPartClosedListener l : stableListeners) {
            try {
                l.handlePartClosed(part);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }

        // gui updates
        final Set<IPartClosedListener> stableListenersPost = 
            new HashSet<IPartClosedListener>(m_partClosedListenersPost);
        for (IPartClosedListener l : stableListenersPost) {
            try {
                l.handlePartClosed(part);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }
    }
    
    /**
     * notify listener about changed properties in PropView or DataSetView
     * @param isCompNameChanged flag to set true, if compName changed
     */
    public void firePropertyChanged(boolean isCompNameChanged) {        
        // model updates
        final Set<IPropertyChangedListener> stableListeners = 
            new HashSet<IPropertyChangedListener>(m_propertyChangedListeners);
        for (IPropertyChangedListener l : stableListeners) {
            try {
                l.handlePropertyChanged(isCompNameChanged);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }

        // gui updates
        final Set<IPropertyChangedListener> stableListenersPost = 
            new HashSet<IPropertyChangedListener>(
                m_propertyChangedListenersPost);
        for (IPropertyChangedListener l : stableListenersPost) {
            try {
                l.handlePropertyChanged(isCompNameChanged);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * notifies the Listeners about the changed working language
     * @param locale the new Locale.
     */
    public void fireLanguageChanged(Locale locale) {
        // model updates
        final Set<ILanguageChangedListener> langChangedListeners = 
            new HashSet<ILanguageChangedListener>(m_langChangedListeners);
        for (ILanguageChangedListener l : langChangedListeners) {
            try {
                l.handleLanguageChanged(locale);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<ILanguageChangedListener> stableListenersPost = 
            new HashSet<ILanguageChangedListener>(m_langChangedListenersPost);
        for (ILanguageChangedListener l : stableListenersPost) {
            try {
                l.handleLanguageChanged(locale);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for loaded project
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addProjectLoadedListener(IProjectLoadedListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_projectLoadedListenersPost.add(l);
        } else {
            m_projectLoadedListeners.add(l);
        }
    }
    
   

    /**
     * @param l listener for reply to load of a project
     */
    public void removeProjectLoadedListener(IProjectLoadedListener l) {
        m_projectLoadedListeners.remove(l);
        m_projectLoadedListenersPost.remove(l);
    }
    
    /**
     * notify listener about loading of a project
     * 
     * @param monitor The progress monitor for this poperation.
     */
    public void fireProjectLoadedListener(IProgressMonitor monitor) {
        new LoadProjectDataOperation().run(monitor);
    }
    
    /**
     * @param l listener for new project
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addProjectCreatedListener(IProjectCreatedListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_projectCreatedListenersPost.add(l);
        } else {
            m_projectCreatedListeners.add(l);
        }
    }
    
    /**
     * @param l listener for reply to create a new project
     */
    public void removeProjectCreatedListener(IProjectCreatedListener l) {
        m_projectCreatedListeners.remove(l);
        m_projectCreatedListenersPost.remove(l);
    }
    
    /**
     * notify listener about loading of a project
     */
    public void fireProjectCreatedListener() {
        // model updates
        final Set<IProjectCreatedListener> stableListeners = 
            new HashSet<IProjectCreatedListener>(m_projectCreatedListeners);
        for (IProjectCreatedListener l : stableListeners) {
            try {
                l.handleProjectCreated();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }        

        // gui updates
        final Set<IProjectCreatedListener> stableListenersPost = 
            new HashSet<IProjectCreatedListener>(m_projectCreatedListenersPost);
        for (IProjectCreatedListener l : stableListenersPost) {
            try {
                l.handleProjectCreated();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for server connection state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addServerConnectionListener(IServerConnectionListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_serverConnectionListenersPost.add(l);
        } else {
            m_serverConnectionListeners.add(l);
        }
    }

    
    /**
     * @param l listener for server connection state
     */
    public void removeServerConnectionListener(IServerConnectionListener l) {
        m_serverConnectionListeners.remove(l);
        m_serverConnectionListenersPost.remove(l);
    }
    
    /**
     * notify listener about modification of server connection state
     * @param state of server connection
     */
    public void fireServerConnectionChanged(ServerState state) {
        final Set<IServerConnectionListener> stableListeners = 
            new HashSet<IServerConnectionListener>(m_serverConnectionListeners);
        for (IServerConnectionListener l : stableListeners) {
            try {
                l.handleServerConnStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IServerConnectionListener> stableListenersPost = 
            new HashSet<IServerConnectionListener>(
                m_serverConnectionListenersPost);
        for (IServerConnectionListener l : stableListenersPost) {
            try {
                l.handleServerConnStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }

    /**
     * @param l listener for server connection state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addAutServerConnectionListener(IServerConnectionListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_autServerConnectionListenersPost.add(l);
        } else {
            m_autServerConnectionListeners.add(l);
        }
    }

    
    /**
     * @param l listener for AUT Server connection state
     */
    public void removeAutServerConnectionListener(IServerConnectionListener l) {
        m_autServerConnectionListeners.remove(l);
        m_autServerConnectionListenersPost.remove(l);
    }
    
    /**
     * notify listener about modification of AUT Server connection state
     * @param state of AUT Server connection
     */
    public void fireAutServerConnectionChanged(ServerState state) {
        final Set<IServerConnectionListener> stableListeners = 
            new HashSet<IServerConnectionListener>(
                    m_autServerConnectionListeners);
        for (IServerConnectionListener l : stableListeners) {
            try {
                l.handleServerConnStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IServerConnectionListener> stableListenersPost = 
            new HashSet<IServerConnectionListener>(
                m_autServerConnectionListenersPost);
        for (IServerConnectionListener l : stableListenersPost) {
            try {
                l.handleServerConnStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }

    /**
     * 
     */
    public void fireProjectPropertiesModified() {
        // model updates
        final Set<IProjectPropertiesModifyListener> stableListeners = 
            new HashSet<IProjectPropertiesModifyListener>(
                m_projectPropertiesModifyListeners);
        for (IProjectPropertiesModifyListener l : stableListeners) {
            try {
                l.handleProjectPropsChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IProjectPropertiesModifyListener> stableListenersPost = 
            new HashSet<IProjectPropertiesModifyListener>(
                m_projectPropertiesModifyListenersPost);
        for (IProjectPropertiesModifyListener l : stableListenersPost) {
            try {
                l.handleProjectPropsChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for for reacting on changes in PropView or DataSetView
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addProjectPropertiesModifyListener(
        IProjectPropertiesModifyListener l, boolean guiMode) {
        if (guiMode) {
            m_projectPropertiesModifyListenersPost.add(l);
        } else {
            m_projectPropertiesModifyListeners.add(l);
        }
    }

    /**
     * @param l listener for modification of project properties
     */
    public void removeProjectPropertiesModifyListener(
        IProjectPropertiesModifyListener l) {
        m_projectPropertiesModifyListeners.remove(l);
        m_projectPropertiesModifyListenersPost.remove(l);
    }
    
    /**
     * @param l listener for Record Mode state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addRecordModeStateListener(IRecordModeStateListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_recordModeStateListenersPost.add(l);
        } else {
            m_recordModeStateListeners.add(l);
        }
    }

    /**
     * @param l listener for Record Mode state
     */
    public void removeRecordModeStateListener(IRecordModeStateListener l) {
        m_recordModeStateListeners.remove(l);
        m_recordModeStateListenersPost.remove(l);
    }
    
    /**
     * 
     * @param l an ICompletenessCheckListener.
     * @return true if the given Listener was added, false otherwise.
     */
    public boolean addCompletenessCheckListener(ICompletenessCheckListener l) {
        return m_completenessCheckListeners.add(l);
    }
    
    /**
     * 
     * @param l an ICompletenessCheckListener.
     * @return true if the given Listener was removed, false otherwise.
     */
    public boolean removeCompletenessCheckListener(
            ICompletenessCheckListener l) {
        
        return m_completenessCheckListeners.remove(l);
    }
    
    /**
     * notify listener about modification of Record Mode
     * @param state of Record Mode
     */
    public void fireRecordModeStateChanged(RecordModeState state) {
        // model updates
        final Set<IRecordModeStateListener> stableListeners = 
            new HashSet<IRecordModeStateListener>(m_recordModeStateListeners);
        for (IRecordModeStateListener l : stableListeners) {
            try {
                l.handleRecordModeStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IRecordModeStateListener> stableListenersPost = 
            new HashSet<IRecordModeStateListener>(
                m_recordModeStateListenersPost);
        for (IRecordModeStateListener l : stableListenersPost) {
            try {
                l.handleRecordModeStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for OM Mode state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addOMStateListener(IOMStateListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_omStateListenersPost.add(l);
        } else {
            m_omStateListeners.add(l);
        }
    }
    
    /**
     * @param l listener for OM Mode state
     */
    public void removeOMStateListener(IOMStateListener l) {
        m_omStateListeners.remove(l);
        m_omStateListenersPost.remove(l);
    }
    
    /**
     * notify listener about modification of OM Mode
     * @param state of OM Mode
     */
    public void fireOMStateChanged(OMState state) {
        // model updates
        final Set<IOMStateListener> stableListeners = 
            new HashSet<IOMStateListener>(m_omStateListeners);
        for (IOMStateListener l : stableListeners) {
            try {
                l.handleOMStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IOMStateListener> stableListenersPost = 
            new HashSet<IOMStateListener>(m_omStateListenersPost);
        for (IOMStateListener l : stableListenersPost) {
            try {
                l.handleOMStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for server connection state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addAutStateListener(IAutStateListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_autStateListenersPost.add(l);
        } else {
            m_autStateListeners.add(l);
        }
    }
    
    /**
     * @param l listener for server connection state
     */
    public void removeAutStateListener(IAutStateListener l) {
        m_autStateListeners.remove(l);
        m_autStateListenersPost.remove(l);
    }
    
    /**
     * notify listener about modification of server connection state
     * @param state of server connection
     */
    public void fireAutStateChanged(AutState state) {
        // model updates
        final Set<IAutStateListener> stableListeners = 
            new HashSet<IAutStateListener>(m_autStateListeners);
        for (IAutStateListener l : stableListeners) {
            try {
                l.handleAutStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IAutStateListener> stableListenersPost = 
            new HashSet<IAutStateListener>(m_autStateListenersPost);
        for (IAutStateListener l : stableListenersPost) {
            try {
                l.handleAutStateChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for test result changes
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addTestresultListener(
            ITestresultChangedListener l, boolean guiMode) {
        if (guiMode) {
            m_testresultListenersPost.add(l);
        } else {
            m_testresultListeners.add(l);
        }
    }
    
    /**
     * @param l listener for test result changes
     */
    public void removeTestresultListener(
            ITestresultChangedListener l) {
        m_testresultListeners.remove(l);
        m_testresultListenersPost.remove(l);
    }
    
    /**
     * notify listener about changes of test results
     * @param state TestresultState (clear or refresh)
     */
    public void fireTestresultChanged(TestresultState state) {
        // model updates
        final Set<ITestresultChangedListener> stableListeners = 
            new HashSet<ITestresultChangedListener>(
                    m_testresultListeners);
        for (ITestresultChangedListener l : stableListeners) {
            try {
                l.handleTestresultChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<ITestresultChangedListener> stableListenersPost = 
            new HashSet<ITestresultChangedListener>(
                    m_testresultListenersPost);
        for (ITestresultChangedListener l : stableListenersPost) {
            try {
                l.handleTestresultChanged(state);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * @param l listener for modification of testsuite state
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addTestSuiteStateListener(ITestSuiteStateListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_testSuiteStateListenersPost.add(l);
        } else {
            m_testSuiteStateListeners.add(l);
        }
    }
    /**
     * @param l listener for modification of testsuite state
     */
    public void removeTestSuiteStateListener(ITestSuiteStateListener l) {
        m_testSuiteStateListeners.remove(l);
        m_testSuiteStateListenersPost.remove(l);
    }
    
    /**
     * notify listener about modification of testsuite state
     * @param event occured event
     */
    public void fireTestSuiteStateChanged(TestExecutionEvent event) {
        // model updates
        final Set<ITestSuiteStateListener> stableListeners = 
            new HashSet<ITestSuiteStateListener>(m_testSuiteStateListeners);
        for (ITestSuiteStateListener l : stableListeners) {
            try {
                l.handleTSStateChanged(event);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<ITestSuiteStateListener> stableListenersPost = 
            new HashSet<ITestSuiteStateListener>(
                m_testSuiteStateListenersPost);
        for (ITestSuiteStateListener l : stableListenersPost) {
            try {
                l.handleTSStateChanged(event);
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }     
    
    /**
     * @param l listener for modification of server preferences
     * @param guiMode
     *      should this listener be called after the model listener 
     */
    public void addServerPrefListener(IServerPrefListener l, 
        boolean guiMode) {
        if (guiMode) {
            m_serverPrefListenersPost.add(l);
        } else {
            m_serverPrefListeners.add(l);
        }
    }

    /**
     * @param l listener for modification of server preferences
     */
    public void removeServerPrefListener(IServerPrefListener l) {
        m_serverPrefListeners.remove(l);
        m_serverPrefListenersPost.remove(l);
    }
    /**
     * notify listeners that a project is opened
     */
    public void fireProjectOpenedListener() {
        final Set<IProjectOpenedListener> projectOpenedListeners =
            new HashSet<IProjectOpenedListener>(m_projectOpenedListeners);
        for (IProjectOpenedListener l : projectOpenedListeners) {
            try {
                l.handleProjectOpened();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t);
            }
        }
    }
    
    /**
     * @param l listener for project is opened
     */
    public void addProjectOpenedListener(IProjectOpenedListener l) {
        m_projectOpenedListeners.add(l);
    }
    /**
     * @param l listener for project is opened
     */
    public void removeProjectOpenedListener(IProjectOpenedListener l) {
        m_projectOpenedListeners.remove(l);
    }
    /**
     * notify listener about modification of server preferences
     */
    public void fireServerPreferencesChanged() {
        // model updates
        final Set<IServerPrefListener> stableListeners = 
            new HashSet<IServerPrefListener>(m_serverPrefListeners);
        for (IServerPrefListener l : stableListeners) {
            try {
                l.handlePrefServerChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }

        // gui updates
        final Set<IServerPrefListener> stableListenersPost = 
            new HashSet<IServerPrefListener>(
                m_serverPrefListenersPost);
        for (IServerPrefListener l : stableListenersPost) {
            try {
                l.handlePrefServerChanged();
            } catch (Throwable t) {
                LOG.error(Messages.UnhandledExceptionWhileCallListeners, t); 
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void dataModified(IPersistentObject po) {
        fireDataChangedListener(new DataChangedEvent(po,
                DataState.StructureModified, UpdateState.all));
    }

    /**
     * @return the dialogStatusListener
     */
    public ListenerManager<IDialogStatusListener> 
    getDialogStatusListenerMgr() {
        return m_dialogStatusListenerMgr;
    }
}