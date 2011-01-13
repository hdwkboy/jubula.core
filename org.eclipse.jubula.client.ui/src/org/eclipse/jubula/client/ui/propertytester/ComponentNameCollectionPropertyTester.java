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
package org.eclipse.jubula.client.ui.propertytester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jubula.client.core.model.IAUTMainPO;
import org.eclipse.jubula.client.core.model.IComponentNamePO;
import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.editors.ObjectMappingMultiPageEditor;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.jubula.tools.objects.IComponentIdentifier;
import org.eclipse.ui.IWorkbenchPart;


/**
 * PropertyTester for collections of Component Names.
 *
 * @author BREDEX GmbH
 * @created Mar 2, 2009
 */
public class ComponentNameCollectionPropertyTester extends PropertyTester {

    /** the id of the "areSameType" property */
    public static final String ARE_SAME_TYPE_PROP = "areSameType"; //$NON-NLS-1$

    /** the id of the "areSameTech" property */
    public static final String ARE_SAME_TECH_PROP = "areSameTech"; //$NON-NLS-1$

    /** the id of the "areInCurrentProject" property */
    public static final String ARE_IN_CURRENT_PROJECT = "areInCurrentProject"; //$NON-NLS-1$

    /** the logger */
    private static final Log LOG = 
        LogFactory.getLog(ComponentNameCollectionPropertyTester.class);

    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {

        if (receiver instanceof Collection) {
            Collection collection = (Collection)receiver;
            List<IComponentNamePO> compNames = 
                new ArrayList<IComponentNamePO>();
            for (Object element : collection) {
                if (element instanceof IComponentNamePO) {
                    compNames.add((IComponentNamePO)element);
                } else {
                    LOG.warn(I18n.getString(
                            "PropertyTester.TypeNotSupported",  //$NON-NLS-1$
                            new String [] {element.getClass().getName()}));
                    return false;
                }
            }
            if (property.equals(ARE_SAME_TYPE_PROP)) {
                boolean areSameType = testAreSameType(compNames);
                boolean expectedBoolean = expectedValue instanceof Boolean 
                    ? ((Boolean)expectedValue).booleanValue() : true;
                return areSameType == expectedBoolean;
            } else if (property.equals(ARE_SAME_TECH_PROP)) {
                boolean areSameTech = 
                    testAreMappedToSameTechnicalNames(compNames);
                boolean expectedBoolean = expectedValue instanceof Boolean 
                    ? ((Boolean)expectedValue).booleanValue() : true;
                return areSameTech == expectedBoolean;
            } else if (property.equals(ARE_IN_CURRENT_PROJECT)) {
                boolean areInCurrentProject = 
                    testAreInCurrentProject(compNames);
                boolean expectedBoolean = expectedValue instanceof Boolean 
                    ? ((Boolean)expectedValue).booleanValue() : true;
                return areInCurrentProject == expectedBoolean;
            }

            LOG.warn(I18n.getString(
                    "PropertyTester.PropertyNotSupported",  //$NON-NLS-1$
                    new String [] {property}));
            return false;
        }

        String receiverClass = 
            receiver != null ? receiver.getClass().getName() : "null"; //$NON-NLS-1$
        LOG.warn(I18n.getString(
                "PropertyTester.TypeNotSupported",  //$NON-NLS-1$
                new String [] {receiverClass}));
        return false;
    }

    /**
     * 
     * @param compNames The Component Names to test.
     * @return <code>true</code> if all of the types for the given 
     *         Component Names are are the same. Otherwise <code>false</code>.
     */
    private boolean testAreSameType(
            Collection<IComponentNamePO> compNames) {

        String type = null;
        
        for (IComponentNamePO compName : compNames) {
            if (type == null) {
                type = compName.getComponentType();
            } else if (!type.equals(compName.getComponentType())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 
     * @param compNames The Component Names to test.
     * @return <code>false</code> if any of the given Component Names mapped 
     *         to different Technical Names within the same AUT for any AUT in 
     *         the current Project. Otherwise, <code>true</code>.
     */
    private boolean testAreMappedToSameTechnicalNames(
            Collection<IComponentNamePO> compNames) {
        
        for (IAUTMainPO aut : getAuts()) {
            IComponentIdentifier technicalName = null;
            for (IComponentNamePO compName : compNames) {
                for (IObjectMappingAssoziationPO assoc 
                        : aut.getObjMap().getMappings()) {
                    if (assoc.getLogicalNames().contains(
                            compName.getGuid())) {
                        if (technicalName == null) {
                            technicalName = assoc.getTechnicalName();
                        } else if (assoc.getTechnicalName() != null
                                && !assoc.getTechnicalName().equals(
                                        technicalName)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * 
     * @param compNames The Component Names to test.
     * @return <code>true</code> if the all of the given Component Names 
     *         belong to the currently open Project. Otherwise 
     *         <code>false</code>.
     */
    private boolean testAreInCurrentProject(
            Collection<IComponentNamePO> compNames) {
        
        IProjectPO currentProject = GeneralStorage.getInstance().getProject();
        if (currentProject != null) {
            Long projectId = currentProject.getId();
            for (IComponentNamePO compName : compNames) {
                if (!projectId.equals(compName.getParentProjectId())) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }

    /**
     * 
     * @return a collection containing:
     *              1. the AUT for the currently active Object Mapping Editor 
     *                 (if any), and
     *              2. all other AUTs for the current Project. 
     */
    private Set<IAUTMainPO> getAuts() {
        Set<IAUTMainPO> returnSet = new HashSet<IAUTMainPO>();
        IWorkbenchPart activeEditor = Plugin.getActivePart();
        if (activeEditor instanceof ObjectMappingMultiPageEditor) {
            ObjectMappingMultiPageEditor omEditor = 
                (ObjectMappingMultiPageEditor)activeEditor;
            returnSet.add(omEditor.getAut());
            
        }

        IProjectPO currentProject = GeneralStorage.getInstance().getProject();
        if (currentProject != null) {
            returnSet.addAll(currentProject.getAutMainList());
        }
        return returnSet;
    }
}