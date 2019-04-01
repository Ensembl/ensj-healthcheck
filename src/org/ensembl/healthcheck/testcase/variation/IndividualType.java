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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks the source table to make sure it is OK. Only for mouse databse, to
 * check external sources
 * 
 */
public class IndividualType extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckSourceDataTableTestCase
	 */
	public IndividualType() {


		setDescription("Check that the individuals have the correct type for each specie");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * 
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {
		boolean result = true;

		Connection con = dbre.getConnection();

		if (dbre.getSpecies().equals(DatabaseRegistryEntry.MUS_MUSCULUS)) {
			int mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM individual WHERE individual_type_id <> 1");
			if (mc > 0) {
				ReportManager.problem(this, con, "Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager.correct(this, con, "Individual type table correct in mouse");
			}
		}

		if (dbre.getSpecies().equals(DatabaseRegistryEntry.CANIS_FAMILIARIS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.DANIO_RERIO)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.GALLUS_GALLUS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.RATTUS_NORVEGICUS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.BOS_TAURUS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.ORNITHORHYNCHUS_ANATINUS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.PONGO_ABELII)) {
			int mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM individual WHERE individual_type_id <> 2");
			if (mc > 0) {
				ReportManager.problem(this, con, "Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager.correct(this, con, "Individual type table correct in " + dbre.getSpecies());
			}
		}

		if (dbre.getSpecies().equals(DatabaseRegistryEntry.ANOPHELES_GAMBIAE)) {
			int mc = DBUtils.getRowCount(con,
					"SELECT COUNT(*) FROM individual WHERE individual_type_id <> 2 and individual_type_id <> 3");
			if (mc > 0) {
				ReportManager.problem(this, con, "Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager.correct(this, con, "Individual type table correct in " + dbre.getSpecies());
			}

		}

		if (dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.PAN_TROGLODYTES)
				|| dbre.getSpecies().equals(DatabaseRegistryEntry.TETRAODON_NIGROVIRIDIS)) {
			int mc = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM individual WHERE individual_type_id <> 3");
			if (mc > 0) {
				ReportManager.problem(this, con, "Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager.correct(this, con, "Individual type table correct in " + dbre.getSpecies());
			}
		}

		return result;
	} // run

	// --------------------------------------------------------------
}
