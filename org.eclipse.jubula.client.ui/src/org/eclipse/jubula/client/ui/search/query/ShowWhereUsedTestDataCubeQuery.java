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
package org.eclipse.jubula.client.ui.search.query;

import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jubula.client.core.businessprocess.TestDataCubeBP;
import org.eclipse.jubula.client.core.model.INodePO;
import org.eclipse.jubula.client.core.model.ITestDataCubePO;
import org.eclipse.jubula.tools.i18n.I18n;


/**
 * @author BREDEX GmbH
 * @created Jul 27, 2010
 */
public class ShowWhereUsedTestDataCubeQuery extends AbstractShowWhereUsedQuery {
    /**
     * <code>m_testDataCube</code>
     */
    private ITestDataCubePO m_testDataCube;
    
    /**
     * @param tdc the test data cube to use for this query
     */
    public ShowWhereUsedTestDataCubeQuery(ITestDataCubePO tdc) {
        setTestDataCube(tdc);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("nls")
    public String getLabel() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTimestamp());
        sb.append(": ");
        sb.append(I18n.getString("UIJob.searchingTestDataCube"));
        sb.append(" \"");
        sb.append(getTestDataCube().getName());
        sb.append("\"");
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public IStatus run(IProgressMonitor monitor) {
        calculateUseOfTestDataCube(getTestDataCube(), monitor);
        return Status.OK_STATUS;
    }

    /**
     * @param tdc
     *            the test data cube to search for
     * @param monitor
     *            the progress monitor
     */
    protected void calculateUseOfTestDataCube(ITestDataCubePO tdc, 
        IProgressMonitor monitor) {
        setSearchResult(getSearchResultList(new HashSet<INodePO>(
                TestDataCubeBP.getReuser(tdc)), null));
        monitor.done();
    }

    /**
     * @param testDataCube the testDataCube to set
     */
    private void setTestDataCube(ITestDataCubePO testDataCube) {
        m_testDataCube = testDataCube;
    }

    /**
     * @return the testDataCube
     */
    private ITestDataCubePO getTestDataCube() {
        return m_testDataCube;
    }
}
