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
package org.eclipse.jubula.client.ui.views.problemview;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jubula.client.core.model.IExecTestCasePO;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.model.ITestSuitePO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.utils.ExecTreeTraverser;
import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
import org.eclipse.jubula.client.core.utils.SpecTreeTraverser;
import org.eclipse.jubula.client.core.utils.TreeTraverser;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.actions.ChooseServerAction;
import org.eclipse.jubula.client.ui.actions.ImportFileAction;
import org.eclipse.jubula.client.ui.actions.NewProjectAction;
import org.eclipse.jubula.client.ui.actions.OpenProjectAction;
import org.eclipse.jubula.client.ui.constants.CommandIDs;
import org.eclipse.jubula.client.ui.constants.Constants;
import org.eclipse.jubula.client.ui.constants.ProblemType;
import org.eclipse.jubula.client.ui.editors.ObjectMappingMultiPageEditor;
import org.eclipse.jubula.client.ui.editors.TestCaseEditor;
import org.eclipse.jubula.client.ui.handlers.ProjectPropertiesHandler;
import org.eclipse.jubula.client.ui.handlers.open.AbstractOpenHandler;
import org.eclipse.jubula.client.ui.utils.CommandHelper;
import org.eclipse.jubula.client.ui.utils.DialogUtils;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;


/**
 * Creates and returns resolutions for Jubula-related markers/problems.
 *
 * @author BREDEX GmbH
 * @created 02.06.2008
 */
public class JBMarkerResolutionGenerator implements IMarkerResolutionGenerator {

    /** the logger */
    private static Log log = 
        LogFactory.getLog(JBMarkerResolutionGenerator.class);

    /**
     * @author BREDEX GmbH
     * @created Mar 12, 2008
     */
    private static final class OpenSpecTcEditorOperation 
        implements ITreeNodeOperation<INodePO> {
        
        
        /**
         * <code>m_gdObject</code>
         */
        private final Object m_gdObject;

        /**
         * @param gdObject An ISpecTestCasePO.toString() 
         * or IExecTestCasePO.toString()
         */
        public OpenSpecTcEditorOperation(Object gdObject) {
            m_gdObject = gdObject;
        }

        /**
         * {@inheritDoc}
         */
        public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                INodePO parent, INodePO node, boolean alreadyVisited) {
            
            if (node.toString().equals(m_gdObject.toString())) {
                ctx.setContinued(false);
                if (node instanceof IExecTestCasePO) {
                    IExecTestCasePO execTcPO = (IExecTestCasePO)node;
                    ISpecTestCasePO specTcPO = execTcPO.getSpecTestCase();
                    AbstractOpenHandler.openEditor(specTcPO);  
                } else if (node instanceof ISpecTestCasePO) {
                    ISpecTestCasePO specTcPO = (ISpecTestCasePO)node;
                    AbstractOpenHandler.openEditor(specTcPO);
                }
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public void postOperate(ITreeTraverserContext<INodePO> ctx, 
                INodePO parent, INodePO node, boolean alreadyVisited) {
            // no op
        }
    }

    /**
     * Opens the editor for a specific test suite.
     * 
     * @author BREDEX GmbH
     * @created 06.06.2008
     */
    private static class OpenTSEditorMarkerResolution 
            implements IMarkerResolution {

        /** the name of the test suite for which to open the editor */
        private String m_tsName;

        /**
         * Constructor
         * 
         * @param testSuiteName The name of the test suite for which to
         *                      open the editor.
         */
        public OpenTSEditorMarkerResolution(String testSuiteName) {
            m_tsName = testSuiteName;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getLabel() {
            return I18n.getString("GDProblemView.openTestSuiteEditor", //$NON-NLS-1$ 
                    new String[] {m_tsName});
        }

        /**
         * {@inheritDoc}
         */
        public void run(IMarker marker) {
            List<ITestSuitePO> tsList = GeneralStorage.getInstance()
                .getProject().getTestSuiteCont().getTestSuiteList();
            for (ITestSuitePO ts : tsList) {
                if (ts.toString().equals(m_tsName)) {
                    IEditorPart editor = AbstractOpenHandler.openEditor(ts); 
                    if (editor != null) {
                        editor.setFocus();
                    }
                    break;
                }
            }
        }
        
    }
    
    /**
     * Opens the editor for a specific test suite.
     * 
     * @author BREDEX GmbH
     * @created 06.06.2008
     */
    private static class OpenOMEditorMarkerResolution 
            implements IMarkerResolution {

        /** the name of the test suite for which to open the editor */
        private String m_tsName;

        /**
         * Constructor
         * 
         * @param testSuiteName The name of the test suite for which to
         *                      open the editor.
         */
        public OpenOMEditorMarkerResolution(String testSuiteName) {
            m_tsName = testSuiteName;
        }
        
        /**
         * {@inheritDoc}
         */
        public String getLabel() {
            return I18n.getString("GDProblemView.openObjectMappingEditor", //$NON-NLS-1$ 
                    new String[] {m_tsName});
        }

        /**
         * {@inheritDoc}
         */
        public void run(IMarker marker) {
            List<ITestSuitePO> tsList = GeneralStorage.getInstance()
                .getProject().getTestSuiteCont().getTestSuiteList();
            for (ITestSuitePO ts : tsList) {
                if (ts.toString().equals(m_tsName)) {
                    ObjectMappingMultiPageEditor editor =
                        (ObjectMappingMultiPageEditor)
                        AbstractOpenHandler.openEditor(ts.getAut());
                    editor.getSite().getPage().activate(editor);
                    break;
                }
            }
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        
        Object reasonInt;
        try {
            reasonInt = marker.getAttribute(Constants.GD_REASON);
            if (!(reasonInt instanceof Integer)
                    || reasonInt == null) {
                return new IMarkerResolution[0];
            }

            ProblemType type = ProblemType.values()[((Integer)reasonInt)
                                                    .intValue()];

            Object gdObject = marker.getAttribute(Constants.GD_OBJECT); 

            return getResolutions(type, gdObject);
        } catch (CoreException ce) {
            log.info("Error occurred while finding resolutions for " //$NON-NLS-1$
                + "a Problem Marker", ce); //$NON-NLS-1$
            return new IMarkerResolution[0];
        } 
    }

    /**
     * Returns a group of possible resolutions for a problem with the given
     * attributes.
     * 
     * @param type The type of the problem.
     * @param gdObject The object causing the problem.
     * @return resolutions for a problem with the given type and object.
     */
    private IMarkerResolution[] getResolutions(ProblemType type, 
            Object gdObject) {

        switch (type) {
            case REASON_CONNECTED_TO_NO_SERVER:
                return getNoServerConnectionResolutions();
            case REASON_EMPTY_TESTSUITE:
            case REASON_NO_AUT_FOR_TESTSUITE_SELECTED:
            case REASON_TD_INCOMPLETE:
                return getOpenTSEditorResolutions(gdObject);
            case REASON_NO_AUT_FOR_PROJECT_EXISTS:
            case REASON_NO_AUTCONFIG_FOR_SERVER_EXIST:
            case REASON_NOJAR_FOR_AUTCONFIG:
            case REASON_NOSERVER_FOR_AUTCONFIG:
                return getNoAUTResolutions();
            case REASON_NO_PROJECT:
                return getNoProjectResolutions();
            case REASON_NO_TESTSUITE:
                return getNoTestSuiteResolutions();
            case REASON_OM_INCOMPLETE:
                return getOMIncompleteResolutions(gdObject);
            case REASON_NO_SERVER_DEFINED:
                return getNoServerDefinedResolutions();
            case REASON_DEPRECATED_ACTION:
            case REASON_DEPRECATED_COMP:
                return getDeprecatedActionOrComponentResolutions(gdObject);
            case REASON_NO_COMPTYPE:
                return getNoCompTypeResolutions(gdObject);
            case REASON_PROJECT_DOES_NOT_EXIST:
                return getMissingProjectResolutions();
            case REASON_MISSING_SPEC_TC:
                return getMissingSpecTcResolutions(gdObject);
            case REASON_UNUSED_TESTDATA:
                return getUnusedTestDataResolutions(gdObject);
            default:
                return new IMarkerResolution[0];
        }
    }

    /**
     * 
     * @return resolutions for no connection to server.
     */
    private IMarkerResolution[] getNoServerConnectionResolutions() {
        // FIXME zeb not sure if this is the correct way to
        //          globally access an action
        return new IMarkerResolution [] {new IMarkerResolution() {

            public String getLabel() {
                return I18n.getString("GDProblemView.connectToAutStarter"); //$NON-NLS-1$
            }

            public void run(IMarker marker) {
                new ChooseServerAction().runWithEvent(null, new Event());
            }
        }
        };
    }

    /**
     * 
     * @return resolutions for a missing project.
     */
    private IMarkerResolution[] getMissingProjectResolutions() {
        return new IMarkerResolution[] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.openReusedProjectSettings"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    ICommandService commandService =
                        (ICommandService)PlatformUI.getWorkbench().getService(
                                ICommandService.class);
                    IHandlerService handlerService = 
                        (IHandlerService)PlatformUI.getWorkbench().getService(
                                IHandlerService.class);
                    Command projectPropertiesCommand = 
                        commandService.getCommand(
                                CommandIDs.PROJECT_PROPERTIES_COMMAND_ID);
                    Map<String, String> parameters = 
                        new HashMap<String, String>();
                    parameters.put(ProjectPropertiesHandler.SECTION_TO_OPEN, 
                            Constants.REUSED_PROJECT_PROPERTY_ID);
                    try {
                        handlerService.executeCommand(
                                ParameterizedCommand.generateCommand(
                                        projectPropertiesCommand, parameters), 
                                null);
                    } catch (CommandException ce) {
                        log.error(
                            "Error occurred while opening Project Settings", //$NON-NLS-1$
                            ce);
                    }
                }
                
            }
        };
    }

    /**
     * 
     * @param gdObject the SpecTestCasePO
     * @return resolutions for a reference to a non-existent SpecTestCase.
     */
    private IMarkerResolution[] getMissingSpecTcResolutions(
            final Object gdObject) {
        return new IMarkerResolution[] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.openTestCaseEditor"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    final ITreeNodeOperation<INodePO> op = 
                        new ITreeNodeOperation<INodePO>() {
                            public boolean operate(
                                ITreeTraverserContext<INodePO> ctx, 
                                INodePO parent, INodePO node, 
                                boolean alreadyVisited) {

                                if (gdObject == null) {
                                    ctx.setContinued(false);
                                } else {
                                    if (node.toString().equals(
                                            gdObject.toString())) {
                                        ctx.setContinued(false);
                                        if (node instanceof IExecTestCasePO) {
                                            IExecTestCasePO execTcPO = 
                                                (IExecTestCasePO)node;
                                            ISpecTestCasePO specTcPO = 
                                                execTcPO.getSpecTestCase();
                                            AbstractOpenHandler
                                            .openEditor(specTcPO);  
                                        } else if (node 
                                                instanceof ISpecTestCasePO) {
                                            ISpecTestCasePO specTcPO = 
                                                (ISpecTestCasePO)node;
                                            AbstractOpenHandler
                                                .openEditor(specTcPO);
                                        } else if (
                                                node instanceof ITestSuitePO) {
                                            ITestSuitePO testSuitePO = 
                                                (ITestSuitePO)node;
                                            AbstractOpenHandler
                                            .openEditor(testSuitePO);  
                                        }
                                    }
                                }
                                return true;
                            }
                            public void postOperate(
                                    ITreeTraverserContext<INodePO> ctx, 
                                    INodePO parent, INodePO node, 
                                    boolean alreadyVisited) {
                                // no op
                            }
                        };
                    IProjectPO project = 
                        GeneralStorage.getInstance().getProject();
                    TreeTraverser traverser = 
                        new ExecTreeTraverser(project, op);
                    traverser.traverse();
                }
                
            }
        };
    }

    /**
     * @param gdObject the SpecTestCAsePO
     * @return resolutions for use of a deprecated component or action.
     */
    private IMarkerResolution[] getDeprecatedActionOrComponentResolutions(
            final Object gdObject) {
        
        return new IMarkerResolution[] {
            new IMarkerResolution() {

                    public String getLabel() {
                        return I18n.getString("GDProblemView.openTestCaseEditor"); //$NON-NLS-1$
                    }

                    public void run(IMarker marker) {
                        final ITreeNodeOperation<INodePO> op = 
                            new ITreeNodeOperation<INodePO>() {
                                public boolean operate(
                                    ITreeTraverserContext<INodePO> ctx, 
                                    INodePO parent, INodePO node, 
                                    boolean alreadyVisited) {
                                
                                    if (node.toString().equals(
                                            gdObject.toString())) {
                                        ctx.setContinued(false);
                                        INodePO selectedNode = node;
                                        INodePO editableNode = 
                                            node.getParentNode();
                                        if (editableNode == null) {
                                            return true;
                                        }
                                        TestCaseEditor editor = 
                                            (TestCaseEditor)AbstractOpenHandler
                                                .openEditor(editableNode);
                                        if (editor != null) {
                                            editor.setOpeningSelection(
                                                    selectedNode);
                                        }
                                    }
                                    return true;
                                }
                                public void postOperate(
                                        ITreeTraverserContext<INodePO> ctx, 
                                        INodePO parent, INodePO node, 
                                        boolean alreadyVisited) {
                                    // no op
                                }
                            };
                        TreeTraverser traverser = 
                            new SpecTreeTraverser(GeneralStorage
                                    .getInstance().getProject(), op);
                        traverser.traverse();
                    }
                    
                }
        };
    }
    
    /**
     * 
     * @param gdObject the SpecTestCasePO
     * @return resolutions for a component without a type.
     */
    private IMarkerResolution[] getNoCompTypeResolutions(
            final Object gdObject) {
        if (gdObject.toString().contains("TestSuitePO")) { //$NON-NLS-1$
            return getOpenTSEditorResolutions(gdObject);
        }
        
        return new IMarkerResolution [] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.openTestCaseEditor"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    IProjectPO project = 
                        GeneralStorage.getInstance().getProject();
                    final ITreeNodeOperation<INodePO> op = 
                        new OpenSpecTcEditorOperation(gdObject);
                    TreeTraverser traverser = 
                        new SpecTreeTraverser(project, op);
                    traverser.traverse();
                }
                
            }
        };
    }

    /**
     * 
     * @return resolutions for no AUT defined.
     */
    private IMarkerResolution[] getNoAUTResolutions() {
        return new IMarkerResolution [] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.openAutSettings"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    ICommandService commandService =
                        (ICommandService)PlatformUI.getWorkbench().getService(
                                ICommandService.class);
                    IHandlerService handlerService = 
                        (IHandlerService)PlatformUI.getWorkbench().getService(
                                IHandlerService.class);
                    Command projectPropertiesCommand = 
                        commandService.getCommand(
                                CommandIDs.PROJECT_PROPERTIES_COMMAND_ID);
                    Map<String, String> parameters = 
                        new HashMap<String, String>();
                    parameters.put(ProjectPropertiesHandler.SECTION_TO_OPEN, 
                            Constants.AUT_PROPERTY_ID);
                    try {
                        handlerService.executeCommand(
                                ParameterizedCommand.generateCommand(
                                        projectPropertiesCommand, parameters), 
                                null);
                    } catch (CommandException ce) {
                        log.error(
                            "Error occurred while opening Project Settings", //$NON-NLS-1$
                            ce);
                    }
                }
                
            }
        };
    }
    
    /**
     *
     * @return resolutions for no AutStarter host defined in workspace 
     *         configuration.
     */ 
    private IMarkerResolution[] getNoServerDefinedResolutions() {
        return new IMarkerResolution[] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.openAutStarterPreferences"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    PreferenceManager pm = Plugin.getDefault()
                        .getWorkbench().getPreferenceManager();
                    for (Object obj : pm.getElements(
                            PreferenceManager.PRE_ORDER)) {
                        PreferenceNode node = (PreferenceNode)obj;
                        if (node.getId().equals(
                                Constants.JB_PREF_PAGE_AUTAGENT)) {
                            PreferenceDialog dialog = PreferencesUtil.
                            createPreferenceDialogOn(
                                    null, Constants.JB_PREF_PAGE_AUTAGENT, 
                                    null, null);
                            DialogUtils.setWidgetNameForModalDialog(dialog);
                            dialog.open();
                            break;
                        }
                    }
                }
                
            }
        };
    }

     /**
      * 
      * @return resolutions for no opened project.
      */
    private IMarkerResolution[] getNoProjectResolutions() {
        return new IMarkerResolution [] {
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.create"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    // FIXME zeb not sure if this is the correct way
                    //           to globally access an action
                    new NewProjectAction().runWithEvent(null, new Event());
                }
                
            },
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.import"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    // FIXME zeb not sure if this is the correct way to
                    //          globally access an action
                    new ImportFileAction().runWithEvent(null, new Event());
                }
                
            },
            new IMarkerResolution() {

                public String getLabel() {
                    return I18n.getString("GDProblemView.open"); //$NON-NLS-1$
                }

                public void run(IMarker marker) {
                    OpenProjectAction.getAction().runWithEvent(new Event());
                }
                
            },
                        
        };
        
    }
    
    /**
     * 
     * @param obj ObjectString to open an editor for
     * @return resolutions for incomplete object mapping.
     */
    private IMarkerResolution[] getOMIncompleteResolutions(Object obj) {
        if (obj instanceof String) {
            return new IMarkerResolution[] {
                new OpenOMEditorMarkerResolution((String)obj)
            };
        } 
        return new IMarkerResolution[0];
    }

    /**
     * 
     * @param gdObject should be an instance of ISpecTestCasePO
     * @return resolutions for unused test data in a Test Case.
     */
    private IMarkerResolution[] getUnusedTestDataResolutions(Object gdObject) {
        return getNoCompTypeResolutions(gdObject);
    }
    
    
    /**
     * 
     * @param obj ObjectString to open an editor for
     * @return resolutions for problems that require opening a Test Suite in 
     *         the Test Suite Editor.
     */
    private IMarkerResolution[] getOpenTSEditorResolutions(Object obj) {
        if (obj instanceof String) {
            return new IMarkerResolution [] {
                new OpenTSEditorMarkerResolution((String)obj)};
        } 
        
        return new IMarkerResolution[0];
    }
 
    /**
     * 
     * @return resolutions for no test suite defined.
     */
    private IMarkerResolution[] getNoTestSuiteResolutions() {
        return new IMarkerResolution[] { new IMarkerResolution() {

            public String getLabel() {
                return I18n.getString("GDProblemView.createTestSuite"); //$NON-NLS-1$
            }

            public void run(IMarker marker) {
                CommandHelper
                        .executeCommand(CommandIDs.NEW_TESTSUITE_COMMAND_ID);
            }
        }
        };
    }
}