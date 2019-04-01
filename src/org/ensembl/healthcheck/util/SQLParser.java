/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.ensembl.healthcheck.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utilities for parsing a SQL file.
 */
public class SQLParser {

	/** Internal list of lines parsed from the file */
	private List lines;

	/** Logger object to use */
	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	/** Creates a new instance of SQLParser */
	public SQLParser() {
		lines = new ArrayList();
	}

	// -------------------------------------------------------------------------
	/**
	 * Parse a file containing SQL.
	 * 
	 * @param fileName
	 *          The name of the file to parse.
	 * @return A list of SQL commands read from the file.
	 * @throws FileNotFoundException
	 *           If fileName cannot be found.
	 */
	public List parse(String fileName) throws FileNotFoundException {

		File file = new File(fileName);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}

		// the file may have SQL statements spread over several lines
		// so line in file != SQL statement
		StringBuffer sql = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		try {
			while ((line = br.readLine()) != null) {

				line = line.trim();

				// skip comments, blank lines and example sql command lines
				if (line.startsWith("#") || line.length() == 0 || line.startsWith("@") || line.startsWith("--")) {
					continue;
				}

				// remove trailing comments
				int commentIndex = line.indexOf("#");
				if (commentIndex > -1) {
					line = line.substring(0, commentIndex);
				}

				if (line.endsWith(";")) { // if we've hit a semi-colon, that's
																	// the end of the SQL statement
					sql.append(line.substring(0, line.length() - 1)); // chop
																														// off ;
					lines.add(sql.toString());
					logger.finest("Added SQL statement beginning " + Utils.truncate(sql.toString(), 80, false));
					sql = new StringBuffer(); // ready for the next one

				} else {

					sql.append(line);

				}
			}

			br.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return lines;

	}

	// -------------------------------------------------------------------------
	/**
	 * Fill a SQL Statement with a set of batch commands from the SQL file.
	 * 
	 * @param stmt
	 *          The statement to be filled.
	 * @return A statement with addBatch() called for each command in the parsed SQL file.
	 */
	public Statement populateBatch(Statement stmt) {

		if (stmt == null) {
			logger.severe("SQLParser: input statement is NULL");
		}

		Statement result = stmt;

		Iterator it = lines.iterator();
		while (it.hasNext()) {
			String line = (String) it.next();
			try {
				result.addBatch(line);
				logger.finest("Added line begining " + Utils.truncate(line, 80, false) + " to batch");
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

		return result;

	}

	// -------------------------------------------------------------------------

	/**
	 * Getter for property lines.
	 * 
	 * @return Value of property lines.
	 */
	public java.util.List getLines() {
		return lines;
	}

	/**
	 * Setter for property lines.
	 * 
	 * @param lines
	 *          New value of property lines.
	 */
	public void setLines(java.util.List lines) {
		this.lines = lines;
	}

	// -------------------------------------------------------------------------
	/**
	 * Dump the SQL commands to stdout.
	 */
	public void printLines() {

		Iterator it = lines.iterator();
		while (it.hasNext()) {
			System.out.println((String) it.next());
		}

	}

	// -------------------------------------------------------------------------

}
