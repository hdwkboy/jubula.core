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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class CSVImportFilter implements IDataImportFilter {
	/** Define supported file extension for CSV file */
	private String[] fileExtensions = { "csv" }; //$NON-NLS-1$

	public String[] getFileExtensions() {
		return fileExtensions;
	}

	/**
	 * parses a *.csv file and returns the data as DataTable structure
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
		/*
		 * Determine the path of the csv file and return an opened file
		 */
		final File dataFile = findDataFile(dataDir, file);
		DataTable dataTable = null;

		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					dataFile));
			ArrayList<String[]> matrix = new ArrayList<String[]>();
			int numberOfPropertiesKeys = -1;
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				String[] properties = getProperties(strLine);
				if (numberOfPropertiesKeys == -1) {
					numberOfPropertiesKeys = properties.length;
				}
				if (properties.length == numberOfPropertiesKeys)
					matrix.add(properties);
			}

			if (matrix.size() > 0) {
				dataTable = new DataTable(matrix.size(), numberOfPropertiesKeys);
				for (int i = 0; i < matrix.size(); i++) {
					String[] entries = matrix.get(i);
					for (int j = 0; j < entries.length; j++) {
						dataTable.updateDataEntry(i, j,
								stripDoubleQuotes(entries[j]));
					}
				}
			}
		} catch (IOException e) {
			throw e;
		}
		return dataTable;
	}

	/**
	 * 
	 * @param entry
	 *            Text to be stripped
	 * @return text that has double-quote stripped
	 */
	private String stripDoubleQuotes(String entry) {
		String processedStr = entry;
		if (entry.startsWith("\"") && entry.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
			processedStr = entry.substring(1, entry.length() - 1);
		}
		return processedStr;
	}

	/**
	 * 
	 * @param line
	 *            Line read from csv file
	 * @return An array of properties separated by comma
	 */
	private String[] getProperties(String line) {
		String[] propertiesList = null;
		propertiesList = line.split(","); //$NON-NLS-1$
		return propertiesList;
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
	private File findDataFile(File dataDir, String file)
			throws FileNotFoundException {
		File dataFile = new File(file);
		File infile;
		if (dataFile.isAbsolute()) {
			infile = dataFile;
		} else {
			infile = new File(dataDir, file);
		}
		return infile;
	}

}
