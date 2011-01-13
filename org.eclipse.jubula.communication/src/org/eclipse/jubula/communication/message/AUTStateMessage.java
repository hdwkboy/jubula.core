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


/**
 * This message is used to transmit the state of the AUT, see the constants. <br>
 * It's the response message to the AUTStartMessage.
 *
 * @author BREDEX GmbH
 * @created 12.08.2004
 */

/**
 * The @-attribute comments are configuration attributes for the .NET XML
 * serializer. They are not needed by the native Java classes. They are
 * defined here because the classes are shared on source code level.
 * Due to the way the attributes are set, the property variables need to be
 * public. Since these are pure data carrying properties this is acceptable.
 * 
 * @attribute System.Serializable()
 * */
public class AUTStateMessage extends Message {
    /** state signaling that the AUT is running */
    public static final int RUNNING = 1;
    
    /** state signaling that the AUT could not started */
    public static final int START_FAILED = 2;
    
    /** static version */
    private static final double VERSION = 1.0;
    
    /** Name of the command class */
    private static final String COMMAND_CLASS  = 
        "org.eclipse.jubula.client.core.commands.AUTStateCommand"; //$NON-NLS-1$
    
/* DOTNETDECLARE:BEGIN */

    /** transmitted version of this message. */
    /** @attribute System.Xml.Serialization.XmlElement("m__version") */
    public double m_version = VERSION;

    /** the state of the aut, see constants */
    /** @attribute System.Xml.Serialization.XmlElement("m__state") */
    public int m_state;
    
    /** a short description in case of an error (actually only the state AUT_START_FAILED) */
    /** @attribute System.Xml.Serialization.XmlElement("m__description") */
    public String m_description;

/* DOTNETDECLARE:END */
    
    /**
     * public default constructor
     */
    public AUTStateMessage() {
        super();
    }

    /**
     * constructor with parameter for the state, use the defined constants *
     * @param state the state the aut is in
     */
    public AUTStateMessage(int state) {
        this();
        m_state = state;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandClass() {
        return COMMAND_CLASS;
    }

    /**
     * {@inheritDoc}
     */
    public double getVersion() {
        return m_version;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return m_description;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        m_description = description;
    }
    
    /**
     * @return Returns the state.
     */
    public int getState() {
        return m_state;
    }
    
    /**
     * @param state The state to set.
     */
    public void setState(int state) {
        m_state = state;
    }
}