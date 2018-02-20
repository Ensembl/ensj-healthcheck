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
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

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

		logger.fine("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE seq_start > seq_end");
		if (rows > THRESHOLD) {
			result = false;
			ReportManager.problem(this, con, rows + " protein features have seq_start > seq_end");
		}

		logger.fine("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE seq_start < 0");
		if (rows > THRESHOLD) {
                        result = false;
			ReportManager.problem(this, con, rows + " protein features have seq_start < 0");
		}

		logger.fine("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_start < 0");
		if (rows > THRESHOLD) {
                        result = false;
			ReportManager.problem(this, con, rows + " protein features have hit_start < 0");
		}

		logger.fine("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE hit_start > hit_end");
		if (rows > THRESHOLD) {
			result = false;
			ReportManager.problem(this, con, rows + " protein features have hit_start > hit_end");
		}

		return result;

	} // run

} // ProteinFeatures
