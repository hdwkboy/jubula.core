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
package org.eclipse.jubula.client.ui.businessprocess;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP.ToolkitPluginError;
import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP.ToolkitPluginError.ERROR;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProjectLoadedListener;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.IPersistentObject;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.core.model.IUsedToolkitPO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.ui.utils.Utils;
import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
import org.eclipse.jubula.tools.exception.Assert;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.jubula.tools.messagehandling.MessageIDs;
import org.eclipse.jubula.tools.xml.businessmodell.ToolkitPluginDescriptor;


/**
 * Class to handle / control toolkit settings.
 *
 * @author BREDEX GmbH
 * @created 09.07.2007
 */
public class ToolkitBP implements IProjectLoadedListener, IDataChangedListener {

    /**
     * The singleton instance
     */
    private static ToolkitBP instance = null;
    
    
    /**
     * Constructor
     */
    private  ToolkitBP() {
        // nothing
    }
    
    /**
     * {@inheritDoc}
     */
    public void handleProjectLoaded() {
        final IProjectPO project = GeneralStorage.getInstance().getProject();
        if (project != null) {
            UsedToolkitBP.getInstance().getToolkitLevel(project);            
        }
    }
    
    /**
     * 
     * @return the singleton instance.s
     */
    public static ToolkitBP getInstance() {
        if (instance == null) {
            instance = new ToolkitBP();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public void handleDataChanged(IPersistentObject po, DataState dataState, 
        UpdateState updateState) {
        
        if (po instanceof INodePO 
            && (DataState.StructureModified == dataState)) {
            
            final INodePO node = (INodePO)po;
            if (po instanceof ISpecTestCasePO) {
                UsedToolkitBP.getInstance().updateToolkitLevel(
                    node, node.getToolkitLevel());
            }
        }
    }

    /**
     * Showing Info Message if loading old project
     * @param usedToolkits toolkits used in given project
     * @return true if project can be loaded, false otherwise.
     */
    public boolean checkXMLVersion(Set<IUsedToolkitPO> usedToolkits) {
    
        final List<ToolkitPluginError> errors = UsedToolkitBP.getInstance()
            .checkUsedToolkitPluginVersions(usedToolkits);
        if (errors.isEmpty()) {
            return true;
        }
        boolean loadProject = true;
        Integer messageID = null;
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(I18n.getString("OpenProjectAction.ToolkitVersionConflict1")); //$NON-NLS-1$
        for (ToolkitPluginError error : errors) {
            String toolkitId = error.getToolkitId();
            ToolkitPluginDescriptor desc = 
                ComponentBuilder.getInstance().getCompSystem()
                .getToolkitPluginDescriptor(toolkitId);
            String toolkitName = desc != null ? desc.getName() : toolkitId;
            strBuilder.append(I18n.getString("OpenProjectAction.ToolkitVersionConflict2")) //$NON-NLS-1$
                .append(toolkitName)
                .append(I18n.getString("OpenProjectAction.ToolkitVersionConflict3")); //$NON-NLS-1$
            
            final ERROR errorType = error.getError();
            final String descr = I18n.getString("OpenProjectAction.ToolkitVersionConflict5"); //$NON-NLS-1$
            switch (errorType) {
                case MAJOR_VERSION_ERROR:
                    messageID = MessageIDs
                        .E_LOAD_PROJECT_TOOLKIT_MAJOR_VERSION_ERROR;
                    strBuilder.append(I18n.getString("OpenProjectAction.ToolkitVersionConflict4a")); //$NON-NLS-1$
                    strBuilder.append(descr);
                    loadProject = false;
                    break;
    
                case MINOR_VERSION_HIGHER:
                    messageID = MessageIDs
                        .E_LOAD_PROJECT_TOOLKIT_MAJOR_VERSION_ERROR;
                    strBuilder.append(I18n.getString("OpenProjectAction.ToolkitVersionConflict4b")); //$NON-NLS-1$
                    strBuilder.append(descr);
                    loadProject = false;
                    break;
                    
                case MINOR_VERSION_LOWER:
                    if (loadProject) { // do not overwrite if already false!
                        messageID = MessageIDs
                            .Q_LOAD_PROJECT_TOOLKIT_MINOR_VERSION_LOWER;
                    }
                    strBuilder.append(I18n.getString("OpenProjectAction.ToolkitVersionConflict4c")); //$NON-NLS-1$
                    break;
                    
                default:
                    Assert.notReached("Unknown error type: "  //$NON-NLS-1$
                        + String.valueOf(errorType));
            }
        }
        String[] details = null;
        if (!messageID.equals(MessageIDs
            .Q_LOAD_PROJECT_TOOLKIT_MINOR_VERSION_LOWER)) {
            
            details = new String[]{strBuilder.toString()};                
        }
        final Dialog dialog = Utils.createMessageDialog(messageID, null, 
            details);
        if (dialog.getReturnCode() == Window.CANCEL) {
            loadProject = false;
        }
        return loadProject;
    }

}