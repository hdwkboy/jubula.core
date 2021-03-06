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
package org.eclipse.jubula.client.core.businessprocess;

import java.util.EventListener;

/**
 * @author BREDEX GmbH
 * @created Mar 16, 2010
 */
public interface ITestresultSummaryEventListener extends EventListener {
    /**
     * This method is called when the state of testresults changes.
     */
    public void testresultSummaryChanged();


}
