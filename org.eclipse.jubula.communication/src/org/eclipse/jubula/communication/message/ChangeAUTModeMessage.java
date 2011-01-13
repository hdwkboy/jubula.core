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

import java.util.SortedSet;

import org.eclipse.jubula.tools.constants.CommandConstants;


/**
 * This message is send from JubulaClient to AUTServer for changing the mode of
 * the AUTServer. <br>
 * 
 * The new mode is given in the member variable m_mode. <br>
 * Valid values are (see also the constants) <lu>
 * <li>OBJECT_MAPPING</li>
 * <li>TESTING</li>
 * </lu>
 * 
 * The AutServer responses with a AUTModeChangedMessage.
 * 
 * @author BREDEX GmbH
 * @created 23.08.2004
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
public class ChangeAUTModeMessage extends Message {
    
    /** constant for mode, in which the test is performed */
    public static final int TESTING = 1;
    
    /** constant for mode for object mapping */
    public static final int OBJECT_MAPPING = 2;

    /** constant for mode for recording */
    public static final int RECORD_MODE = 3;
    
    /** constant for mode for checking */
    public static final int CHECK_MODE = 4;

    /** Static version */
    private static final double VERSION = 1.0;
    
/* DOTNETDECLARE:BEGIN */

    /** transmitted version of this message. */
    /** @attribute System.Xml.Serialization.XmlElement("m__version") */
    public double m_version = VERSION;

    /** the mode to change to */
    /** @attribute System.Xml.Serialization.XmlElement("m__mode") */
    public int m_mode;
    
    /** modifier to map an item/record */
    /** @attribute System.Xml.Serialization.XmlElement("m__keyModifier") */
    public int m_keyModifier = 0;
    
    /** key to map an item/record */
    /** @attribute System.Xml.Serialization.XmlElement("m__key") */
    public int m_key = 0;

    /** mouse button to map an item/record */
    /** @attribute System.Xml.Serialization.XmlElement("m__mouseButton") */
    public int m_mouseButton = 0;
    
    /** modifier to record Application*/
    /** @attribute System.Xml.Serialization.XmlElement("m__key2Modifier") */
    public int m_key2Modifier = 0;
    
    /** key to record Application */
    /** @attribute System.Xml.Serialization.XmlElement("m__key2") */
    public int m_key2 = 0;

    /** modifier to start/stop check mode*/
    /** @attribute System.Xml.Serialization.XmlElement("m__checkModeKeyModifier") */
    public int m_checkModeKeyModifier = 0;
    
    /** key to start/stop check mode */
    /** @attribute System.Xml.Serialization.XmlElement("m__checkModeKey") */
    public int m_checkModeKey = 0;
    
    /** modifier for check current component */
    /** @attribute System.Xml.Serialization.XmlElement("m__checkCompKeyModifier") */
    public int m_checkCompKeyModifier = 0;
    
    /** key for check current component */
    /** @attribute System.Xml.Serialization.XmlElement("m__checkCompKey") */
    public int m_checkCompKey = 0;

    /** true if recorded actions dialog should be open, false otherwise */
    /** @attribute System.Xml.Serialization.XmlElement("m__dialogOpen") */
    public boolean m_dialogOpen;
    
    /** singleLineTrigger for Observation Mode */
    /** @attribute System.Xml.Serialization.XmlElement("m__singleLineTrigger") */
    public SortedSet m_singleLineTrigger;
    
    /** multiLineTrigger for Observation Mode */
    /** @attribute System.Xml.Serialization.XmlElement("m__multiLineTrigger") */
    public SortedSet m_multiLineTrigger;
    
    /** which toolkit is selected for this project */
    /** @attribute System.Xml.Serialization.XmlElement("m__toolkit") */
    public String m_toolkit;
    

/* DOTNETDECLARE:END */

    /** default constructor */
    public ChangeAUTModeMessage() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getCommandClass() {
        return CommandConstants.CHANGE_AUT_MODE_COMMAND;
    }

    /**
     * {@inheritDoc}
     */
    public double getVersion() {
        return m_version;
    }

    /**
     * @return Returns the mode.
     */
    public int getMode() {
        return m_mode;
    }
    
    /**
     * @param mode The mode to set.
     */
    public void setMode(int mode) {
        m_mode = mode;
    }
    
    /**
     * @return Returns the key to map an item/record.
     */
    public int getKey() {
        return m_key;
    }
    
    /**
     * @param key The key to set.
     */
    public void setKey(int key) {
        m_key = key;
    }
    
    /**
     * @return the mouse button used for object mapping
     */
    public int getMouseButton() {
        return m_mouseButton;
    }

    /**
     * @param mouseButton the mouse button to use for object mapping
     */
    public void setMouseButton(int mouseButton) {
        m_mouseButton = mouseButton;
    }

    /**
     * @return Returns the keyModifier.
     */
    public int getKeyModifier() {
        return m_keyModifier;
    }
    
    /**
     * @param keyModifier The keyModifier to set.
     */
    public void setKeyModifier(int keyModifier) {
        m_keyModifier = keyModifier;
    }
    
    /**
     * @return Returns the key for Application record
     */
    public int getKey2() {
        return m_key2;
    }

    /**
     * @param key2 The key to set.
     */
    public void setKey2(int key2) {
        m_key2 = key2;
    }

    /**
     * @return Returns the keyMod for Application component.
     */
    public int getKey2Modifier() {
        return m_key2Modifier;
    }

    /**
     * @param mod The keyMod to set.
     */
    public void setKey2Modifier(int mod) {
        m_key2Modifier = mod;
    }
    
    /**
     * @return Returns the key for checkMode
     */
    public int getCheckModeKey() {
        return m_checkModeKey;
    }

    /**
     * @param checkModeKey The checkModeKey to set.
     */
    public void setCheckModeKey(int checkModeKey) {
        m_checkModeKey = checkModeKey;
    }
    
    /**
     * @return Returns the checkModeKeyMod for checkMode.
     */
    public int getCheckModeKeyModifier() {
        return m_checkModeKeyModifier;
    }

    /**
     * @param checkModeKeyMod the checkModeKeyMod to set.
     */
    public void setCheckModeKeyModifier(int checkModeKeyMod) {
        m_checkModeKeyModifier = checkModeKeyMod;
    }
    
    /**
     * @return the checkCompKey
     */
    public int getCheckCompKey() {
        return m_checkCompKey;
    }

    /**
     * @param checkCompKey the checkCompKey to set
     */
    public void setCheckCompKey(int checkCompKey) {
        m_checkCompKey = checkCompKey;
    }

    /**
     * @return the checkCompKeyMod
     */
    public int getCheckCompKeyModifier() {
        return m_checkCompKeyModifier;
    }

    /**
     * @param checkCompKeyMod the checkCompKeyMod to set
     */
    public void setCheckCompKeyModifier(int checkCompKeyMod) {
        m_checkCompKeyModifier = checkCompKeyMod;
    }
    
    /**
     * @return true if recorded actions dialog should be open, false otherwise
     */
    public boolean getRecordDialogOpen() {
        return m_dialogOpen;
    }

    /**
     * @param dialogOpen set state of recorded actions dialog
     */
    public void setRecordDialogOpen(boolean dialogOpen) {
        m_dialogOpen = dialogOpen;
    }
    
    /**
     * @return singleLineTrigger for Observation Mode
     */
    public SortedSet getSingleLineTrigger() {
        return m_singleLineTrigger;
    }

    /**
     * @param singleLineTrigger singleLineTrigger for Observation Mode
     */
    public void setSingleLineTrigger(SortedSet singleLineTrigger) {
        m_singleLineTrigger = singleLineTrigger;
    }
    
    /**
     * @return multiLineTrigger for Observation Mode
     */
    public SortedSet getMultiLineTrigger() {
        return m_multiLineTrigger;
    }

    /**
     * @param multiLineTrigger multiLineTrigger for Observation Mode
     */
    public void setMultiLineTrigger(SortedSet multiLineTrigger) {
        m_multiLineTrigger = multiLineTrigger;
    }

    /**
     * @return the toolkit
     */
    public String getToolkit() {
        return m_toolkit;
    }

    /**
     * @param toolkit the toolkit to set
     */
    public void setToolkit(String toolkit) {
        m_toolkit = toolkit;
    }
}