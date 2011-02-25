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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the number of KNOWN & NOVEL genes is within 20% in the new and
 * previous databases. Also check for unset statuses in genes & transcripts.
 * Also check that all KNOWN and KNOWN_BY_PROJECTION genes have display_srefs set
 */

public class GeneStatus extends SingleDatabaseTestCase {

	// fraction of KNOWN & NOVEL genes that are allowed to be different
	private static double THRESHOLD = 0.2;

	/**
	 * Create a new GeneStatus testcase.
	 */
	public GeneStatus() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that the number of KNOWN genes & transcripts is within 20% in the new and previous databases. Also check for unset status.");
                setTeamResponsible("Core Genebuild");

	}

	/**
	 * Don't try to run on Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);

	}

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
		
		if (dbre.getType() != DatabaseType.OTHERFEATURES &&dbre.getType() != DatabaseType.RNASEQ) {
			checkPrevious(dbre);
		}

		result &= checkNull(dbre);

		result &= checkDisplayXrefs(dbre);
		
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
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is " + sec.getName());

		String[] types = { "gene", "transcript" };

		for (int t = 0; t < types.length; t++) {

			String type = types[t];

			String[] stats = { "KNOWN" };

			for (int i = 0; i < stats.length; i++) {

				String status = stats[i];

				String sql = "SELECT COUNT(*) FROM " + type + " WHERE status='" + status + "'";
				int current = getRowCount(dbre.getConnection(), sql);
				int previous = getRowCount(sec.getConnection(), sql);

				// if there are no KNOWN genes at all, fail
				if (status.equals("KNOWN") && current == 0) {

					ReportManager.problem(this, con, "No " + type + "s have status " + status);
					return false;

				}

				// otherwise check ratios
				if (previous == 0) { // avoid division by zero

					ReportManager.warning(this, con, "Previous count of " + status + " " + type + "s is 0, skipping");
					return false;

				}

				double difference = (double) (previous - current) / (double) previous;

				logger.finest(type + ": previous " + previous + " current " + current + " difference ratio " + difference);

				if (difference > THRESHOLD) {

					ReportManager.problem(this, con, "Only " + current + " " + type + "s have " + status
							+ " status in the current database, compared with " + previous + " in the previous database");
					result = false;

				} else {

					ReportManager.correct(this, con, "Current database has " + current + " " + type + "s of status " + status
							+ " compared to " + previous + " in the previous database, which is within the allowed tollerance.");

				}

			}

		}

		return result;
	}

	// ----------------------------------------------------------------------

	private boolean checkNull(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] types = { "gene", "transcript" };

		for (int i = 0; i < types.length; i++) {

			result &= checkNoNulls(con, types[i], "status");
			
		}

		return result;

	}

//----------------------------------------------------------------------

	private boolean checkDisplayXrefs(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] statuses = { "KNOWN", "KNOWN_BY_PROJECTION" };

		for (int i = 0; i < statuses.length; i++) {

			String status = statuses[i];
			
			int total = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE status='" + status + "'");

			int rows = getRowCount(con, "SELECT COUNT(*) FROM gene WHERE status='" + status + "' AND display_xref_id IS NULL");

			if (rows > 0) {

				ReportManager.problem(this, con, rows + " genes of status " + status + " have NULL display_xrefs (out of a total of " + total + ")");
				result = false;
				
			} else {

				ReportManager.correct(this, con, "No null display_xrefs of status " + status);
			}

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // GeneStatus

