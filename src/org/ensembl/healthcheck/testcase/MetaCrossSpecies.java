/*
  Copyright (C) 2003 EBI, GRL
 
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
package org.ensembl.healthcheck.testcase;

import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.TestResult;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.DatabaseConnectionIterator;

/**
 * Check meta table species, classification and taxonomy_id is the same
 * in all DBs for each species
 */

public class MetaCrossSpecies extends EnsTestCase {

	public MetaCrossSpecies() {
		addToGroup("post_genebuild");
		setDescription("Check meta table species, classification and taxonomy_id is the same in all DBs for each species");
	}

	/**
	 * Check various aspects of the meta table.
	 * @return Result.
	 */
	public TestResult run() {

		boolean result = true;

		String[] species = getListOfSpecies();

		for (int i = 0; i < species.length; i++) {

			String speciesRegexp = species[i] + CORE_DB_REGEXP_POSTFIX;
			logger.info("Checking meta tables in " + speciesRegexp);

			int nDatabases = 0;
			DatabaseConnectionIterator dcit = testRunner.getDatabaseConnectionIterator(speciesRegexp);
			while (dcit.hasNext()) {
				nDatabases++;
				dcit.next();
			}

			if (nDatabases > 1) { // no point checking against only one
				boolean allMatch = checkSameSQLResult("SELECT LCASE(meta_value) FROM meta WHERE meta_key LIKE \'species.%' ORDER BY meta_id", speciesRegexp);
				if (!allMatch) {
					result = false;
					ReportManager.problem(this, speciesRegexp, "meta information not the same for all " + species[i] + " databases (" + nDatabases + " checked)");
				} else {
					ReportManager.correct(this, speciesRegexp, "meta information is the same for all " + species[i] + " databases (" + nDatabases + " checked)");
				}
			}

		} // foreach species

		// -------------------------------------------

		return new TestResult(getShortTestName(), result);

	}

} // MetaCrossSpecies
