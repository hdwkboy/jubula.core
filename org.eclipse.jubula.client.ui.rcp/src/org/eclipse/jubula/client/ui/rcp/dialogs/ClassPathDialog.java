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
package org.eclipse.jubula.client.ui.rcp.dialogs;

import java.io.File;

import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.client.ui.rcp.utils.Utils;
import org.eclipse.jubula.tools.constants.StringConstants;
import org.eclipse.jubula.tools.exception.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


/**
 * @author BREDEX GmbH
 * @created Nov 29, 2006
 */
public class ClassPathDialog extends InputDialog {
    /** gui component */
    private Button m_addFileButton;
    /** gui component */
    private Button m_addDirButton;
    /** the the WidgetSelectionListener */
    private final WidgetSelectionListener m_selectionListener = 
        new WidgetSelectionListener();
    /** true, if browse buttons should be enabled */
    private boolean m_buttonsEnabled;

    /**
     * @param parentShell The parent shell.
     * @param title The name of the title.
     * @param oldName The old name of the selected item.
     * @param message The message.
     * @param label The label of the textfield.
     * @param wrongNameError The wrongNameError message.
     * @param doubleNameError The doubleNameError message.
     * @param image The image of the dialog.
     * @param shell The name of the shell.
     * @param maxLength Maximum Length of input
     * @param browseable True, if you want to add a browse button in the dialog.
     * @param browseButtonsEnabled true, if browse buttons should be enabled
     */
    public ClassPathDialog(Shell parentShell, String title, String oldName, 
            String message, String label, String wrongNameError, 
            String doubleNameError, String image, String shell, 
            boolean browseable, int maxLength, boolean browseButtonsEnabled) {
        
        super(parentShell, title, oldName, message, label, wrongNameError,
                doubleNameError, image, shell, browseable, maxLength);
        m_buttonsEnabled = browseButtonsEnabled;
    }
    
    /**
     * installs all listeners to the gui components. All components visualizing
     * a property do have some sort of modification listeners which store
     * edited data in the edited instance. Some gui components have additional
     * listeners for data validatuion or permission reevaluation.
     */
    private void installListeners() {
        m_addDirButton.addSelectionListener(m_selectionListener);
        m_addFileButton.addSelectionListener(m_selectionListener);
    }

    /**
     * {@inheritDoc}
     */
    protected void createAdditionalComponents(Composite parent) {
        new Label(parent, SWT.NONE);
        m_addFileButton = new Button(parent, SWT.PUSH);
        m_addFileButton.setText(Messages.ClassPathDialogFile);
        GridData data = new GridData ();
        data.horizontalAlignment = GridData.END;
        data.grabExcessHorizontalSpace = true;
        m_addFileButton.setLayoutData(data);
        m_addFileButton.setEnabled(m_buttonsEnabled);
        
        m_addDirButton = new Button(parent, SWT.PUSH);
        m_addDirButton.setText(Messages.ClassPathDialogDir);
        data = new GridData ();
        data.horizontalAlignment = GridData.END;
        m_addDirButton.setLayoutData(data);
        m_addDirButton.setEnabled(m_buttonsEnabled);
        
        installListeners();
    }
    
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite parent) {
        Control area = super.createDialogArea(parent);
        return area;
    }
    
    /**
     * Handles the button event.
     * @param directoryDialog The directory dialog.
     */
    void handleAddDirButtonEvent(DirectoryDialog directoryDialog) {
        directoryDialog.setMessage(Messages.ClassPathDialogAddDir);
        directoryDialog.setFilterPath(Utils.getLastDirPath());
        String directory = directoryDialog.open();
        if (directory != null) {
            Utils.storeLastDirPath(directoryDialog.getFilterPath());
            setInputFieldText(directory);
        }
    }
    
    /**
     * Handles the button event.
     * @param fileDialog The file dialog.
     */
    void handleAddFileButtonEvent(FileDialog fileDialog) {
        String directory;
        fileDialog.setFilterExtensions(new String[]{StringConstants.STAR
                + StringConstants.DOT + StringConstants.STAR});
        fileDialog.setText(Messages.ClassPathDialogFileDialogMessage);
        fileDialog.setFilterPath(Utils.getLastDirPath());
        directory = fileDialog.open();
        if (directory != null) {
            Utils.storeLastDirPath(fileDialog.getFilterPath());
            String concatFiles = StringConstants.EMPTY;
            for (String file : fileDialog.getFileNames()) {
                file = fileDialog.getFilterPath() + File.separator 
                    + file + File.pathSeparator;
                concatFiles = concatFiles + file;
            }
            setInputFieldText(concatFiles);
        }
    }
    
    /**
     * This private inner class contains a new SelectionListener.
     * 
     * @author BREDEX GmbH
     * @created 13.07.2005
     */
    private class WidgetSelectionListener extends SelectionAdapter {

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            Object o = e.getSource();
            if (o.equals(m_addDirButton)) {
                handleAddDirButtonEvent(new DirectoryDialog(Plugin.getShell(), 
                        SWT.APPLICATION_MODAL | SWT.ON_TOP));
                return;
            } else if (o.equals(m_addFileButton)) {
                handleAddFileButtonEvent(new FileDialog(Plugin.getShell(), 
                        SWT.MULTI | SWT.APPLICATION_MODAL | SWT.ON_TOP));
                return;
            } 
            Assert.notReached(Messages.EventActivatedUnknownWidget
                    + StringConstants.LEFT_PARENTHESES + o
                    + StringConstants.RIGHT_PARENTHESES
                    + StringConstants.DOT);    
        }
    }
}