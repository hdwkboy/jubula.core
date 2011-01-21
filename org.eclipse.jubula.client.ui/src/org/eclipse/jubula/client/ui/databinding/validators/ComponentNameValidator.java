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
package org.eclipse.jubula.client.ui.databinding.validators;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
import org.eclipse.jubula.client.ui.i18n.Messages;
import org.eclipse.jubula.tools.constants.StringConstants;


/**
 * Validates a component name.
 *
 * @author BREDEX GmbH
 * @created Jan 12, 2009
 */
public class ComponentNameValidator implements IValidator {

    /** the mapper used for finding and resolving component names */
    private IComponentNameMapper m_compNamesMapper;
    /** support for rename */
    private String m_oldName;

    /**
     * Constructor
     * 
     * @param compNamesMapper 
     *          The mapper used for finding and resolving component names.
     * @param oldName if not null allow this name the support rename
     */
    public ComponentNameValidator(IComponentNameMapper compNamesMapper, 
            String oldName) {
        m_compNamesMapper = compNamesMapper;
        m_oldName = oldName;
    }
    
    /**
     * {@inheritDoc}
     */
    public IStatus validate(Object value) {
        String stringValue = value.toString();
        return isValidComponentName(stringValue);
    }

    /**
     * @param stringValue component name
     * @return IStatus.OK if this is a valid component name, error status
     * otherwise
     */
    public IStatus isValidComponentName(String stringValue) {
        IStatus is = isValidComponentNameString(stringValue);
        if (!is.isOK()) {
            return is;
        }
        if ((m_compNamesMapper.getCompNameCache()
                .getGuidForName(stringValue) == null)
                || stringValue.equals(m_oldName)) {

            return ValidationStatus.ok();
        }

        return ValidationStatus.error(
                Messages.LogicalNameErrorExists);
    }

    /**
     * @param stringValue name to check
     * @return IStatus.OK if this is a valid component name, error status
     * otherwise
     */
    public static IStatus isValidComponentNameString(String stringValue) {
        if (stringValue.trim().length() == 0) {
            return ValidationStatus.error(
                    Messages.LogicalNameErrorEmpty);
        }
        if (stringValue.startsWith(StringConstants.SPACE)
            || stringValue.charAt(
                    stringValue.length() - 1) == ' ') {

            return ValidationStatus.error(
                    Messages.LogicalNameErrorNoSpaceAtStartOrEnd);
        }
        for (char ch : stringValue.toCharArray()) {
            if (Character.isISOControl(ch)) {
                return ValidationStatus.error(
                        Messages.LogicalNameErrorInvalidChar);
            }
        }
        return ValidationStatus.ok();
    }

}
