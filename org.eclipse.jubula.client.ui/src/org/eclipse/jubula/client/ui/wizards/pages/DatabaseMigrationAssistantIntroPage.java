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
package org.eclipse.jubula.client.ui.wizards.pages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;


/**
 * @author BREDEX GmbH
 * @created May 27, 2010
 */
public class DatabaseMigrationAssistantIntroPage extends WizardPage {

    /**
     * Constructor
     * 
     * @param pageName The name of the page.
     */
    public DatabaseMigrationAssistantIntroPage(String pageName) {
        super(pageName);
        setTitle(I18n.getString("DatabaseMigrationAssistant.introPage.title")); //$NON-NLS-1$
        setMessage(I18n.getString("DatabaseMigrationAssistant.introPage.description")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        Label label = new Label(composite, SWT.WRAP);
        label.setText(I18n.getString("DatabaseMigrationAssistant.introPage.text")); //$NON-NLS-1$

        initializeDialogUnits(label);

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING)
            .grab(true, false)
            .hint(convertHorizontalDLUsToPixels(
                    IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
                    SWT.DEFAULT).applyTo(label);

        Plugin.getHelpSystem().setHelp(composite, ContextHelpIds
                .IMPORT_PROJECT_DIALOG);

        setControl(composite);
    }

    /**
     * {@inheritDoc}
     */
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem().displayHelp(
                ContextHelpIds.DATABASE_MIGRATION_ASSISTANT);
    }

}
