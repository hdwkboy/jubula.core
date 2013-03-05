/*******************************************************************************
 * Copyright (c) 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation 
 *******************************************************************************/
package org.eclipse.jubula.rc.swt.tester.adapter;

import org.eclipse.jubula.rc.common.driver.IRunnable;
import org.eclipse.jubula.rc.common.exception.StepExecutionException;
import org.eclipse.jubula.rc.common.logger.AutServerLogger;
import org.eclipse.jubula.rc.common.tester.adapter.interfaces.ITextInputComponent;
import org.eclipse.jubula.rc.swt.tester.CAPUtil;
import org.eclipse.jubula.tools.utils.EnvironmentUtils;
import org.eclipse.swt.widgets.Text;

/**
 * Implementation of the Interface <code>ITextComponentAdapter</code> as a
 * adapter for the <code>Text</code> component.
 * @author BREDEX GmbH
 *
 */
public class TextComponentAdapter extends ControlAdapter implements
        ITextInputComponent {
    
    /** the logger */
    private static AutServerLogger log = new AutServerLogger(
        TextComponentAdapter.class);
    
    /** */
    private Text m_textComponent;
    /**
     * 
     * @param objectToAdapt 
     */
    public TextComponentAdapter(Object objectToAdapt) {
        super(objectToAdapt);
        m_textComponent = (Text) objectToAdapt;
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        String actual = (String)getEventThreadQueuer().invokeAndWait(
                "getText", new IRunnable() { //$NON-NLS-1$
                    public Object run() {
                        return CAPUtil.getWidgetText(m_textComponent,
                                m_textComponent.getText());
                    }
                });
        return actual;
    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(final int start) {
        getEventThreadQueuer().invokeAndWait("setSelection", //$NON-NLS-1$
                new IRunnable() {
                    public Object run() {
                        m_textComponent.setSelection(start);
                        return null;
                    }
                });

    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(final int start, final int end) {
        getEventThreadQueuer().invokeAndWait("setSelection", //$NON-NLS-1$
                new IRunnable() {
                    public Object run() {
                        m_textComponent.setSelection(start, end);
                        return null;
                    }
                });

    }
    
    /**
     * {@inheritDoc}
     */
    public String getSelectionText() {
        String actual = (String)getEventThreadQueuer().invokeAndWait(
                "getSelectionText", new IRunnable() { //$NON-NLS-1$
                    public Object run() {
                        return m_textComponent.getSelectionText();
                    }
                });
        return actual;
    }

    /**
     * {@inheritDoc}
     */
    public void selectAll() {
        final String totalText = getText();
        
        // fix for https://bxapps.bredex.de/bugzilla/show_bug.cgi?id=201
        // The keystroke "command + a" sometimes causes an "a" to be entered
        // into the text field instead of selecting all text (or having no 
        // effect).
        if (!EnvironmentUtils.isMacOS()) {
            try {
                getRobot().keyStroke(getRobot().getSystemModifierSpec() + " A"); //$NON-NLS-1$
            } catch (StepExecutionException see) {
                /*This might happen under certain circumstances e.g. on MacOS X see
              bug 342691 */ 
                log.warn(see);
            }
        }
        
        if (!totalText.equals(getSelectionText())) {
            // the selection failed for some reason
            getEventThreadQueuer().invokeAndWait("text.selectAll", //$NON-NLS-1$
                    new IRunnable() {
                        public Object run() {
                            m_textComponent.selectAll();
                            return null;
                        }
                    });
        }
        
        String selectionText = getSelectionText();
        if (!totalText.equals(selectionText)) {
            log.warn("SelectAll failed!\n"  //$NON-NLS-1$
                + "Total text: '" + totalText + "'\n"   //$NON-NLS-1$//$NON-NLS-2$
                + "Selected text: '" + selectionText + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditable() {
        return ((Boolean)getEventThreadQueuer().invokeAndWait(
                "isEditable", //$NON-NLS-1$
                new IRunnable() {
                public Object run() {
                    return m_textComponent.getEditable() 
                        && m_textComponent.getEnabled()
                            ? Boolean.TRUE : Boolean.FALSE; // see findBugs
                }
            })).booleanValue();
    }

}
