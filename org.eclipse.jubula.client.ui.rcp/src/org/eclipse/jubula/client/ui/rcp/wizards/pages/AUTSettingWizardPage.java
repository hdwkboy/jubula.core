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
package org.eclipse.jubula.client.ui.rcp.wizards.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jubula.client.core.model.IAUTMainPO;
import org.eclipse.jubula.client.core.model.IProjectPO;
import org.eclipse.jubula.client.core.utils.Languages;
import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
import org.eclipse.jubula.client.ui.constants.IconConstants;
import org.eclipse.jubula.client.ui.rcp.Plugin;
import org.eclipse.jubula.client.ui.rcp.databinding.validators.AutIdValidator;
import org.eclipse.jubula.client.ui.rcp.dialogs.NagDialog;
import org.eclipse.jubula.client.ui.rcp.factory.ControlFactory;
import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
import org.eclipse.jubula.client.ui.rcp.widgets.AutIdListComposite;
import org.eclipse.jubula.client.ui.rcp.widgets.ListElementChooserComposite;
import org.eclipse.jubula.client.ui.rcp.wizards.ProjectWizard;
import org.eclipse.jubula.client.ui.utils.LayoutUtil;
import org.eclipse.jubula.client.ui.widgets.DirectCombo;
import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
import org.eclipse.jubula.tools.constants.CommandConstants;
import org.eclipse.jubula.tools.constants.StringConstants;
import org.eclipse.jubula.tools.exception.Assert;
import org.eclipse.jubula.tools.xml.businessmodell.ToolkitPluginDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;



/**
 * @author BREDEX GmbH
 * @created 18.05.2005
 */
public class AUTSettingWizardPage extends WizardPage {

    /** number of columns = 1 */
    private static final int NUM_COLUMNS_1 = 1; 
    /** number of columns = 2 */
    private static final int NUM_COLUMNS_2 = 2;  

    /** the text field for the available languages (= project languages)*/
    private List m_availableLangListField;

    /** the text field for the aut languages */
    private List m_autLangListField;

    /** the button to add a language from the upper field into the bottom field */
    private Button m_rightButton;

    /** the button to delete a language from the bottom field */
    private Button m_leftButton;

    /** the AUT name editor */
    private Text m_autNameText;
    
    /** the combo box with the toolkit names */
    private DirectCombo<String> m_autToolKitComboBox;

    /** the new AUT to create */
    private IAUTMainPO m_autMain;
    
    /** the new project to create */
    private IProjectPO m_project;

    /** the the WidgetSelectionListener */
    private final WidgetSelectionListener m_selectionListener = 
        new WidgetSelectionListener();

    /** the WidgetModifyListener */
    private final WidgetModifyListener m_modifyListener = 
        new WidgetModifyListener();
    
    /** the composite with 2 ListBoxes */
    private ListElementChooserComposite m_chooseLists;
    
    /** the button to add all languages from the upper field into the bottom field */
    private Button m_allRightButton;
    
    /** the button to delete all languages from the bottom field */
    private Button m_allLeftButton;
    
    /***/
    private ScrolledComposite m_scroll;
    
    /***/
    private boolean m_initFirstTime = true;
    
    /** The button to indicate whether names should be generated */
    private Button m_generateNames;
    
    /**
     * @param pageName The page name.
     * @param newProject The new project to create.
     * @param autMain The new AUT to create.
     */
    public AUTSettingWizardPage(String pageName, IProjectPO newProject, 
        IAUTMainPO autMain) {
        
        super(pageName);
        setPageComplete(false);
        m_project = newProject;
        m_autMain = autMain;
    }

    /**
     * @return the combo box with the toolkit names
     */
    public DirectCombo<String> getToolkitComboBox() {
        return m_autToolKitComboBox;
    }
    
    /**
     * @param parent The parent composite.
     */
    public void createControl(Composite parent) {
        m_scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        Composite composite = createComposite(m_scroll, 1, GridData.FILL, 
            false);
        Composite autNameComposite = createComposite(composite, 
            NUM_COLUMNS_2, GridData.FILL, false);        
        newLabel(autNameComposite, StringConstants.EMPTY);
        newLabel(autNameComposite, StringConstants.EMPTY);
        createAUTNameEditor(autNameComposite);

        separator(composite);
        createAutIdList(composite);
        separator(composite);

        Composite innerComposite = new Composite(composite, SWT.NONE);
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = NUM_COLUMNS_1;
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        innerComposite.setLayout(compositeLayout);
        GridData compositeData = new GridData();
        compositeData.horizontalSpan = NUM_COLUMNS_2;
        compositeData.horizontalAlignment = GridData.CENTER;
        compositeData.grabExcessHorizontalSpace = true;
        innerComposite.setLayoutData(compositeData);
        java.util.List<String> leftList = new ArrayList<String>();
        Iterator<Locale> iter = m_project.getLangHelper().getLangListIterator();
        while (iter.hasNext()) {
            leftList.add(Languages.getInstance().getDisplayString(iter.next()));
        }
        Label descriptionLabel = new Label(innerComposite, SWT.NONE);
        descriptionLabel.setText(
                Messages.AUTSettingWizardPageSelectLanguagesOfTD);
        ControlDecorator.decorateInfo(descriptionLabel, 
                "ControlDecorator.NewProjectAUTLanguage", false);
        m_chooseLists = new ListElementChooserComposite(
            innerComposite, 
            Messages.AUTSettingWizardPageUpperLabel,
            leftList,  
            Messages.AUTSettingWizardPageBottomLabel,
            new ArrayList(),  
            15, 
            new Image[]{
                IconConstants.RIGHT_ARROW_IMAGE,
                IconConstants.DOUBLE_RIGHT_ARROW_IMAGE,
                IconConstants.LEFT_ARROW_IMAGE,
                IconConstants.DOUBLE_LEFT_ARROW_IMAGE},
            new Image[] { IconConstants.RIGHT_ARROW_DIS_IMAGE, 
                IconConstants.DOUBLE_RIGHT_ARROW_DIS_IMAGE, 
                IconConstants.LEFT_ARROW_DIS_IMAGE, 
                IconConstants.DOUBLE_LEFT_ARROW_DIS_IMAGE },
            new String[]{Messages.AUTSettingWizardPageDownToolTip,
                Messages.AUTSettingWizardPageAllDownToolTip,
                Messages.AUTSettingWizardPageUpToolTip,
                Messages.AUTSettingWizardPageAllUpToolTip},
            ListElementChooserComposite.VERTICAL);  
        getObjects();

        addListener();
        
        resizeLists();
        
        Plugin.getHelpSystem().setHelp(composite, 
            ContextHelpIds.AUT_SETTING_WIZARD_PAGE);
        checkLanguageButtons();
        
        createNextLabel(composite);
        
        m_scroll.setContent(composite);
        m_scroll.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        m_scroll.setExpandHorizontal(true);
        m_scroll.setExpandVertical(true);
        setControl(m_scroll);
        
    }
    
    /**
     * Resizes the two ListBoxes.
     */
    private void resizeLists() {
        ((GridData)m_availableLangListField.getLayoutData()).widthHint = 
            Dialog.convertHeightInCharsToPixels(
                LayoutUtil.getFontMetrics(m_availableLangListField), 15);
        ((GridData)m_autLangListField.getLayoutData()).widthHint = 
            Dialog.convertHeightInCharsToPixels(
                LayoutUtil.getFontMetrics(m_autLangListField), 15);
    }
    
    /**
     * Creates a label.
     * @param composite the parent composite
     */
    private void createNextLabel(Composite composite) {
        Label nextLabel = new Label(composite, SWT.NONE);
        nextLabel.setText(Messages.AUTSettingWizardPageClickNext);
        GridData data = new GridData();
        data.grabExcessVerticalSpace = true;
        data.verticalAlignment = GridData.END;
        nextLabel.setLayoutData(data);
    }

    /**
     * Creates a new composite.
     * @param parent The parent composite.
     * @param numColumns the number of columns for this composite.
     * @param alignment The horizontalAlignment.
     * @param horizontalSpace The horizontalSpace.
     * @return The new composite.
     */
    private Composite createComposite(Composite parent, int numColumns, 
            int alignment, boolean horizontalSpace) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.numColumns = numColumns;
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        composite.setLayout(compositeLayout);
        GridData compositeData = new GridData();
        compositeData.horizontalAlignment = alignment;
        compositeData.grabExcessHorizontalSpace = horizontalSpace;
        composite.setLayoutData(compositeData);
        return composite;       
    }
 
    /**
     * Gets the listBoxes / Buttons from the ListElementChooserComposite composite
     */
    private void getObjects() {
        m_availableLangListField = m_chooseLists.getListOne();
        m_autLangListField = m_chooseLists.getListTwo();
        m_leftButton = m_chooseLists.getSelectionTwoToOneButton();
        m_rightButton = m_chooseLists.getSelectionOneToTwoButton();
        m_allLeftButton = m_chooseLists.getAllTwoToOneButton();
        m_allRightButton = m_chooseLists.getAllOneToTwoButton();
    }

    /**
     * Creates the textfield for the project name. 
     * @param parent The parent composite.
     */
    private void createAUTNameEditor(Composite parent) {
        Composite leftComposite = createComposite(parent, NUM_COLUMNS_1, 
            GridData.FILL, false);
        Composite rightComposite = createComposite(parent, NUM_COLUMNS_1, 
            GridData.FILL, true);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);
        rightComposite.setLayout(gridLayout);
        newLabel(leftComposite, Messages.AUTSettingWizardPageAutName);
        m_autNameText = new Text(rightComposite, SWT.BORDER);
        m_autNameText.setFocus();
        final GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        m_autNameText.setLayoutData(gridData);
        LayoutUtil.setMaxChar(m_autNameText);
        createAutToolkitCombo(leftComposite, rightComposite, gridData);
        createGenerateNamesCheckBox(parent);
    }

    /**
     * Creates the graphical components for viewing and changing the list of
     * AUT IDs.
     * 
     * @param parent The parent composite for the graphical components.
     */
    private void createAutIdList(Composite parent) {
        Composite autIdListComposite = 
            new AutIdListComposite(parent, m_autMain, 
                    new AutIdValidator(m_project));
        GridData compositeData = new GridData();
        compositeData.horizontalAlignment = SWT.FILL;
        compositeData.grabExcessHorizontalSpace = true;
        autIdListComposite.setLayoutData(compositeData);
    }
    
    /**
     * @param  parent The parent composite.
     */
    private void createGenerateNamesCheckBox(Composite parent) {
        Composite leftComposite = createComposite(parent, 3,
                SWT.LEFT, false);
        Composite rightComposite = createComposite(parent, NUM_COLUMNS_1,
                SWT.FILL, true);
        Label infoLabel = newLabel(leftComposite, 
                Messages.AUTPropertiesDialogGenerateNames);
        ControlDecorator.decorateInfo(infoLabel, 
                "AUTPropertiesDialog.generateNamesDescription", false); //$NON-NLS-1$
        m_generateNames = new Button(rightComposite, SWT.CHECK);
        m_generateNames.addSelectionListener(new SelectionListener() {
            
            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing 
            }

            public void widgetSelected(SelectionEvent e) {
                if (m_autMain.isGenerateNames() 
                        != m_generateNames.getSelection()) {
                    m_autMain.setGenerateNames(m_generateNames.getSelection());
                }
            }
            
        });
        if (m_autMain.getToolkit() != null 
                && m_autMain.getToolkit()
                    .equals(CommandConstants.RCP_TOOLKIT)) {
            m_generateNames.setEnabled(true);
        } else {
            m_generateNames.setEnabled(false);
            m_generateNames.setSelection(false);
        }
        m_generateNames.setSelection(m_autMain.isGenerateNames());
    }

    /**
     * Creates a Combo to select the toolkit
     * @param leftComposite see createAUTNameEditor()
     * @param rightComposite see createAUTNameEditor()
     * @param gridData see createAUTNameEditor()
     */
    private void createAutToolkitCombo(Composite leftComposite, 
        Composite rightComposite, final GridData gridData) {
        
        newLabel(leftComposite, StringConstants.EMPTY);
        newLabel(rightComposite, StringConstants.EMPTY);
        ControlDecorator.decorateInfo(newLabel(leftComposite, 
                Messages.AUTSettingWizardPageToolkit), 
                "ControlDecorator.NewProjectAUTToolkit", false);
        try {
            m_autToolKitComboBox = ControlFactory.createAutToolkitCombo(
                rightComposite, m_project, m_autMain.getToolkit());
            m_autToolKitComboBox.deselectAll();
            m_autToolKitComboBox.clearSelection();
            String autToolkit = m_autMain.getToolkit(); 
            if (autToolkit != null && autToolkit.trim().length() != 0) {
                m_autToolKitComboBox.setSelectedObject(autToolkit);
            }
        } catch (ToolkitPluginException tpe) {
            // Toolkit for project could not be found.
            // Create a combo with only the aut toolkit.
            m_autToolKitComboBox = ControlFactory.createAutToolkitCombo(
                rightComposite, m_autMain);
        }
        m_autToolKitComboBox.addSelectionListener(m_selectionListener);
        m_autToolKitComboBox.setLayoutData(gridData);
    }

    /**
     * Creates a label for this page. 
     * @param text The label text to set.
     * @param parent The composite.
     * @return a new label
     */
    private Label newLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridData labelGrid = new GridData(GridData.BEGINNING, GridData.CENTER,
            false, false, 1, 1);
        label.setLayoutData(labelGrid);
        return label;
    }
    
    /**
     * {@inheritDoc}
     * MH: Doesn't seem to work...
     */
    public void performHelp() {
        PlatformUI.getWorkbench().getHelpSystem()
            .displayHelp(ContextHelpIds.AUT_SETTING_WIZARD_PAGE);
    }
    
    /**
     * Dis-/Enables the UP-/DownButtons, if the languageLists are empty.
     */
    private void checkLanguageButtons() {
        m_rightButton.setEnabled(true);
        m_leftButton.setEnabled(false);
    }

    /**
     * Fills the lists of languages.
     */
    public void fillLanguageLists() {
        if (m_initFirstTime) {
            m_autLangListField.removeAll();
            for (Locale lang : m_project.getLangHelper().getLanguageList()) {
                m_autLangListField.add(
                    Languages.getInstance().getDisplayString(lang));
            }
            m_initFirstTime = false;
        }
        m_availableLangListField.removeAll();
        if (m_project != null) {
            Iterator<Locale> iter = 
                m_project.getLangHelper().getLangListIterator();
            while (iter.hasNext()) {
                Locale language = iter.next();
                String lang = 
                    Languages.getInstance().getDisplayString(language);
                m_availableLangListField.add(lang);
            }
        } 
        // delete invalid aut Languages
        for (int i = 0; i < m_autLangListField.getItemCount(); i++) {
            java.util.List <String> projectLanguages = 
                Arrays.asList(m_availableLangListField.getItems());
            if (!projectLanguages.contains(m_autLangListField.getItem(i))) {
                m_autLangListField.remove(m_autLangListField.
                    getItem(i));
            } 
        }
        // remove used aut languages from available languages
        for (int i = 0; i < m_autLangListField.getItemCount(); i++) {
            java.util.List <String> projectLanguages = 
                Arrays.asList(m_availableLangListField.getItems());
            if (projectLanguages.contains(m_autLangListField.getItem(i))) {
                m_availableLangListField.remove(m_autLangListField.
                    getItem(i));
            } 
        }
        confirmNextButton();
    }
    
    /** Removes all items from the language lists. */
    public void clearLanguageLists() {
        m_availableLangListField.removeAll();
        m_autLangListField.removeAll();
    }
    
    /** 
     * Removes the selected AUT languages 
     * @param selection The selected AUT languages.
     */
    public void removeAUTLanguages(String selection) {
        for (int i = 0; i < m_autLangListField.getItemCount(); i++) {
            if (m_autLangListField.getItem(i).equals(selection)) {
                m_autLangListField.remove(selection);
            }
        }
    }

    /**
     * Adds listeners.
     */
    private void addListener() {
        m_rightButton.addSelectionListener(m_selectionListener);
        m_leftButton.addSelectionListener(m_selectionListener);
        m_allRightButton.addSelectionListener(m_selectionListener);
        m_allLeftButton.addSelectionListener(m_selectionListener);
        m_autNameText.addModifyListener(m_modifyListener);
        m_autLangListField.addSelectionListener(m_selectionListener);
        m_availableLangListField.addSelectionListener(m_selectionListener);
    }

    /**
     * Removes listeners.
     */
    private void removeListener() {
        m_rightButton.removeSelectionListener(m_selectionListener);
        m_leftButton.removeSelectionListener(m_selectionListener);
        m_allRightButton.removeSelectionListener(m_selectionListener);
        m_allLeftButton.removeSelectionListener(m_selectionListener);
        m_autNameText.removeModifyListener(m_modifyListener);
        m_autLangListField.removeSelectionListener(m_selectionListener);
        m_availableLangListField.removeSelectionListener(m_selectionListener);
    }

    /**
     * This private inner class contains a new SelectionListener.
     * @author BREDEX GmbH
     * @created 10.02.2005
     */
    private class WidgetSelectionListener implements SelectionListener {
        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            handleEvent(e);
        }

   
        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) { 
            handleEvent(e);
        }
        
        /**
         * Handles the given event.
         * @param e a SelectionEvent.
         */
        private void handleEvent(SelectionEvent e) {
            final Object o = e.getSource();
            if (o.equals(m_rightButton)
                || o.equals(m_leftButton)
                || o.equals(m_allLeftButton)
                || o.equals(m_allRightButton)) {
                
                updateLanguages();
                checkCompleteness();
                return;
            } else if (o.equals(m_autLangListField) 
                || o.equals(m_availableLangListField)) {
                
                return;
            } else if (o.equals(m_autToolKitComboBox)) {
                if (CommandConstants.RCP_TOOLKIT.equals(
                        m_autToolKitComboBox.getSelectedObject())) {
                    m_generateNames.setEnabled(true);
                    m_generateNames.setSelection(true);
                } else {
                    m_generateNames.setEnabled(false);
                    m_generateNames.setSelection(false);
                }
                checkCompleteness();
                return;
            }
            Assert.notReached(Messages.EventActivatedByUnknownWidget 
                + StringConstants.COLON + StringConstants.SPACE 
                + StringConstants.APOSTROPHE + String.valueOf(o) 
                + StringConstants.APOSTROPHE);
        }
    }
    
    /**
     * Updates the aut languages.
     */
    private void updateLanguages() {
        m_autMain.getLangHelper().clearLangList();
        for (String lang : m_autLangListField.getItems()) {
            m_autMain.getLangHelper().addLanguageToList(
                Languages.getInstance().getLocale(lang));
        }
    } 

    /**
     * This private inner class contains a new ModifyListener.
     * @author BREDEX GmbH
     * @created 11.02.2005
     */
    private class WidgetModifyListener implements ModifyListener {
        /**
         * {@inheritDoc}
         */
        public void modifyText(ModifyEvent e) {
            Object o = e.getSource();
            if (o.equals(m_autNameText)) {
                m_autMain.setName(m_autNameText.getText());
                checkCompleteness();
                return;
            } 
            Assert.notReached(Messages.EventActivatedByUnknownWidget 
                    + StringConstants.DOT);
        }
    }

    /**
     * Creates an AUT when pressing the next button.
     */
    private void confirmNextButton() {
        String oldToolkit = m_autMain.getToolkit();
        m_autMain.setName(m_autNameText.getText());
        m_autMain.setToolkit(m_autToolKitComboBox.getSelectedObject());
        m_autMain.setGenerateNames(m_generateNames.getSelection());
        checkToolkit(this.getShell(), m_autMain, oldToolkit);
        updateLanguages();
    }
    
    /**
     * check if the user needs info about the selected toolkit
     * @param shell the shell to be used as a parent or null for the
     * plug-ins default shell.
     * @param autMain which AUT is checked
     * @param oldToolkit old value
     */
    public static void checkToolkit(Shell shell,
            IAUTMainPO autMain, String oldToolkit) {
        String newToolkit = autMain.getToolkit();
        if (newToolkit != null) {
            if (((oldToolkit == null) || !newToolkit.equals(oldToolkit))
                    && newToolkit.equals(CommandConstants.RCP_TOOLKIT)) {
                NagDialog.runNagDialog(shell, "InfoNagger.DefineRcpAut", //$NON-NLS-1$
                        ContextHelpIds.AUT_CONFIG_SETTING_WIZARD_PAGE); 
            }
        }
    }
    /**
     * Creates a separator line.
     * 
     * @param composite
     *            The parent composite.
     */
    private void separator(Composite composite) {
        newLabel(composite, StringConstants.EMPTY);
        Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData sepData = new GridData();
        sepData.horizontalAlignment = GridData.FILL;
        sepData.horizontalSpan = NUM_COLUMNS_2;
        sep.setLayoutData(sepData);
        newLabel(composite, StringConstants.EMPTY);
    }
        
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        fillLanguageLists();
        super.setVisible(visible);
        java.util.List<ToolkitPluginDescriptor> toolkits;
        java.util.List<String> values = new ArrayList<String>();
        java.util.List<String> displayValues = new ArrayList<String>();
        try {
            toolkits = ControlFactory
                .getAutToolkits(m_project);

            for (ToolkitPluginDescriptor desc : toolkits) {
                values.add(desc.getToolkitID());
                displayValues.add(desc.getName());
            }
        } catch (ToolkitPluginException tpe) {
            ToolkitPluginDescriptor autToolkit =
                ComponentBuilder.getInstance().getCompSystem()
                .getToolkitPluginDescriptor(m_autMain.getToolkit());
            
            if (autToolkit != null) {
                values.add(autToolkit.getToolkitID());
                displayValues.add(autToolkit.getName());
            } else {
                values.add(m_autMain.getToolkit());
                displayValues.add(m_autMain.getToolkit());
            }
        }
        String selectedObject = m_autToolKitComboBox.getSelectedObject();
        m_autToolKitComboBox.setItems(values, displayValues);
        if (selectedObject == null || selectedObject.length() < 1) {
            m_autToolKitComboBox.deselectAll();
        } else {
            m_autToolKitComboBox.setSelectedObject(selectedObject);
        }
        if (visible) {
            m_autNameText.setFocus();
            checkCompleteness();
            m_chooseLists.checkButtons();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        m_autMain = null;
        removeListener();
        super.dispose();
    }
    
    /**
     * Sets the "Next>"-button true, if all fields are filled in correctly.
     */
    private void checkCompleteness() {
        if (modifyAUTNameFieldAction() 
            && modifyAutToolkitComboAction()) {
            
            setMessage(Messages.ProjectWizardNewAUT, IMessageProvider.NONE);
            setPageComplete(true);
            confirmNextButton();
        } else {
            setPageComplete(false);
        }
    }

    /**
     * The action for the toolkit combo box.
     * @return <code>true</code> if the input for the toolkit combo box is
     *         valid. Otherwise, <code>false</code>.
     */
    private boolean modifyAutToolkitComboAction() {
        boolean isToolkitSelected = 
            m_autToolKitComboBox.getSelectedObject() != null;
        
        if (isToolkitSelected) {
            String oldToolkit = m_autMain.getToolkit();
            m_autMain.setToolkit(m_autToolKitComboBox.getSelectedObject());
            checkToolkit(getShell(), m_autMain, oldToolkit);
        } else {
            setMessage(Messages.ProjectWizardNoToolkitSelected,
                IMessageProvider.ERROR);

        }

        return isToolkitSelected;
    }
    
    /** 
     * The action of the AUT name field.
     * @return false, if the AUT name field contents an error:
     * the AUT name starts or end with a blank, or the field is empty
     */
    private boolean modifyAUTNameFieldAction() {
        boolean isError = false;
        int autNameLength = m_autNameText.getText().length();
        if ((autNameLength == 0)
                || (m_autNameText.getText().startsWith(" ")) //$NON-NLS-1$
            || (m_autNameText.getText().charAt(autNameLength - 1) == ' ')) {
            
            isError = true;
        }
        if (isError) {
            if (autNameLength == 0) {
                setMessage(Messages.AUTSettingWizardPageEmptyAUTName,
                        IMessageProvider.ERROR); 
                setPageComplete(false);
            } else {
                setMessage(Messages.ProjectWizardNotValidAUT,
                        IMessageProvider.ERROR); 
                setPageComplete(false);
            }
        }
        return !isError;
    }
    
   
    /**
     * {@inheritDoc}
     */
    public ProjectWizard getWizard() {
        return (ProjectWizard)super.getWizard();
    }
}