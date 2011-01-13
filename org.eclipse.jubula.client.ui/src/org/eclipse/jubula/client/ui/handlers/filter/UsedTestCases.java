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
package org.eclipse.jubula.client.ui.handlers.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jubula.client.ui.handlers.filter.testcases.FilterUsedTestCases;
import org.eclipse.jubula.client.ui.views.TestCaseBrowser;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * @author BREDEX GmbH
 * @created 03.07.2009
 */
public class UsedTestCases extends AbstractHandler {

    /**
     * {@inheritDoc}
     */
    public Object execute(ExecutionEvent event) {
        IWorkbenchPart ap = HandlerUtil.getActivePart(event);
        if (ap instanceof TestCaseBrowser) {
            TestCaseBrowser tcb = (TestCaseBrowser)ap;
            boolean filterAlreadyActive = false;
            for (ViewerFilter vf : tcb.getTreeViewer().getFilters()) {
                if (vf instanceof FilterUsedTestCases) {
                    tcb.getTreeViewer().removeFilter(vf);
                    filterAlreadyActive = true;
                }
            }
            if (!filterAlreadyActive) {
                tcb.getTreeViewer().addFilter(new FilterUsedTestCases());
            }
        }
        return null;
    }
}