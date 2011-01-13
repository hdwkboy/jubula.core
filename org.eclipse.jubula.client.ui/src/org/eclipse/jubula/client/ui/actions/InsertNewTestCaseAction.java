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

import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
import org.eclipse.jubula.client.ui.constants.IconConstants;
import org.eclipse.jubula.client.ui.model.GuiNode;


/**
 * @author BREDEX GmbH
 * @created 27.06.2006
 */
public class InsertNewTestCaseAction extends AbstractNewTestCaseAction {
    /**
     * Constructor.
     */
    public InsertNewTestCaseAction() {
        setText(super.getText());
        setImageDescriptor(IconConstants.NEW_REF_TC_IMAGE_DESCRIPTOR); 
        setDisabledImageDescriptor(IconConstants.
                NEW_REF_TC_DISABLED_IMAGE_DESCRIPTOR);
        setEnabled(false);
        setHelpid(ContextHelpIds.DIALOG_TC_INSERT_NEW);
    }
    
    /**
     * {@inheritDoc}
     */
    protected Integer getPositionToInsert(ISpecTestCasePO workTC, 
            GuiNode selectedNodeGUI) {
        
        int positionToAdd = selectedNodeGUI.getPositionInParent();
        return positionToAdd;
    }
}