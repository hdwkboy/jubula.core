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
package org.eclipse.jubula.client.ui.rcp.controllers.propertysources;

import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


/**
 * @author BREDEX GmbH
 * @created 06.12.2005
 */
public class ProjectGUIPropertySource extends AbstractGuiNodePropertySource {

    
    /** Constant for the String TestCase Name */
    public static final String P_ELEMENT_DISPLAY_NAME =
        Messages.ProjectGUIPropertySourceProjectName;
    
    /** cached property descriptor for name */
    private IPropertyDescriptor m_namePropDesc = null;
    

    /**
     * @param guiNode the ProjectGUI
     */
    public ProjectGUIPropertySource(IProjectPO guiNode) {
        super(guiNode);
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropDescriptor() {
        if (!getPropertyDescriptorList().isEmpty()) {
            clearPropertyDescriptors();
        }
        
        // Project Name
        if (m_namePropDesc == null) {
            m_namePropDesc = new TextPropertyDescriptor(
                    new ElementNameController(), P_ELEMENT_DISPLAY_NAME);
        }
        addPropertyDescriptor(m_namePropDesc);
    }
}