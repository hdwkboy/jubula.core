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
package org.eclipse.jubula.client.ui.rcp.propertytester;

import org.eclipse.jubula.client.core.model.IComponentNamePO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.ui.propertytester.AbstractBooleanPropertyTester;
import org.eclipse.jubula.client.ui.rcp.businessprocess.ComponentNameReuseBP;
import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;


/**
 * PropertyTester for Component Names.
 *
 * @author BREDEX GmbH
 * @created Mar 5, 2009
 */
public class ComponentNamePropertyTester extends AbstractBooleanPropertyTester {

    /** the id of the "isBeingUsed" property */
    public static final String IS_BEING_USED_PROP = "isBeingUsed"; //$NON-NLS-1$
    
    /** the id of the "isDefaultMapping" property */
    public static final String IS_DEFAULT_MAPPING_PROP = "isDefaultMapping"; //$NON-NLS-1$

    /**
     * <code>PROPERTIES</code>
     */
    private static final String[] PROPERTIES = new String[] { 
        IS_BEING_USED_PROP, IS_DEFAULT_MAPPING_PROP };

    /** {@inheritDoc} */
    public boolean testImpl(Object receiver, String property, Object[] args) {
        IComponentNamePO compName = (IComponentNamePO)receiver;
        if (property.equals(IS_BEING_USED_PROP)) {
            return testIsBeingUsed(compName);
        }
        if (property.equals(IS_DEFAULT_MAPPING_PROP)) {
            return testIsDefaultMapping(compName);
        }
        return false;
    }

    /**
     * 
     * @param compName The Component Name to test.
     * @return <code>true</code> if the Component Name is somehow in use
     *         within the current Project. Otherwise <code>false</code>.
     */
    private boolean testIsBeingUsed(IComponentNamePO compName) {
        IProjectPO currentProject = GeneralStorage.getInstance().getProject();
        
        if (currentProject != null) {
            return ComponentNameReuseBP.getInstance().isCompNameReused(
                    compName.getGuid());
        }
        return true;
    }

    /**
     * @param compName The Component Name to test.
     * @return <code>true</code> if the Component Name is part of a default 
     *         mapping (ex. Menu, Application). Otherwise <code>false</code>.
     */
    private boolean testIsDefaultMapping(IComponentNamePO compName) {

        String name = compName.getName();
        CompSystem compSystem = ComponentBuilder.getInstance().getCompSystem();
        return compSystem.getDefaultMappingNames().containsKey(name);
        
    }

    /** {@inheritDoc} */
    public Class<? extends Object> getType() {
        return IComponentNamePO.class;
    }

    /** {@inheritDoc} */
    public String[] getProperties() {
        return PROPERTIES;
    }
}
