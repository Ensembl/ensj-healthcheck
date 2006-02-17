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
 * Check that the number of KNOWN & NOVEL genes is within 20% in the new and previous databases.
 */

public class GeneStatus extends SingleDatabaseTestCase {

	// fraction of KNOWN & NOVEL genes that are allowed to be different
	private static double THRESHOLD = 0.2;

	/**
	 * Create a new GeneStatus testcase.
	 */
	public GeneStatus() {

		addToGroup("release");
		setDescription("Check that the number of KNOWN genes is within 20% in the new and previous databases");

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
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		DatabaseRegistryEntry sec = getEquivalentFromSecondaryServer(dbre);

		if (sec == null) {
			logger.warning("Can't get equivalent database for " + dbre.getName());
			return true;
		}

		logger.finest("Equivalent database on secondary server is " + sec.getName());

		//String[] stats = { "KNOWN", "NOVEL" };
		String[] stats = { "KNOWN" };

		for (int i = 0; i < stats.length; i++) {

			String status = stats[i];

			String sql = "SELECT COUNT(*) FROM gene WHERE status='" + status + "'";
			int current = getRowCount(dbre.getConnection(), sql);
			int previous = getRowCount(sec.getConnection(), sql);

			// if there are no KNOWN genes at all, fail
			if (status.equals("KNOWN") && current == 0) {

				ReportManager.problem(this, con, "No genes have status " + status);
				return false;

			}

			// otherwise check ratios
			if (previous == 0) { // avoid division by zero
			
				ReportManager.warning(this, con, "Previous count of " + status + " genes is 0, skipping");
				return false;
				
			}
			
			double difference = (double)(previous - current) / (double)previous;

			//System.out.println(previous + " " + current + " " + difference);
			
			if (difference > THRESHOLD) {

				ReportManager.problem(this, con, "Only " + current + " genes have " + status
						+ " status in the current database, compared with " + previous + " in the previous database");
				result = false;

			}

			ReportManager.correct(this, con, "Current database has " + current + " genes of status " + status + " compared to " + previous + " in the previous database, which is within the allowed tollerance.");
			
		}
	
		return result;

	} // run

	// ----------------------------------------------------------------------

} // GeneStatus

