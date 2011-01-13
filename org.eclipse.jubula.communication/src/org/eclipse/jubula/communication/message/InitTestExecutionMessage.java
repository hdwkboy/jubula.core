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
 * This message tells the server that a test execution started
 *
 * @author BREDEX GmbH
 * @created 07.02.2006
 */

/**
 * The @-attribute comments are configuration attributes for the .NET XML
 * serializer. They are not needed by the native Java classes. They are
 * defined here because the classes are shared on source code level.
 * Due to the way the attributes are set, the property variables need to be
 * public. Since these are pure data carrying properties this is acceptable.
 *
 * @attribute System.Serializable()
 */
public class InitTestExecutionMessage extends Message {

    /** version */
    private static final double VERSION = 1.0;

/* DOTNETDECLARE:BEGIN */

    /** default activation method */
    /** @attribute System.Xml.Serialization.XmlElement("m__defaultActivationMethod") */
    public String m_defaultActivationMethod;

/* DOTNETDECLARE:END */
    

    /**
     * {@inheritDoc}
     */
    public String getCommandClass() {
        return CommandConstants.INIT_TEST_EXECUTION_COMMAND;
    }
    
    /**
     * {@inheritDoc}
     */
    public double getVersion() {
        return VERSION;
    }
    
    /**
     * @return default activation method
     */
    public String getDefaultActivationMethod() {
        return m_defaultActivationMethod;
    }
    
    /**
     * @param defaultActivationMethod default activation method
     */
    public void setDefaultActivationMethod(String defaultActivationMethod) {
        m_defaultActivationMethod = defaultActivationMethod;
    }

}