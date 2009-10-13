/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
