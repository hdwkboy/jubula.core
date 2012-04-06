/*******************************************************************************
 * Copyright (c) 2012 MontaVista Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MontaVista Software, Inc - Initial implementation of Java Properties file
 *
 *******************************************************************************/
package org.eclipse.jubula.client.core.businessprocess.importfilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;


public class PropertiesImportFilter implements IDataImportFilter {
	/** Define supported extensions for properties files **/
	private String[] fileExtensions = { "properties" }; //$NON-NLS-1$

	public String[] getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * parses a *.properties file and returns the data as DataTable structure
	 * 
	 * @param dataDir
	 *            directory for data files
	 * @param file
	 *            data source File
	 * @param locale
	 *            data source locale
	 * @return filled TestDataManager with new data
	 * @throws IOException
	 *             error occurred while reading data source
	 * 
	 */
	public DataTable parse(File dataDir, String file, Locale locale)
			throws IOException {
		Properties testProperties = new Properties();
		/* Determine the path of the properties file and return an opened 
		 * file */
		final FileInputStream dataFile = findDataFile(dataDir, file);
			testProperties.load(dataFile);

		int numberOfPropertiesKeys = testProperties.size();
		/*
		 * Create a new 2 by y, where y is the number of keys in the properties
		 * file.
		 */
		DataTable dataTable = null;
		if (numberOfPropertiesKeys > 0){
			dataTable = new DataTable(2, numberOfPropertiesKeys);
			/* Insert keys on first row and corresponding value on the 
			 * second role. Key/value pair is one-to-one
			 */
			Enumeration<Object> keys = testProperties.keys();
			int colIdx = 0;
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				/* Adding key */
				dataTable.updateDataEntry(0, colIdx, key);
				/* Adding value */
				dataTable.updateDataEntry(1, colIdx,
						testProperties.getProperty(key));
				/* Increment the column index */
				colIdx++;
			}
		}
		return dataTable;
	}

	/**
	 * Open a data file for reading
	 * 
	 * @param dataDir
	 *            the data directory
	 * @param file
	 *            the filename
	 * @return an opened FileInputStream for the filename
	 * @throws FileNotFoundException
	 *             guess when!
	 */
	private FileInputStream findDataFile(File dataDir, String file)
			throws FileNotFoundException {
		File dataFile = new File(file);
		File infile;
		if (dataFile.isAbsolute()) {
			infile = dataFile;
		} else {
			infile = new File(dataDir, file);
		}
		return new FileInputStream(infile);
	}
}
