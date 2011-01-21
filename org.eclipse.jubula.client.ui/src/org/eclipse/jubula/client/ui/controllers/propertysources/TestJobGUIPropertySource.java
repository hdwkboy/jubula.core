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
package org.eclipse.jubula.client.ui.controllers.propertysources;

import org.eclipse.jubula.client.ui.i18n.Messages;
import org.eclipse.jubula.client.ui.model.TestJobGUI;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


/**
 * @author BREDEX GmbH
 * @created Mar 16, 2010
 */
public class TestJobGUIPropertySource extends AbstractGuiNodePropertySource {
    /** Constant for the String Specification Name */
    private static final String P_JOBNAME_DISPLAY_NAME = 
        Messages.TestJobGUIPropertySourceTestJobName;

    /** cached property descriptor for name */
    private IPropertyDescriptor m_namePropDesc = null;
    
    /**
     * @param testJobGui
     *            the test job gui node
     */
    public TestJobGUIPropertySource(TestJobGUI testJobGui) {
        super(testJobGui);
        initPropDescriptor();
    }

    /**
     * Inits the PropertyDescriptors and adds them into super.m_propDescriptors.
     * 
     */
    protected void initPropDescriptor() {
        if (!getPropertyDescriptorList().isEmpty()) {
            clearPropertyDescriptors();
        }
        
        // name
        if (m_namePropDesc == null) {
            m_namePropDesc = new TextPropertyDescriptor(
                    new ElementNameController(), P_JOBNAME_DISPLAY_NAME);
        }
        addPropertyDescriptor(m_namePropDesc);

        super.initPropDescriptor();
    }
}
