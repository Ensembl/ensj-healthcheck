/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Base class to compare a certain set of things (e.g. biotypes, xrefs) from one database with the equivalent things in the previous
 * database.
 * 
 * Extending classes should implement the description, threshold and getCounts() methods. See individual Javadocs for details.
 */

public abstract class ComparePreviousVersionBase extends SingleDatabaseTestCase {

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		if (System.getProperty("ignore.previous.checks") != null) {
			logger.finest("ignore.previous.checks is set in database.properties, skipping this test");
			return true;
		}

                boolean skipCondition = skipCondition(dbre);

                if (skipCondition) {
                         logger.finest("Skipping test as data is not yet available");
                         return true;
                }

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		if (sec == null) {
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is " + sec.getName());

		Map currentCounts = getCounts(dbre);
		Map secondaryCounts = getCounts(sec);

		// compare each of the secondary (previous release, probably) with current
		Set externalDBs = secondaryCounts.keySet();
		Iterator it = externalDBs.iterator();
		String successText = "";

		// show % tolerance here?
		double tolerance = (100 - ((threshold() / 1) * 100));

		if (testUpperThreshold()) {
			successText = " - within tolerance +/-" + tolerance + "%";

		} else {
			successText = " - greater or within tolerance";
		}

		while (it.hasNext()) {

			String key = (String) it.next();

			int secondaryCount = ((Integer) (secondaryCounts.get(key))).intValue();

			if (secondaryCount == 0) {
				continue;
			}

			// check it exists at all
			if (currentCounts.containsKey(key)) {

				int currentCount = ((Integer) (currentCounts.get(key))).intValue();

				if (((double) currentCount / (double) secondaryCount) < threshold() && secondaryCount > minimum()) {
					ReportManager.problem(this, dbre.getConnection(), sec.getName() + " has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " only has " + currentCount);
					result = false;
				} else if (testUpperThreshold() &&
				// ((1 -(double) secondaryCount / (double) currentCount)) > threshold()) {
						(((double) currentCount / (double) secondaryCount)) > (1 / threshold()) && secondaryCount > minimum()) {
					ReportManager.problem(this, dbre.getConnection(), sec.getName() + " only has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " has " + currentCount);
					result = false;
				}

			} else {
				ReportManager.problem(this, dbre.getConnection(), sec.getName() + " has " + secondaryCount + " " + entityDescription() + " " + key + " but " + dbre.getName() + " has none");
				result = false;
			}
		}
		return result;

	} // run

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCountsBySQL(DatabaseRegistryEntry dbre, String sql) {

		Map<String, Integer> result = new HashMap<String, Integer>();

		try {

			Statement stmt = dbre.getConnection().createStatement();

			logger.finest("Getting " + entityDescription() + " counts for " + dbre.getName());

			ResultSet rs = stmt.executeQuery(sql);

			while (rs != null && rs.next()) {
				result.put(rs.getString(1), rs.getInt(2));
				logger.finest(rs.getString(1) + " " + rs.getInt(2));
			}

			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	// ----------------------------------------------------------------------

	/**
	 * Should return a map where the keys are the names of the entities being tested (e.g. biotypes) and the values are the counts of
	 * each type.
	 */
	protected abstract Map getCounts(DatabaseRegistryEntry dbre);

	// ------------------------------------------------------------------------
	/**
	 * Should return a description of what's being tested.
	 */
	protected abstract String entityDescription();

	// ------------------------------------------------------------------------
	/**
	 * Should return the fraction (0-1) of old/new below which a warning is generated.
	 */
	protected abstract double threshold();

	// ------------------------------------------------------------------------

        protected abstract double minimum();

        // ------------------------------------------------------------------------

	protected boolean testUpperThreshold() {
		return false;
	}

        // ------------------------------------------------------------------------

        protected boolean skipCondition(DatabaseRegistryEntry dbre) {
                return false;
        }

} // ComparePreviousVersionBase
