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
package org.eclipse.jubula.client.ui.rcp.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jubula.client.core.businessprocess.AbstractParamInterfaceBP;
import org.eclipse.jubula.client.core.businessprocess.CapBP;
import org.eclipse.jubula.client.core.model.ICapPO;
import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
import org.eclipse.jubula.client.core.model.IParamNodePO;
import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
import org.eclipse.jubula.client.core.model.ITestDataCubePO;
import org.eclipse.jubula.client.core.utils.IParamValueValidator;
import org.eclipse.jubula.client.core.utils.StringHelper;
import org.eclipse.jubula.client.ui.rcp.controllers.propertydescriptors.ParamComboPropertyDescriptor;
import org.eclipse.jubula.client.ui.rcp.controllers.propertydescriptors.ParamTextPropertyDescriptor;
import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.AbstractGuiNodePropertySource.AbstractParamValueController;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.client.ui.rcp.widgets.CheckedParamText;
import org.eclipse.jubula.client.ui.rcp.widgets.CheckedParamTextContentAssisted;
import org.eclipse.jubula.client.ui.rcp.widgets.FunctionProposalProvider;
import org.eclipse.jubula.client.ui.rcp.widgets.ParamProposalProvider;
import org.eclipse.jubula.tools.constants.StringConstants;
import org.eclipse.jubula.tools.constants.TestDataConstants;
import org.eclipse.jubula.tools.exception.Assert;
import org.eclipse.jubula.tools.xml.businessmodell.Action;
import org.eclipse.jubula.tools.xml.businessmodell.Param;
import org.eclipse.jubula.tools.xml.businessmodell.ParamValueSet;
import org.eclipse.jubula.tools.xml.businessmodell.ValueSetElement;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.PropertyDescriptor;


/**
 * This class creates swt Controls depending on the parameters of IParamNodePOs
 * to edit the test data of the parameters
 * 
 * @author BREDEX GmbH
 * @created 19.06.2006
 */
public class TestDataControlFactory {

    /**
     * values available for Boolean parameters
     */
    private static final String[] BOOLEAN_VALUES = 
        new String[]{"true", "false"}; //$NON-NLS-1$ //$NON-NLS-2$


    /**
     * private utility constructor
     */
    private TestDataControlFactory() {
        // do nothing
    }
    
    /**
     * Creates Controls depending on the given IParamNodePO and the given
     * parameter name in dataset view.<br>
     * E.g. it returns a Text Control which only accepts Integers.
     * @param paramName the current parameter name
     * @param paramObj the current param object
     * @param parent the parent composite
     * @param style the style of the new Control
     * @return the control to edit test data
     */
    public static Control createControl(IParameterInterfacePO paramObj, 
        String paramName, Composite parent, int style) {
        
        Map<String, String> map = StringHelper.getInstance().getMap();
        String nameOfParam = paramName;
        IParamDescriptionPO paramDesc = paramObj.getParameterForName(
            nameOfParam);
        if (paramDesc == null) {
            nameOfParam = map.get(paramName);
            paramDesc = paramObj.getParameterForName(nameOfParam);
        }
        if (paramObj instanceof ICapPO) {
            ICapPO cap = (ICapPO)paramObj;
            Action action = CapBP.getAction(cap);
            List<String> values = new ArrayList<String>();
            Param param = action.findParam(paramDesc.getUniqueId());
            for (Iterator iter = param.valueSetIterator(); iter.hasNext();) {
                values.add(map.get(((ValueSetElement)iter.next()).getValue()));
            }
            if (!values.isEmpty()) {
                return new CheckedParamText(parent, style, cap, paramDesc,
                        createParamValueValidator(TestDataConstants.COMBO,
                                param.getValueSet().isCombinable(),
                                values.toArray(new String[values.size()])));
            }
        }
        if (paramObj instanceof IParamNodePO) {
            IParamNodePO paramNode = (IParamNodePO)paramObj;
            ParamValueSet valueSet = 
                ParamTextPropertyDescriptor.getValuesSet(
                        paramNode, paramDesc.getUniqueId());
            String [] values = ParamTextPropertyDescriptor.getValues(valueSet);
            if (TestDataConstants.BOOLEAN.equals(paramDesc.getType())) {
                values = BOOLEAN_VALUES;
            }

            return new CheckedParamTextContentAssisted(parent, style,
                    paramNode, paramDesc, 
                    createParamValueValidator(
                            paramDesc.getType(), 
                            valueSet != null ? valueSet.isCombinable() : false, 
                                    values), 
                    new ParamProposalProvider(
                            values, paramNode, paramDesc));
        }

        if (paramObj instanceof ITestDataCubePO) {
            ITestDataCubePO tdc = (ITestDataCubePO)paramObj;
            if (TestDataConstants.BOOLEAN.equals(paramDesc.getType())) {
                return new CheckedParamTextContentAssisted(parent, style,
                        tdc, paramDesc, 
                        createParamValueValidator(
                                paramDesc.getType(), 
                                false, BOOLEAN_VALUES), 
                        new FunctionProposalProvider());
            }
            return new CheckedParamTextContentAssisted(parent, style,
                    tdc, paramDesc, 
                    createParamValueValidator(paramDesc.getType(), false), 
                    new FunctionProposalProvider());
        }
        
        Assert.notReached(Messages.ImplementFor + StringConstants.SPACE 
                + paramObj.getClass().getName());
        return null;
    }
    
    /**
     * Creates a PropertyDescriptor depending on the given paramValController, 
     * displayName and values array.<br>
     * The values parameter is to get a ComboBoxPropertyDescriptor if
     * array.length > 0.<br>
     * For all other PropertyDescriptors set an empty String-Array into this
     * parameter.
     * 
     * @param paramValController an AbstractParamValueController.
     * @param displayName the display name of the PropertyDescriptor
     * @param values The values parameter is to get a ComboBoxPropertyDescriptor
     *               if values.length > 0.<br>
     *               For all other PropertyDescriptors set an empty 
     *               String-Array into this parameter!
     * @param valuesAreCombinable Whether combinations of the 
     *                            supplied values are allowed.
     *                            
     * @return a PropertyDescriptor
     */
    public static PropertyDescriptor createValuePropertyDescriptor(
            AbstractParamValueController paramValController,
            String displayName, String[] values, boolean valuesAreCombinable) {

        final String paramType = paramValController.getParamDesc().getType();
        if (values.length > 0) {
            return new ParamComboPropertyDescriptor(paramValController,
                    displayName, values, createParamValueValidator(
                            TestDataConstants.COMBO, valuesAreCombinable, 
                            values));
        }
        if (TestDataConstants.BOOLEAN.equals(paramType)) {
            return new ParamTextPropertyDescriptor(paramValController,
                    displayName, createParamValueValidator(
                            TestDataConstants.COMBO, valuesAreCombinable, 
                            BOOLEAN_VALUES));
        }
        return new ParamTextPropertyDescriptor(paramValController,
                displayName, 
                createParamValueValidator(paramType, valuesAreCombinable));
    }

    /**
     * @param type
     *            type of parameter
     * @param valuesAreCombinable
     *            whether combinations of the supplied values are allowed
     * @param values
     *            list of possible values for a parameter
     * @return validator fit to given type
     */
    private static IParamValueValidator createParamValueValidator(String type, 
            boolean valuesAreCombinable, String... values) {
        return AbstractParamInterfaceBP.createParamValueValidator(
                type, valuesAreCombinable, values);
    }
}