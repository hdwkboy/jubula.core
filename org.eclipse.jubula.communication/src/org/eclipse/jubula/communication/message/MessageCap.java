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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jubula.tools.objects.IComponentIdentifier;
import org.eclipse.jubula.tools.xml.businessmodell.Action;

/**
 * @author BREDEX GmbH
 * @created 14.10.2004
 */
public class MessageCap {
    /** A list of parameters */
    private List m_messageParams;

    /**
     * The ComponentIdentifier that hold the technical name and the componentType
     */
    private IComponentIdentifier m_ci;

    /** The method of the implementation class that shall be invoked */
    private String m_method;

    /** Action of CAP */
    private Action m_action;

    /** boolean to decide, if CAP is from old recorded Web Observ-Mode */
    private boolean m_isOldWebObserv = false;

    /** the resolved(possibly overwritten) logical name */
    private String m_resolvedLogicalName;

    /**
     * full qualified name of the command class to execute after test execution
     * in TestExecution
     */
    private String m_postExecutionCommand;

    /** The LogicalName of CAP */
    private String m_logicalName;

    /** The Additional Message of CAP for Observation Console */
    private String m_extraMsg;

    /** The default constructor */
    public MessageCap() {
        super();
        m_messageParams = new ArrayList();
    }

    /**
     * Adds an messageParam to the list of parameters
     * 
     * @param messageParam
     *            A message for sending to server
     */
    public void addMessageParam(MessageParam messageParam) {
        m_messageParams.add(messageParam);
    }

    /** @return Returns the ci. */
    public IComponentIdentifier getCi() {
        return m_ci;
    }

    /**
     * @param ci
     *            The ci to set.
     */
    public void setCi(IComponentIdentifier ci) {
        m_ci = ci;
    }

    /** @return Returns the messageParams. */
    public List getMessageParams() {
        return m_messageParams;
    }

    /** @return Returns the method. */
    public String getMethod() {
        return m_method;
    }

    /**
     * @param method
     *            The method to set.
     */
    public void setMethod(String method) {
        m_method = method;
    }

    /** @return Returns the Action. */
    public Action getAction() {
        return m_action;
    }

    /**
     * @param isOldWebObserv
     *            set to true, if recorded CAP is from old Web-ObservationMode
     */
    public void setWebObservOld(boolean isOldWebObserv) {
        m_isOldWebObserv = isOldWebObserv;
    }

    /**
     * @return Returns true, if recorded CAP is from old Web-ObservationMode
     */
    public boolean isWebObservOld() {
        return m_isOldWebObserv;
    }

    /**
     * @param action
     *            The Action to set.
     */
    public void setAction(Action action) {
        m_action = action;
    }

    /** @return the resolved logical name */
    public String getResolvedLogicalName() {
        return m_resolvedLogicalName;
    }

    /**
     * @param resolvedLogicalName
     *            String
     */
    public void setResolvedLogicalName(String resolvedLogicalName) {
        m_resolvedLogicalName = resolvedLogicalName;
    }

    /**
     * Gets the CAP logical name
     * 
     * @return The logical name
     */
    public String getLogicalName() {
        return m_logicalName;
    }

    /**
     * Sets the CAP logical name
     * 
     * @param logicalName
     *            the logical name
     */
    public void setLogicalName(String logicalName) {
        m_logicalName = logicalName;
    }

    /**
     * Gets the CAP additional Message for Observation Console
     * 
     * @return the extra Message
     */
    public String getExtraMessage() {
        return m_extraMsg;
    }

    /**
     * Sets the CAP additional Message for Observation Console
     * 
     * @param extraMsg
     *            the extra Message
     */
    public void setExtraMessage(String extraMsg) {
        m_extraMsg = extraMsg;
    }

    /** @return Returns the postExecutionCommand. */
    public String getPostExecutionCommand() {
        return m_postExecutionCommand;
    }

    /**
     * @param postExecutionCommand
     *            The postExecutionCommand to set.
     */
    public void setPostExecutionCommand(String postExecutionCommand) {
        m_postExecutionCommand = postExecutionCommand;
    }
}