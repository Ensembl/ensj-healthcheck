/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that feature co-ords make sense.
 */
public class ProteinFeatures extends SingleDatabaseTestCase {

	private static int THRESHOLD = 1000;

	/**
	 * Creates a new instance of CheckFeatureCoordsTestCase
	 */
	public ProteinFeatures() {
		
		setDescription("Check that protein annotation feature coords make sense and that all translations exist in the database");
		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Iterate over each affected database and perform various checks.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passes.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		int rows = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE seq_start > seq_end");
		if (rows > THRESHOLD) {
			result = false;
			ReportManager.problem(this, con, rows + " protein features have seq_start > seq_end");
		} else {
			ReportManager.correct(this, con, "No protein features where seq_start > seq_end");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE seq_start < 0");
		if (rows > THRESHOLD) {
			ReportManager.problem(this, con, rows + " protein features have seq_start < 0");
		} else {
			ReportManager.correct(this, con, "No protein features where seq_start < 0");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_start < 0");
		if (rows > THRESHOLD) {
			ReportManager.problem(this, con, rows + " protein features have hit_start < 0");
		} else {
			ReportManager.correct(this, con, "No protein features where hit_start < 0");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE hit_start > hit_end");
		if (rows > THRESHOLD) {
			result = false;
			ReportManager.problem(this, con, rows + " protein features have hit_start > hit_end");
		} else {
			ReportManager.correct(this, con, "No protein features where hit_start > hit_end");
		}

		return result;

	} // run

} // ProteinFeatures
