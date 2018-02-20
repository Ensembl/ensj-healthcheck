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

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Check that the number of KNOWN & NOVEL genes is within 20% in the new and
 * previous databases. Also check for unset statuses in genes & transcripts.
 * Also check that all KNOWN and KNOWN_BY_PROJECTION genes have display_srefs
 * set
 */

public class GeneStatus extends SingleDatabaseTestCase {

	// fraction of KNOWN & NOVEL genes that are allowed to be different
	private static double THRESHOLD = 0.2;

	/**
	 * Create a new GeneStatus testcase.
	 */
	public GeneStatus() {

		setDescription("Check that the number of KNOWN genes & transcripts is within 20% in the new and previous databases. Also check for unset status.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Don't try to run on cDNA databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		if (dbre.getType() != DatabaseType.OTHERFEATURES
				&& dbre.getType() != DatabaseType.RNASEQ) {
			checkPrevious(dbre);
		}

		result &= checkNull(dbre);

		return result;

	} // run

	// ----------------------------------------------------------------------

	private boolean checkPrevious(DatabaseRegistryEntry dbre) {

		boolean result = true;

		if (System.getProperty("ignore.previous.checks") != null) {
			logger.finest("ignore.previous.checks is set in database.properties, skipping this test");
			return true;
		}

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		if (sec == null) {
			logger.warning("Can't get equivalent database for "
					+ dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is "
				+ sec.getName());

		String[] types = { "gene", "transcript" };

		for (int t = 0; t < types.length; t++) {

			String type = types[t];

			String[] stats = { "KNOWN" };

			for (int i = 0; i < stats.length; i++) {

				String status = stats[i];

				String sql = "SELECT COUNT(*) FROM " + type + " WHERE status='"
						+ status + "'";
				int current = DBUtils.getRowCount(dbre.getConnection(), sql);
				int previous = DBUtils.getRowCount(sec.getConnection(), sql);

				// if there are no KNOWN genes at all, fail
				if (status.equals("KNOWN") && current == 0) {

					ReportManager.problem(this, con, "No " + type
							+ "s have status " + status);
					return false;

				}

				// otherwise check ratios
				if (previous == 0) { // avoid division by zero

					ReportManager.warning(this, con, "Previous count of "
							+ status + " " + type + "s is 0, skipping");
					return false;

				}

				double difference = (double) (previous - current)
						/ (double) previous;

				logger.finest(type + ": previous " + previous + " current "
						+ current + " difference ratio " + difference);

				if (difference > THRESHOLD && previous > 100) {

					ReportManager.problem(this, con, "Only " + current + " "
							+ type + "s have " + status
							+ " status in the current database, compared with "
							+ previous + " in the previous database");
					result = false;

				} else {

					ReportManager
							.correct(
									this,
									con,
									"Current database has "
											+ current
											+ " "
											+ type
											+ "s of status "
											+ status
											+ " compared to "
											+ previous
											+ " in the previous database, which is within the allowed tollerance.");

				}

			}

		}

		return result;
	}

	private boolean checkNull(DatabaseRegistryEntry dbre) {
		boolean result = true;
		for (String type : new String[] { "gene", "transcript" }) {
			result &= checkNoNulls(dbre.getConnection(), type, "status");
		}
		return result;
	}

}
