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
package org.eclipse.jubula.client.core.preferences.database;

import org.eclipse.jubula.tools.constants.StringConstants;
import org.eclipse.persistence.config.BatchWriting;

/**
 * 
 * @author BREDEX GmbH
 * @created 04.02.2011
 */
public class PostGreSQLConnectionInfo extends AbstractHostBasedConnectionInfo {
    /**
     * the JDBC driver class name 
     */
    public static final String DRIVER_CLASS_NAME = "org.postgresql.Driver"; //$NON-NLS-1$
    
    /**
     * the JDBC connection prefix 
     */
    public static final String JDBC_PRE = "jdbc:postgresql://"; //$NON-NLS-1$
    
    /**
     * Constructor
     */
    public PostGreSQLConnectionInfo() {
        super(5432);
    }
    
    @Override
    public String getConnectionUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(JDBC_PRE)
            .append(getHostname())
            .append(StringConstants.COLON)
            .append(getPort())
            .append(StringConstants.SLASH)
            .append(getDatabaseName());
        return urlBuilder.toString();
    }

    @Override
    public String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }
    
    @Override
    public String getBatchWriting() {
        return BatchWriting.JDBC;
    }
}
