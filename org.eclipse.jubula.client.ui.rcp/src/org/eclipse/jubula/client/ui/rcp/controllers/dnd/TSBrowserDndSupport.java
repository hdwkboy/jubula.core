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
package org.eclipse.jubula.client.ui.rcp.controllers.dnd;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jubula.client.core.businessprocess.db.NodeBP;
import org.eclipse.jubula.client.core.model.ICategoryPO;
import org.eclipse.jubula.client.core.model.IExecObjContPO;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.IPersistentObject;
import org.eclipse.jubula.client.core.model.ITestJobPO;
import org.eclipse.jubula.client.core.model.ITestSuitePO;
import org.eclipse.jubula.client.core.persistence.PMException;
import org.eclipse.jubula.client.ui.rcp.views.TestSuiteBrowser;
import org.eclipse.jubula.tools.exception.ProjectDeletedException;


/**
 * Utility class containing methods for use in drag and drop as well as 
 * cut and paste operations in the Test Case Browser.
 *
 * @author BREDEX GmbH
 * @created 19.10.2011
 */
public class TSBrowserDndSupport extends AbstractBrowserDndSupport {

    /**
     * Private constructor
     */
    private TSBrowserDndSupport() {
        // Do nothing
    }

    /**
     * Checks whether the nodes in the given selection can legally be moved
     * to the given target location.
     * 
     * @param selection The selection to check.
     * @param target The target location to check.
     * @return <code>true</code> if the move is legal. Otherwise, 
     *         <code>false</code>.
     */
    public static boolean canMove(
            IStructuredSelection selection, Object target) {
        Iterator iter = selection.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            // check the object to drag
            if ((!(obj instanceof ITestSuitePO) 
                    && !(obj instanceof ICategoryPO)
                    && !(obj instanceof ITestJobPO))
                || (obj instanceof INodePO 
                    && !NodeBP.isEditable((IPersistentObject)obj))) {
                return false;
            }
            // check the object to drop on (target)
            if (!(target instanceof ICategoryPO
                    || target instanceof IExecObjContPO)
                    || (target instanceof INodePO
                            && !NodeBP.isEditable((IPersistentObject)target))) {
                return false;
            }
            if (target instanceof INodePO
                    && ((INodePO) obj)
                            .hasCircularDependences(((INodePO) target))) {
                return false;
            }
        }
        return true;

    }
    
    /**
     * Moves the given nodes to the given target location.
     * 
     * @param nodesToBeMoved The nodes to move.
     * @param target The target location.
     */
    public static void moveNodes(List<INodePO> nodesToBeMoved,
            IPersistentObject target) throws PMException,
            ProjectDeletedException {
        if (TestSuiteBrowser.getInstance() != null) {
            doMove(nodesToBeMoved, target);
        }
    }

}
