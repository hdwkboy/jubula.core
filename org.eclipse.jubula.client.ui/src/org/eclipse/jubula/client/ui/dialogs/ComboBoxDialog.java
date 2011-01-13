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
package org.eclipse.jubula.client.ui.dialogs;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.constants.Layout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * Dialog with a combo box to select something.
 *
 * @author BREDEX GmbH
 * @created 06.07.2005
 */
public class ComboBoxDialog extends TitleAreaDialog {
    
    /** number of columns = 1 */
    private static final int NUM_COLUMNS_1 = 1;

    /** number of columns = 4 */
    private static final int NUM_COLUMNS_4 = 4;

    /** vertical spacing = 2 */
    private static final int VERTICAL_SPACING = 2;

    /** margin width = 0 */
    private static final int MARGIN_WIDTH = 10;

    /** margin height = 2 */
    private static final int MARGIN_HEIGHT = 10;

    /** width hint = 300 */
    private static final int WIDTH_HINT = 300;

    /** horizontal span = 3 */
    private static final int HORIZONTAL_SPAN = 3;
    
    /** The message m_text */
    private String m_message;
    
    /** the name textfield */
    private Combo m_comboBox;

    /**
     * <code>m_inputList</code> list&lt;String&gt;
     */
    private List < String > m_inputList;

    /** result of dialog, null if nothing was selected */
    private String m_selection = null;

    /**
     * result of dialog
     */
    private int m_selectionIndex;
    /**
     * <code>m_label</code> label in front of combobox
     */
    private String m_label;

    /**
     * <code>m_title</code> title
     */
    private String m_title;

    /**
     * <code>m_image</code> associated image
     */
    private Image m_image;

    /**
     * <code>m_shellTitle</code> the shell title
     */
    private String m_shellTitle;


    /**
     * @param parentShell The parent shell.
     * @param inputList list with to fill the combo box
     * @param message message m_text
     * @param title title
     * @param image name of image
     * @param shellTitle shell title
     * @param label label in front of combo box
     */
    public ComboBoxDialog(Shell parentShell, 
            List < String > inputList,
            String message, 
            String title,
            Image image, 
            String shellTitle,
            String label) {
        super(parentShell);
        m_inputList = inputList;
        m_message = message;
        m_label = label;
        m_title = title;
        m_image = image;
        m_shellTitle = shellTitle;
        
    }
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        inputListSort();
        setMessage(m_message);
        setTitle(m_title); 
        setTitleImage(m_image);
        getShell().setText(m_shellTitle); 
        // new Composite as container
        final GridLayout gridLayoutParent = new GridLayout();
        gridLayoutParent.numColumns = NUM_COLUMNS_1;
        gridLayoutParent.verticalSpacing = VERTICAL_SPACING;
        gridLayoutParent.marginWidth = MARGIN_WIDTH;
        gridLayoutParent.marginHeight = MARGIN_HEIGHT;
        parent.setLayout(gridLayoutParent);

        Plugin.createSeparator(parent);

        Composite area = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = NUM_COLUMNS_4;
        area.setLayout(gridLayout);

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.widthHint = WIDTH_HINT;

        area.setLayoutData(gridData);

        createComboBox(area);

        Plugin.createSeparator(parent);
        
        return area;
    }

    /**
     * Sorts the input list (with case insensitivity !!!).
     */
    private void inputListSort() {
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                String s1 = o1.toLowerCase();
                String s2 = o2.toLowerCase();
                return s1.compareTo(s2);
            }
        };
        Set<String> set = new TreeSet<String>(comparator);
        set.addAll(m_inputList);
        m_inputList.clear();
        m_inputList.addAll(set);
    }
   
    /**
     * @param parent
     *            The parent composite.
     */
    private void createComboBox(Composite parent) {
        new Label(parent, SWT.NONE).setLayoutData(new GridData(GridData.FILL, 
            GridData.CENTER, false, false, HORIZONTAL_SPAN + 1, 1));
        new Label(parent, SWT.NONE).setText(m_label);
        m_comboBox = new Combo(parent, SWT.SINGLE | SWT.BORDER
                | SWT.READ_ONLY);
        GridData gridData = newGridData();
        Layout.addToolTipAndMaxWidth(gridData, m_comboBox);
        m_comboBox.setLayoutData(gridData);
        fillComboBox();
        m_comboBox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                enableOKButton();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing                
            }           
        });     
        
    }

    /**
     * enables the OK button
     */
    public void enableOKButton() {
        if (getButton(IDialogConstants.OK_ID) != null) {
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        }
        setMessage(m_message); 
    }

    /**
     * This method is called, when the OK button was pressed
     */
    protected void okPressed() {
        m_selectionIndex = m_comboBox.getSelectionIndex();
        if (m_selectionIndex != -1) {
            m_selection = m_comboBox.getText();
        } else {
            m_selection = null;
        }
        setReturnCode(OK);
        close();
    }

    /**
     * Creates a new GridData.
     * @return grid data
     */
    private GridData newGridData() {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = HORIZONTAL_SPAN;
        return gridData;
    }

    /**
     * Fills the combo box with all items of the input list.
     */
    protected void fillComboBox() {
//        ServerManager serverList = ServerManager.getInstance();
//        String preferred = Constants.EMPTY_STRING;
        for (String newItem : m_inputList) {
            m_comboBox.add(newItem);
//            if (newItem.equals(serverList.getLastUsedServer())) {
//                preferred = newItem;
//            }
        }
//        if (!Constants.EMPTY_STRING.equals(preferred)) {
//            m_comboBox.remove(preferred);
//            m_comboBox.add(preferred, 0);
//        }
        if (m_comboBox.getItemCount() > 0) {
            m_comboBox.select(0);
        }
    }
    /**
     * @return Returns the selection.
     */
    public String getSelection() {
        return m_selection;
    }
    /**
     * @return Returns the comboBox.
     */
    public Combo getComboBox() {
        return m_comboBox;
    }
    /**
     * @return Returns the inputList.
     */
    public List getInputList() {
        return m_inputList;
    }
    
    /**
     * 
     * @return the selection Index
     */
    public int getSelectionIndex() {
        return m_selectionIndex;
    }
}