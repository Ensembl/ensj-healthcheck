/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck;

import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.util.Utils;

/**
 * Utility to aid in debugging database regular expressions by listing which databases match a regular expression given on the
 * command line.
 */

public class DatabaseNameMatcher {

	private String databaseRegexp = "";

	// -------------------------------------------------------------------------
	/**
	 * Command-line entry point.
	 * 
	 * @param args
	 *          Arguments.
	 */
	public static void main(final String[] args) {

		DatabaseNameMatcher dnm = new DatabaseNameMatcher();

		dnm.parseCommandLine(args);

		Utils.readPropertiesFileIntoSystem("database.properties", false);

		dnm.showMatches();

	} // main

	// -------------------------------------------------------------------------
	private void parseCommandLine(final String[] args) {

		if (args.length == 0) {
			
			System.out.println("\nUsage: DatabaseNameMatcher regexp\n");
			System.exit(1);
			
		} else {
			
			databaseRegexp = args[0];
			
		}

	} // parseCommandLine

	// -------------------------------------------------------------------------
	/**
	 * Show the databases that have names that match the regexp.
	 */
	public final void showMatches() {

		List<String> regexps = new ArrayList<String>();
		regexps.add(databaseRegexp);

		DatabaseRegistry registry = new DatabaseRegistry(regexps, null, null, false);

		DatabaseRegistryEntry[] databases = registry.getAll();

		if (databases.length > 0) {
			
			System.out.println("\n" + databases.length + " database names matched " + databaseRegexp + " :");
			
			for (DatabaseRegistryEntry dbre : databases) {
				System.out.println("\t" + dbre.getName());
			}
			
		} else {
			
			System.out.println("Warning: No database names matched");
			
		}

	} // showMatches

	// -------------------------------------------------------------------------

} // DatabaseNameMatcher
