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
package org.eclipse.jubula.client.core.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jubula.client.core.IServerLogListener;
import org.eclipse.jubula.client.core.i18n.Messages;
import org.eclipse.jubula.communication.ICommand;
import org.eclipse.jubula.communication.message.Message;
import org.eclipse.jubula.communication.message.ServerLogResponseMessage;
import org.eclipse.jubula.tools.constants.StringConstants;


/**
 * @author BREDEX GmbH
 * @created Feb 8, 2007
 * 
 */
public class ServerLogResponseCommand implements ICommand {

    /** the logger */
    private static Logger log = 
        LoggerFactory.getLogger(ServerLogResponseCommand.class);

    /** the message */
    private ServerLogResponseMessage m_message;

    /**
     * Listener from GUI
     */
    private IServerLogListener m_listener = null;
    
    /**
     * constructor
     * @param list 
     *      Listener
     */
    public ServerLogResponseCommand(IServerLogListener list) {
        m_listener = list;
    }

    /**
     * {@inheritDoc}
     */
    public Message execute() {
        m_listener.processServerLog(m_message);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Message getMessage() {
        return m_message;
    }

    /**
     * {@inheritDoc}
     */
    public void setMessage(Message message) {
        m_message = (ServerLogResponseMessage) message;

    }

    /**
     * {@inheritDoc}
     */
    public void timeout() {
        log.error(this.getClass().getName() + StringConstants.DOT
                + Messages.TimeoutCalled);
    }

}
