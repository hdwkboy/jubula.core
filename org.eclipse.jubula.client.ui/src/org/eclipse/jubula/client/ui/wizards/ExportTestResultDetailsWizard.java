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
package org.eclipse.jubula.client.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jubula.client.core.businessprocess.AbstractXMLReportGenerator;
import org.eclipse.jubula.client.core.businessprocess.CompleteXMLReportGenerator;
import org.eclipse.jubula.client.core.businessprocess.FileXMLReportWriter;
import org.eclipse.jubula.client.core.businessprocess.IXMLReportWriter;
import org.eclipse.jubula.client.core.businessprocess.TestResultBP;
import org.eclipse.jubula.client.core.model.ITestResultSummaryPO;
import org.eclipse.jubula.client.core.model.SummarizedTestResult;
import org.eclipse.jubula.client.core.model.TestResultNode;
import org.eclipse.jubula.client.core.persistence.GeneralStorage;
import org.eclipse.jubula.client.core.persistence.Hibernator;
import org.eclipse.jubula.client.core.persistence.TestResultPM;
import org.eclipse.jubula.client.ui.Plugin;
import org.eclipse.jubula.client.ui.editors.TestResultViewer.GenerateTestResultTreeOperation;
import org.eclipse.jubula.client.ui.wizards.pages.ExportTestResultDetailsDestinationWizardPage;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wizard for exporting Test Result details.
 *
 * @author BREDEX GmbH
 * @created Jun 25, 2010
 */
public class ExportTestResultDetailsWizard extends Wizard 
        implements IExportWizard {
    /**
     * <code>WIZARD_ID</code>
     */
    public static final String ID = "org.eclipse.jubula.client.ui.exportWizard.ExportTestResultDetailsWizard"; //$NON-NLS-1$

    /** the logger */
    private static final Logger LOG = 
        LoggerFactory.getLogger(ExportTestResultDetailsWizard.class);

    /** button labels for the dialog for overwriting files */
    private static final String[] OVERWRITE_DIALOG_BUTTON_LABELS = 
        new String[] {
            IDialogConstants.YES_LABEL,
            IDialogConstants.YES_TO_ALL_LABEL,
            IDialogConstants.NO_LABEL,
            IDialogConstants.NO_TO_ALL_LABEL,
            IDialogConstants.CANCEL_LABEL 
        };

    /** button IDs for the dialog for overwriting files */
    private static final int[] OVERWRITE_DIALOG_BUTTON_IDS = new int[] {
        IDialogConstants.YES_ID, 
        IDialogConstants.YES_TO_ALL_ID,
        IDialogConstants.NO_ID,
        IDialogConstants.NO_TO_ALL_ID,
        IDialogConstants.CANCEL_ID
    };

    /**
     * Operation for exporting Test Result details.
     *
     * @author BREDEX GmbH
     * @created Jun 25, 2010
     */
    private class ExportTestResultDetailsOperation 
            implements IRunnableWithProgress {

        // The constants for the overwrite 3 state (taken from 
        // org.eclipse.ui.internal.wizards.datatransfer.FileSystemExportOperation)
        /** 
         * constant to indicate that no "* to all" answer has yet been given 
         * for this operation 
         */
        private static final int OVERWRITE_NOT_SET = 0;

        /** 
         * constant to indicate that "no to all" (overwrite none) has been given 
         * for this operation 
         */
        private static final int OVERWRITE_NONE = 1;

        /** 
         * constant to indicate that "yes to all" (overwrite all) has been given 
         * for this operation 
         */
        private static final int OVERWRITE_ALL = 2;

        /** current file overwrite status */
        private int m_overwriteState = OVERWRITE_NOT_SET;

        /**
         * {@inheritDoc}
         */
        public void run(IProgressMonitor monitor) {

            SubMonitor subMonitor = 
                SubMonitor.convert(monitor, "Exporting...",  //$NON-NLS-1$
                        m_selectedSummaries.length * 2);
            
            try {
                for (ITestResultSummaryPO summary : m_selectedSummaries) {
                    GenerateTestResultTreeOperation operation =
                        new GenerateTestResultTreeOperation(
                                summary.getId(), 
                                summary.getInternalProjectID());
                    
                    operation.run(subMonitor.newChild(1));

                    TestResultNode rootDetailNode = operation.getRootNode();
                    AbstractXMLReportGenerator generator = 
                        new CompleteXMLReportGenerator(
                                new SummarizedTestResult(
                                        summary, rootDetailNode));

                    exportDocument(generator.generateXmlReport(), summary,
                            subMonitor);
                }
            } finally {
                monitor.done();
            }
        }

        /**
         * Exports the given document to XML and HTML files.
         * 
         * @param document XML document fully representing the Test Result 
         *                 being exported.
         * @param summary Summary of the Test Result being exported.
         * @param monitor The monitor.
         */
        private void exportDocument(Document document, 
                ITestResultSummaryPO summary, IProgressMonitor monitor) {
            try {
                URL xslFileURL = TestResultBP.getInstance().getXslFileURL();
                String cssFileName = TestResultBP.getInstance()
                        .getCssFileName();

                File cssFile = new File(cssFileName);
                String htmlDir = 
                    cssFile.getParentFile().getAbsolutePath();
                File fileToWrite = new File(
                    m_destinationPage.getDestination(), 
                        summary.getTestsuiteName() + "_" + summary.getTestsuiteStartTime().getTime()); //$NON-NLS-1$

                boolean isWriteFile = m_overwriteState == OVERWRITE_ALL
                    || !doesFileExist(fileToWrite);
                if (!isWriteFile 
                        && m_overwriteState != OVERWRITE_NONE) {
                    final MessageDialog dialog = new MessageDialog(
                            getContainer().getShell(), I18n.getString("ExportTestResultDetailsWizard.ConfirmOverwriteDialog.title"), //$NON-NLS-1$
                            null, I18n.getString("ExportTestResultDetailsWizard.ConfirmOverwriteDialog.description", new String [] {fileToWrite.getCanonicalPath()}), //$NON-NLS-1$
                            MessageDialog.QUESTION,
                            OVERWRITE_DIALOG_BUTTON_LABELS, 0);

                    getShell().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            dialog.open();
                        }
                    });
                    
                    int dialogResult = dialog.getReturnCode();
                    dialogResult = dialogResult == SWT.DEFAULT 
                            ? IDialogConstants.CANCEL_ID 
                            : OVERWRITE_DIALOG_BUTTON_IDS[dialogResult];

                    switch (dialogResult) {
                        case IDialogConstants.YES_TO_ALL_ID:
                            m_overwriteState = OVERWRITE_ALL;
                            // fall through
                        case IDialogConstants.YES_ID:
                            isWriteFile = true;
                            break;
                        case IDialogConstants.NO_TO_ALL_ID:
                            m_overwriteState = OVERWRITE_NONE;
                            // fall through
                        case IDialogConstants.NO_ID:
                            isWriteFile = false;
                            break;
                        case IDialogConstants.CANCEL_ID:
                            // cancel the operation
                            return;
                        default:
                            LOG.warn("Unexpected dialog return code: " + dialogResult); //$NON-NLS-1$
                            break;
                    }
                    
                }
                
                if (isWriteFile) {
                    IXMLReportWriter writer =  new FileXMLReportWriter(
                            fileToWrite.getAbsolutePath(), 
                            xslFileURL, htmlDir);
                    writer.write(document);
                }
                monitor.worked(1);
            } catch (IOException ioe) {
                Plugin.getDefault().handleError(ioe);
            }
        }

        /**
         * 
         * @param file The file to check. This file must not contain the 
         *             extension(s), as these will be checked during method 
         *             execution.
         * @return <code>true</code> if the given file exists as an XML file 
         *         (<code>filename</code>.xml) or as an HTML file 
         *         (<code>filename</code>.htm). Otherwise <code>false</code>.
         */
        private boolean doesFileExist(File file) {
            boolean doesXmlFileExist =
                new File(file.getAbsolutePath() 
                        + FileXMLReportWriter.FILE_EXTENSION_XML)
                            .exists();
            boolean doesHtmlFileExist =
                new File(file.getAbsolutePath() 
                        + FileXMLReportWriter.FILE_EXTENSION_HTML)
                            .exists();
            return doesHtmlFileExist || doesXmlFileExist;
        }
        
    }

    /** 
     * Test Result summaries selected for export. The details for these 
     * summaries will be exported. 
     */
    private ITestResultSummaryPO[] m_selectedSummaries;
    
    /** page for determining where the exported details should be written */
    private ExportTestResultDetailsDestinationWizardPage m_destinationPage;
    
    /**
     * {@inheritDoc}
     */
    public void addPages() {
        super.addPages();
        m_destinationPage = new ExportTestResultDetailsDestinationWizardPage();
        addPage(m_destinationPage);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @SuppressWarnings("synthetic-access")
    public boolean performFinish() {

        try {
            getContainer().run(true, true, 
                    new ExportTestResultDetailsOperation());
        } catch (InvocationTargetException e) {
            LOG.error("Error occurred while exporting Test Result details.", e.getCause()); //$NON-NLS-1$
        } catch (InterruptedException ie) {
            // Operation canceled
            // Do nothing
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setNeedsProgressMonitor(true);
        List<ITestResultSummaryPO> selectedSummaryList = 
            new ArrayList<ITestResultSummaryPO>(selection.size());
        
        // Only try to compute the details if there is an active
        // DB connection. Otherwise we will receive an NPE
        // while trying to initialize the Master Session.
        if (Hibernator.instance() != null) {
            List<Number> idsWithDetails = 
                TestResultPM.computeTestresultIdsWithDetails(
                        GeneralStorage.getInstance().getMasterSession());
            for (Object selectedElement : selection.toArray()) {
                if (selectedElement instanceof ITestResultSummaryPO
                        && idsWithDetails.contains(
                            ((ITestResultSummaryPO)selectedElement).getId())) {
                    
                    selectedSummaryList.add(
                            (ITestResultSummaryPO)selectedElement);
                }
            }
        }
        
        m_selectedSummaries = selectedSummaryList.toArray(
                new ITestResultSummaryPO[selectedSummaryList.size()]);

        setWindowTitle(I18n.getString("ExportTestResultDetailsWizard.windowTitle", new String [] {String.valueOf(m_selectedSummaries.length)})); //$NON-NLS-1$
    }

}