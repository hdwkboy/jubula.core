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
package org.eclipse.jubula.communication.message;

import org.eclipse.jubula.tools.constants.CommandConstants;

/**
 * The message send from server to the client, transmitting the state of the
 * stop process of the AUTServer. <br>
 * OK: AUTServer stopped successfully.
 * 
 * @author BREDEX GmbH
 * @created 18.12.2007
 */
public class StopAUTServerStateMessage extends Message {
    /** empty default constructor */
    public StopAUTServerStateMessage() {
        super();
    }

    /** {@inheritDoc} */
    public String getCommandClass() {
        return CommandConstants.STOP_AUT_SERVER_STATE_COMMAND;
    }
}