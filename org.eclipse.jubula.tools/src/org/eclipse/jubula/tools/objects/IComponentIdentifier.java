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
package org.eclipse.jubula.tools.objects;

import java.util.List;

/**
 * @author BREDEX GmbH
 * @created 20.12.2005
 */
public interface IComponentIdentifier {

    /**
     * @return Returns the componentClassName.
     */
    public abstract String getComponentClassName();

    /**
     * @param componentClassName The componentClassName to set.
     */
    public abstract void setComponentClassName(String componentClassName);

    /**
     * @return the name of the component
     */
    public abstract String getComponentName();

    /**
     * @param hierarchyNames
     *            The hierarchyNames to set. if null, the list will be cleared.
     */
    public abstract void setHierarchyNames(List hierarchyNames);

    // getter and settor for serialisation 
    /**
     * @return Returns the hierarchyNames.
     */
    public abstract List getHierarchyNames();

    /**
     * @param hierarchyNames The hierarchyNames to add.
     */
    public abstract void addHierarchyName(String hierarchyNames);

    /**
     * {@inheritDoc}
     */
    public abstract String toString();

    /**
     * clears the hierarchynames list
     *
     */
    public abstract void clearHierarchyNames();

    /**
     * @return Clone of object
     */
    public abstract IComponentIdentifier makeClone();

    /**
     * @return Returns the supportedClassName.
     */
    public abstract String getSupportedClassName();

    /**
     * @param supportedClassName The supportedClassName to set.
     */
    public abstract void setSupportedClassName(String supportedClassName);

    /**
     * @return Returns the neighbours.
     */
    public abstract List getNeighbours();

    /**
     * @param neighbours The neighbours to set.
     */
    public abstract void setNeighbours(List neighbours);

    /**
     * @param neighbours The hierarchyNames to add.
     */
    public abstract void addNeighbour(String neighbours);

    /**
     * generates a name for the component
     * @return String
     */
    public abstract String generateLogicalName();

    /**
     * @return the alternative name for display of the component or null if the 
     *         normal name returned by method getComponentName() has to be used 
     *         as display name
     */
    public String getAlternativeDisplayName();

    /**
     * @param alternativeDisplayName the alternative name for display of the 
     *        component or null if the normal name returned by method 
     *        getComponentName() has to be used as display name
     */
    public void setAlternativeDisplayName(String alternativeDisplayName);

    /**
     * @return the name for display of the component
     */
    public String getComponentNameToDisplay();
    
    /**
     * @param matchPercentage the matchPercentage to set
     */
    public void setMatchPercentage(double matchPercentage);

    /**
     * @return the matchPercentage
     */
    public double getMatchPercentage();

    /**
     * @param numberOfOtherMatchingComponents the numberOfOtherMatchingComponents to set
     */
    public void setNumberOfOtherMatchingComponents(
            int numberOfOtherMatchingComponents);

    /**
     * @return the numberOfOtherMatchingComponents
     */
    public int getNumberOfOtherMatchingComponents();

    /**
     * @return whether this component identifier could be used to retrieve the
     *         original component on collection
     */
    public boolean isEqualOriginalFound();
    
    /**
     * @param equalOriginalFound the equalOriginalFound to set
     */
    public void setEqualOriginalFound(boolean equalOriginalFound);
}