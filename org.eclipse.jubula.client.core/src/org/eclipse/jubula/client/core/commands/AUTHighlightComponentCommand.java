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

import org.eclipse.jubula.client.core.IAUTEventListener;
import org.eclipse.jubula.communication.ICommand;
import org.eclipse.jubula.communication.message.AUTHighlightComponentResponseMessage;
import org.eclipse.jubula.communication.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The command object for ChangeAUTModeMessage. <br>
 * The execute() method enables the <code>mode</code> and returns a
 * AUTModeChangedMessage.
 * 
 * @author BREDEX GmbH
 * @created 09.08.2005
 * 
 */
public class AUTHighlightComponentCommand implements ICommand {
    /** the logger */
    private static Logger log = 
        LoggerFactory.getLogger(AUTHighlightComponentCommand.class);

    /** the message */
    private AUTHighlightComponentResponseMessage m_message;
    
    /**
     * Listener from GUI
     */
    private IAUTEventListener m_listener = null;
    
    
    /**
     * constructor
     * @param list 
     *      Listener
     */
    public AUTHighlightComponentCommand(IAUTEventListener list) {
        m_listener = list;
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
        m_message = (AUTHighlightComponentResponseMessage) message;

    }

    /**
     * Changes the mode aof the AUTServer to the mode taken from the message.
     * Returns an AUTModeChangedMessage with the new mode.
     * 
     * {@inheritDoc}
     */
    public Message execute() {
        if (!m_message.isVerified()) {
            m_listener.stateChanged(null);
        }
        return null;
    }

    /** 
     * {@inheritDoc}
     */
    public void timeout() {
        log.error(this.getClass().getName() + "timeout() called"); //$NON-NLS-1$
    }

}
