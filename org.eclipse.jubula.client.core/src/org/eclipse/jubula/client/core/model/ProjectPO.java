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
package org.eclipse.jubula.client.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jubula.client.core.businessprocess.ProjectNameBP;
import org.eclipse.jubula.client.core.i18n.Messages;
import org.eclipse.jubula.client.core.persistence.PersistenceUtil;


/**
 * @author BREDEX GmbH
 * @created 04.02.2005
 */
@Entity
@DiscriminatorValue(value = "P")
class ProjectPO extends ParamNodePO implements IProjectPO {

    /** standard logging */
    private static Logger log = LoggerFactory.getLogger(ProjectPO.class);
    
    /** the properties for this project */
    private ProjectPropertiesPO m_projectProperties = null;
    
    /** object to manage all AUTs */
    private AUTContPO m_autCont = null;

    /**
     * <code>m_testSuiteCont</code> object to manage all testsuites
     */
    private TestSuiteContPO m_testSuiteCont;
    
    /** object to manage all test jobs */
    private TestJobContPO m_testJobCont;
    /**
     * <code>m_specObjCont</code>object to manage all specObjects
     */
    private SpecObjContPO m_specObjCont = null;
    
    /**
     * <code>m_clientMetaDataVersion</code>metaDataVersion of Client which 
     * was used for creation of project
     */
    private Integer m_clientMetaDataVersion = null;
    
    /**
     * List of attribute descriptions used for this project.
     */
    private List<IDocAttributeDescriptionPO> m_projectAttributeDescriptions = 
        new ArrayList<IDocAttributeDescriptionPO>();
    
    /**
     * List of attribute descriptions used for test cases within this project.
     */
    private List<IDocAttributeDescriptionPO> m_testCaseAttributeDescriptions = 
        new ArrayList<IDocAttributeDescriptionPO>();

    /**
     * List of attribute descriptions used for test suites within this project.
     */
    private List<IDocAttributeDescriptionPO> m_testSuiteAttributeDescriptions = 
        new ArrayList<IDocAttributeDescriptionPO>();

    /**
     * <code>m_testdatacubecont</code> object to manage all test data cubes
     */
    private TestDataCubeContPO m_testdatacubecont;

    /**
     * only for Persistence (JPA / EclipseLink)
     */
    ProjectPO() {
//      only for Persistence (JPA / EclipseLink)
    }
    
    /**
     * The constructor.
     * @param name The name of the project.
     * @param metadataVersion metadata version
     * @param isGenerated indicates whether this node has been generated
     */
    ProjectPO(String name, Integer metadataVersion, boolean isGenerated) {
        this(metadataVersion, 1, 0, 
                PersistenceUtil.generateGuid(), isGenerated);
        ProjectNameBP.getInstance().setName(this.getGuid(), name, false);
    }

    /**
     * The constructor for projects that already have a GUID and version number.
     * @param metadataVersion metadataVersion
     * @param majorNumber The major version number for this project
     * @param minorNumber The minor version number for this project
     * @param guid The GUID of the project.
     * @param isGenerated indicates whether this node has been generated
     */
    ProjectPO(Integer metadataVersion, Integer majorNumber,
            Integer minorNumber, String guid, boolean isGenerated) {
        
        super("dummy", guid, isGenerated); //$NON-NLS-1$
        init(metadataVersion, majorNumber, minorNumber);
    }

    /**
     * @param metadataVersion metadataVersion
     * @param majorNumber The major version number of the project.
     * @param minorNumber The minor version number of the project.
     */
    private void init(Integer metadataVersion, 
            Integer majorNumber, Integer minorNumber) {
        
        setProjectProperties(
                PoMaker.createProjectPropertiesPO(
                    majorNumber, minorNumber));
        setAutCont(PoMaker.createAUTContPO());
        setTestSuiteCont(PoMaker.createTestSuiteContPO());
        setTestJobContPO(PoMaker.createTestJobContPO());
        setTestDataCubeContPO(PoMaker.createTestDataCubeContPO());
        setSpecObjCont(PoMaker.createSpecObjContPO());
        setClientMetaDataVersion(metadataVersion);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public Set<IAUTMainPO> getAutMainList() {
        return Collections.unmodifiableSet(getHbmAutCont().getAutMainList());
    }

    /**
     * Adds an AUT to a project.
     * @param aut The AUT to add.
     */
    public void addAUTMain(IAUTMainPO aut) {
        getHbmAutCont().addAUTMain(aut);
    }
    /**
     * Removes an AUT from a project.
     * @param aut The AUT to remove.
     */
    public void removeAUTMain(IAUTMainPO aut) {
        getHbmAutCont().removeAUTMain(aut);
        List<ITestSuitePO> tsList = getTestSuiteCont().getTestSuiteList();
        for (ITestSuitePO ts : tsList) {
            if (aut == ts.getAut()) {
                ts.setAut(null);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Transient
    public Locale getDefaultLanguage() {
        return m_projectProperties.getDefaultLanguage();
    }
    /**
     * 
     * {@inheritDoc}
     */
    public void setDefaultLanguage(Locale defaultLanguage) {
        m_projectProperties.setDefaultLanguage(defaultLanguage);
    }    
    
      

    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public ISpecObjContPO getSpecObjCont() {
        return getHbmSpecObjCont();
    }

    /**
     * @param specObjCont The specObjCont to set.
     */
    private void setSpecObjCont(ISpecObjContPO specObjCont) {
        setHbmSpecObjCont((SpecObjContPO)specObjCont);
        getHbmSpecObjCont().setParentProjectId(getId());
    }

    /**
     * @param autCont The autCont to set.
     */
    private void setAutCont(IAUTContPO autCont) {
        setHbmAutCont((AUTContPO)autCont);
    }

    /**
     *      
     * @return Returns the specObjCont.
     */
    @OneToOne(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECOBJ_CONT", unique = true)
    private SpecObjContPO getHbmSpecObjCont() {
        return m_specObjCont;
    }

    /**
     * @param specObjCont The specObjCont to set.
     */
    private void setHbmSpecObjCont(SpecObjContPO specObjCont) {
        m_specObjCont = specObjCont;
    }

    /**
     *      
     * @return Returns the AutCont.
     */
    @OneToOne(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @JoinColumn(name = "AUT_CONT", unique = true)
    private AUTContPO getHbmAutCont() {
        return m_autCont;
    }

    /**
     * @param autCont The autCont to set.
     */
    private void setHbmAutCont(AUTContPO autCont) {
        m_autCont = autCont;
    }

    /**
     * 
     * @return the AUT container.
     */
    @Transient
    public IAUTContPO getAutCont() {
        return getHbmAutCont();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public ITestSuiteContPO getTestSuiteCont() {
        return getHbmTestSuiteCont();
    }

    /**
     * @param testSuiteCont The testSuiteCont to set.
     */
    private void setTestSuiteCont(ITestSuiteContPO testSuiteCont) {
        setHbmTestSuiteCont((TestSuiteContPO)testSuiteCont);
        getHbmTestSuiteCont().setParentProjectId(getId());
    }

    /**
     *      
     * @return Returns the testSuiteCont.
     */
    @OneToOne(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @JoinColumn(name = "TS_CONT", unique = true)
    private TestSuiteContPO getHbmTestSuiteCont() {
        return m_testSuiteCont;
    }

    /**
     * @param testSuiteCont The testSuiteCont to set.
     */
    private void setHbmTestSuiteCont(TestSuiteContPO testSuiteCont) {
        m_testSuiteCont = testSuiteCont;
    }
    
    /**
     * {@inheritDoc}
     */
    @Transient
    public ITestJobContPO getTestJobCont() {
        return getHbmTestJobContPO();
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void setTestJobContPO(ITestJobContPO tjc) {
        setHbmTestJobContPO((TestJobContPO)tjc);
        getHbmTestJobContPO().setParentProjectId(getId());
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void setTestDataCubeContPO(ITestDataCubeContPO tdcc) {
        setHbmTestDataCubeContPO((TestDataCubeContPO)tdcc);
        getHbmTestDataCubeContPO().setParentProjectId(getId());
    }
    
    /**
     *      
     * @return Returns the testJobCont.
     */
    @OneToOne(cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @JoinColumn(name = "TJ_CONT", unique = true)
    private TestJobContPO getHbmTestJobContPO() {
        return m_testJobCont;
    }

    /**
     * @param testJobCont The testJobCont to set.
     */
    private void setHbmTestJobContPO(TestJobContPO testJobCont) {
        m_testJobCont = testJobCont;
    }
 

    /**
     * 
     * @return Returns the clientMetaDataVersion.
     */
    @Basic
    @Column(name = "CONF_MAJ_VERS")
    public Integer getClientMetaDataVersion() {
        return m_clientMetaDataVersion;
    }

    /**
     * @param metaDataVersion The metaDataVersion to set.
     */
    public void setClientMetaDataVersion(Integer metaDataVersion) {
        m_clientMetaDataVersion = metaDataVersion;
    }


    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public Set<IReusedProjectPO> getUsedProjects() {
        return Collections.unmodifiableSet(
            getProjectProperties().getUsedProjects());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public Integer getMajorProjectVersion() {
        return getProjectProperties().getMajorNumber();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public Integer getMinorProjectVersion() {
        return getProjectProperties().getMinorNumber();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Transient
    public boolean getIsReusable() {
        return getProjectProperties().getIsReusable();
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setIsReusable(boolean isReusable) {
        getProjectProperties().setIsReusable(isReusable);
    }
    

    /**
     * {@inheritDoc}
     */
    @Transient
    public boolean getIsProtected() {
        return getProjectProperties().getIsProtected();
    }

    /**
     * {@inheritDoc}
     */
    public void setIsProtected(boolean isProtected) {
        getProjectProperties().setIsProtected(isProtected);
    }

    /**
     * {@inheritDoc}
     */
    public void addUsedProject(IReusedProjectPO reusedProject) {
        getProjectProperties().addUsedProject(reusedProject);
    }

    /**
     * {@inheritDoc}
     */
    public void removeUsedProject(IReusedProjectPO project) {
        getProjectProperties().removeUsedProject(project);
    }
    
    /**
     * Sets the child node's parentProjectId equal to this node's id.
     * 
     * @param childNode The node that will have its parentProjectId set.
     */
    protected void setParentProjectIdForChildNode(INodePO childNode) {
        childNode.setParentProjectId(getId());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof IProjectPO) {
            IProjectPO proj = (IProjectPO)obj;
            return new EqualsBuilder()
                .append(getGuid(), proj.getGuid())
                .append(getMajorProjectVersion(), proj.getMajorProjectVersion())
                .append(getMinorProjectVersion(), proj.getMinorProjectVersion())
                .isEquals();
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getGuid())
            .append(getMajorProjectVersion())
            .append(getMinorProjectVersion())
            .toHashCode();
    }

    /**
     * This method is intended for use by the persistence layer and should do 
     * nothing other than return the properties.
     * 
     * @return Returns the project properties.
     */
    @OneToOne(cascade = CascadeType.ALL, 
               fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_PROPERTIES", unique = true)
    public ProjectPropertiesPO getProperties() {
        return m_projectProperties;
    }
    
    /**
     * This method is intended for use by the persistence layer and should do 
     * nothing other than assign the properties.
     * 
     * @param properties The new project properties.
     */
    private void setProperties(ProjectPropertiesPO properties) {
        m_projectProperties = properties;
    }
    
    /**
     * 
     * @param properties The new project properties.
     */
    private void setProjectProperties(IProjectPropertiesPO properties) {
        setProperties((ProjectPropertiesPO)properties);
        if (getProjectProperties() != null) {
            getProjectProperties().setParentProjectId(getId());
        }
    }

    /**
     * 
     * @return the project properties.
     */
    @Transient
    public IProjectPropertiesPO getProjectProperties() {
        return getProperties();
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    @Transient
    public LanguageHelper getLangHelper() {
        return getProjectProperties().getLangHelper();
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getToolkit() {
        return getProjectProperties().getToolkit();
    }

    /**
     * {@inheritDoc}
     */
    public void setToolkit(String toolkit) {
        getProjectProperties().setToolkit(toolkit);
    }

    /**
     * {@inheritDoc}
     */
    public void clearUsedProjects() {
        getProjectProperties().clearUsedProjects();
    }

    /**
     * 
     * @return a displayable name for the project. The returned String is of the
     *         form: [name]_[majorNumber].[minorNumber]
     */
    @Transient
    public String getDisplayName() {
        StringBuffer sb = new StringBuffer(getName());
        sb.append(IProjectPO.NAME_SEPARATOR).append(getVersionString());
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Transient
    public String getName() {
        return ProjectNameBP.getInstance().getName(getGuid());
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        //ProjectNameBP.getInstance().setName(getGuid(), name);
        log.debug(Messages.SetNameNotSupportedOnProjectPO);
    }

    /**
     * Sets parentProjectId for all existing child nodes.
     * {@inheritDoc}
     */
    public void setParentProjectId(Long projectId) {
        if (getProjectProperties() != null) {
            getProjectProperties().setParentProjectId(projectId);
        }
        if (getHbmAutCont() != null) {
            getHbmAutCont().setParentProjectId(projectId);
        }
        if (getHbmSpecObjCont() != null) {
            getHbmSpecObjCont().setParentProjectId(projectId);
        }
        if (getHbmTestSuiteCont() != null) {
            getHbmTestSuiteCont().setParentProjectId(projectId);
        }
        if (getHbmTestJobContPO() != null) {
            getHbmTestJobContPO().setParentProjectId(projectId);
        }
        if (getHbmTestDataCubeContPO() != null) {
            getHbmTestDataCubeContPO().setParentProjectId(projectId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public String getVersionString() {
        StringBuffer sb = new StringBuffer(
            String.valueOf(getMajorProjectVersion()));
        sb.append(IProjectPO.VERSION_SEPARATOR)
            .append(getMinorProjectVersion());
        return sb.toString();
    }
    
    /** {@inheritDoc}
     * @see org.eclipse.jubula.client.core.model.NodePO#isInterfaceLocked()
     */
    @Transient
    public Boolean isReused() {
        return true;
    }

    /**
     *      
     * @return the project attribute descriptions.
     */
    @ManyToMany(fetch = FetchType.EAGER, 
                cascade = CascadeType.ALL, 
                targetEntity = DocAttributeDescriptionPO.class)
    @JoinTable(name = "P_DOC_ATTR_DESC_LIST", 
               joinColumns = @JoinColumn(name = "DOC_ATTR_DESC"), 
               inverseJoinColumns = @JoinColumn(name = "PROJECT"))
    @OrderColumn(name = "IDX")
    List<IDocAttributeDescriptionPO> 
    getHbmProjectAttributeDescriptions() {
        return m_projectAttributeDescriptions;
    }
    /**
     * For Persistence (JPA / EclipseLink)
     * 
     * @param projectAttributeDescriptions The descriptions to set.
     */
    void setHbmProjectAttributeDescriptions(
            List<IDocAttributeDescriptionPO> projectAttributeDescriptions) {
        m_projectAttributeDescriptions = projectAttributeDescriptions;
    }

    /**
     *      
     * @return the test case attribute descriptions.
     */
    @ManyToMany(fetch = FetchType.EAGER, 
                cascade = CascadeType.ALL, 
                targetEntity = DocAttributeDescriptionPO.class)
    @JoinTable(name = "TC_DOC_ATTR_DESC_LIST", 
               joinColumns = @JoinColumn(name = "DOC_ATTR_DESC"), 
               inverseJoinColumns = @JoinColumn(name = "PROJECT"))
    @OrderColumn(name = "IDX")
    List<IDocAttributeDescriptionPO> 
    getHbmTestCaseAttributeDescriptions() {
        return m_testCaseAttributeDescriptions;
    }
    /**
     * For Persistence (JPA / EclipseLink)
     * 
     * @param testCaseAttributeDescriptions The descriptions to set.
     */
    void setHbmTestCaseAttributeDescriptions(
            List<IDocAttributeDescriptionPO> testCaseAttributeDescriptions) {
        m_testCaseAttributeDescriptions = testCaseAttributeDescriptions;
    }

    /**
     *      
     * @return the test suite attribute descriptions.
     */
    @ManyToMany(fetch = FetchType.EAGER, 
                cascade = CascadeType.ALL, 
                targetEntity = DocAttributeDescriptionPO.class)
    @JoinTable(name = "TS_DOC_ATTR_DESC_LIST", 
               joinColumns = @JoinColumn(name = "DOC_ATTR_DESC"), 
               inverseJoinColumns = @JoinColumn(name = "PROJECT"))
    @OrderColumn(name = "IDX")
    List<IDocAttributeDescriptionPO> 
    getHbmTestSuiteAttributeDescriptions() {
    
        return m_testSuiteAttributeDescriptions;
    }
    /**
     * For Persistence (JPA / EclipseLink)
     * 
     * @param testSuiteAttributeDescriptions The descriptions to set.
     */
    void setHbmTestSuiteAttributeDescriptions(
            List<IDocAttributeDescriptionPO> testSuiteAttributeDescriptions) {
        m_testSuiteAttributeDescriptions = testSuiteAttributeDescriptions;
    }

    /**
     * {@inheritDoc}
     */
    public void addProjectAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {
        
        getHbmProjectAttributeDescriptions().remove(attrDesc);
        getHbmProjectAttributeDescriptions().add(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    public void addTestCaseAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {

        getHbmTestCaseAttributeDescriptions().remove(attrDesc);
        getHbmTestCaseAttributeDescriptions().add(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    public void addTestSuiteAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {

        getTestSuiteAttributeDescriptions().remove(attrDesc);
        getTestSuiteAttributeDescriptions().add(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    public void removeProjectAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {

        getHbmProjectAttributeDescriptions().remove(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTestCaseAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {

        getTestCaseAttributeDescriptions().remove(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTestSuiteAttributeDescription(
            IDocAttributeDescriptionPO attrDesc) {

        getTestSuiteAttributeDescriptions().remove(attrDesc);
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public List<IDocAttributeDescriptionPO> getProjectAttributeDescriptions() {
        return Collections.unmodifiableList(
                getHbmProjectAttributeDescriptions());
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public List<IDocAttributeDescriptionPO> getTestCaseAttributeDescriptions() {
        return Collections.unmodifiableList(
                getHbmTestCaseAttributeDescriptions());
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public List<IDocAttributeDescriptionPO> 
    getTestSuiteAttributeDescriptions() {
    
        return Collections.unmodifiableList(
                getHbmTestSuiteAttributeDescriptions());
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public ITestDataCubeContPO getTestDataCubeCont() {
        return getHbmTestDataCubeContPO();
    }

    /**
     * @param tdcc The testdata cube container to set.
     */
    private void setHbmTestDataCubeContPO(TestDataCubeContPO tdcc) {
        m_testdatacubecont = tdcc;
    }
    
    /**
     *      
     * @return Returns the test data cube container.
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TDC_CONT", unique = true)
    private TestDataCubeContPO getHbmTestDataCubeContPO() {
        return m_testdatacubecont;
    }

    /**
     * {@inheritDoc}
     */
    public void setTestResultCleanupInterval(int noOfDays) {
        getProjectProperties().setTestResultCleanupInterval(noOfDays);
    }

    /**
     * {@inheritDoc}
     */
    @Transient
    public Integer getTestResultCleanupInterval() {
        return getProjectProperties().getTestResultCleanupInterval();
    }
}