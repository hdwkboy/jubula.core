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
package org.eclipse.jubula.client.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jubula.client.core.businessprocess.db.TestCaseBP;
import org.eclipse.jubula.client.core.events.DataEventDispatcher;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
import org.eclipse.jubula.client.core.model.IExecTestCasePO;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.persistence.PMException;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.constants.IconConstants;
import org.eclipse.jubula.client.ui.controllers.PMExceptionHandler;
import org.eclipse.jubula.client.ui.dialogs.InputDialog;
import org.eclipse.jubula.client.ui.editors.GDEditorHelper;
import org.eclipse.jubula.client.ui.editors.TestCaseEditor;
import org.eclipse.jubula.client.ui.model.GuiNode;
import org.eclipse.jubula.client.ui.utils.DialogUtils;
import org.eclipse.jubula.tools.exception.GDProjectDeletedException;
import org.eclipse.jubula.tools.i18n.I18n;


/**
 *  Superclass of all NewTestCaseActions
 *
 * @author BREDEX GmbH
 * @created 27.06.2006
 */
public abstract class AbstractNewTestCaseAction extends Action {

    /** the help id */
    private String m_helpid = null;
    
    /**
     * Constructor.
     */
    public AbstractNewTestCaseAction() {
        super(I18n.getString("AbstractNewTestCaseAction.newTC")); //$NON-NLS-1$
        setImageDescriptor(IconConstants.NEW_TC_IMAGE_DESCRIPTOR); 
        setDisabledImageDescriptor(IconConstants.
                NEW_TC_DISABLED_IMAGE_DESCRIPTOR); 
        setEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        TestCaseEditor tce = (TestCaseEditor)Plugin.getActiveEditor();
        if (!(tce.getTreeViewer().getSelection() 
                instanceof IStructuredSelection)) {
            return;
        }
        if (GDEditorHelper.EditableState.OK == tce.getEditorHelper()
                .requestEditableState()) {
            GuiNode selectedGuiNode = (GuiNode)((IStructuredSelection)tce
                    .getTreeViewer().getSelection()).getFirstElement();
            final ISpecTestCasePO editorNode = (ISpecTestCasePO)tce
                    .getEditorHelper().getEditSupport().getWorkVersion();
            INodePO selectedNode = selectedGuiNode.getContent();
            InputDialog dialog = new InputDialog(Plugin.getShell(), I18n
                    .getString("NewTestCaseAction.TCTitle"), //$NON-NLS-1$
                    I18n.getString("NewTestCaseAction.newTestCase"), //$NON-NLS-1$
                    I18n.getString("NewTestCaseAction.TCMessage"), //$NON-NLS-1$
                    I18n.getString("RenameAction.TCLabel"), //$NON-NLS-1$
                    I18n.getString("RenameAction.TCError"), //$NON-NLS-1$
                    I18n.getString("NewTestCaseAction.doubleTCName"), //$NON-NLS-1$
                    IconConstants.NEW_TC_DIALOG_STRING, I18n
                            .getString("NewTestCaseAction.TCShell"), false); //$NON-NLS-1$
            if (m_helpid != null) {
                dialog.setHelpAvailable(true);
                dialog.create();
                Plugin.getHelpSystem().setHelp(dialog.getShell(), m_helpid);
            } else {
                dialog.create();
            }
            DialogUtils.setWidgetNameForModalDialog(dialog);
            dialog.open();
            ISpecTestCasePO newSpecTC = null;
            if (Window.OK == dialog.getReturnCode()) {
                String tcName = dialog.getName();
                IProjectPO parent = GeneralStorage.getInstance().getProject();
                try {
                    newSpecTC = TestCaseBP.createNewSpecTestCase(tcName,
                            parent, null);
                    DataEventDispatcher.getInstance().fireDataChangedListener(
                            newSpecTC, DataState.Added, UpdateState.all);
                } catch (PMException e) {
                    PMExceptionHandler.handlePMExceptionForMasterSession(e);
                } catch (GDProjectDeletedException e) {
                    PMExceptionHandler.handleGDProjectDeletedException();
                }
            }
            if (newSpecTC != null) {
                Integer index = null;
                if (!(selectedNode instanceof ISpecTestCasePO)) {
                    index = getPositionToInsert(editorNode, selectedGuiNode);
                }

                try {
                    ISpecTestCasePO workNewSpecTC = (ISpecTestCasePO)tce
                            .getEditorHelper().getEditSupport()
                            .createWorkVersion(newSpecTC);
                    IExecTestCasePO newExecTC = TestCaseBP
                            .addReferencedTestCase(tce.getEditorHelper()
                                    .getEditSupport(), editorNode,
                                    workNewSpecTC, index);

                    tce.getEditorHelper().setDirty(true);
                    DataEventDispatcher.getInstance().fireDataChangedListener(
                            newExecTC, DataState.Added, UpdateState.all);
                } catch (PMException e) {
                    PMExceptionHandler.handlePMExceptionForEditor(e, tce);
                }
            }
        }
    }
    
    /**
     * @param selectedNodeGUI the currently selected guiNode
     * @param workTC the workversion of the current specTC
     * @return the position to add
     */
    protected abstract Integer getPositionToInsert(ISpecTestCasePO workTC,
            GuiNode selectedNodeGUI);

    /**
     * @param helpid the helpid to set
     */
    public void setHelpid(String helpid) {
        m_helpid = helpid;
    }
}