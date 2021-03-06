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
package org.eclipse.jubula.client.core.model;

import java.util.Locale;
import java.util.Set;

/**
 * @author BREDEX GmbH
 * @created Jun 11, 2007
 */
public interface IProjectPropertiesPO extends IPersistentObject, ILangSupport {

    /**
     * @return <code>true</code> if this project is reusable. Otherwise
     *         <code>false</code>.
     */
    public abstract boolean getIsReusable();
    
    /**
     * @param isReusable Whether the project should be reusable.
     */
    public void setIsReusable(boolean isReusable);

    /**
     * @return <code>true</code> if this project is protected. Otherwise
     *         <code>false</code>.
     */
    public abstract boolean getIsProtected();
    
    /**
     * @param isProtected Whether the project should be protected.
     */
    public void setIsProtected(boolean isProtected);
    
    /**
     * 
     * @return the set of used projects.
     */
    public Set<IReusedProjectPO> getUsedProjects();

    /**
     * 
     * @param reusedProject The project to reuse.
     */
    public void addUsedProject(IReusedProjectPO reusedProject);
    
    /**
     * 
     * @param project The project to remove.
     */
    public void removeUsedProject(IReusedProjectPO project);

    /**
     * @return the id of the toolkit of this project
     */
    public abstract String getToolkit();
    
       
    /**
     * @param toolkit the id of the toolKit type of this project
     */
    public abstract void setToolkit(String toolkit);

    /**
     * @return Returns the defaultLanguage.
     */
    public abstract Locale getDefaultLanguage();

    /**
     * @param defaultLanguage The defaultLanguage to set.
     */
    public abstract void setDefaultLanguage(Locale defaultLanguage);

    /**
     * @return the major version number of this project
     */
    public abstract Integer getMajorNumber();

    /**
     * @return the major version number of this project
     */
    public abstract Integer getMinorNumber();

    /**
     * @return Returns the langHelper.
     */
    public abstract LanguageHelper getLangHelper();

    /**
     * Clears the reused projects set.
     */
    public abstract void clearUsedProjects();

    /**
     * @return the the number of days to clean the results for
     */
    public abstract Integer getTestResultCleanupInterval();
    
    /**
     * @param noOfDays the number of days to clean the results for
     */
    public abstract void setTestResultCleanupInterval(Integer noOfDays);
    
    /**
     * @return the check conf container
     */
    public abstract ICheckConfContPO getCheckConfCont();
}
