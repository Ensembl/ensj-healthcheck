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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
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

		addToGroup("variation-release");

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

		if (dbre.getSpecies() == Species.MUS_MUSCULUS) {
			int mc = DBUtils
					.getRowCount(con,
							"SELECT COUNT(*) FROM individual WHERE individual_type_id <> 1");
			if (mc > 0) {
				ReportManager.problem(this, con,
						"Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager.correct(this, con,
						"Individual type table correct in mouse");
			}
		}

		if (dbre.getSpecies() == Species.CANIS_FAMILIARIS
				|| dbre.getSpecies() == Species.DANIO_RERIO
				|| dbre.getSpecies() == Species.GALLUS_GALLUS
				|| dbre.getSpecies() == Species.RATTUS_NORVEGICUS
				|| dbre.getSpecies() == Species.BOS_TAURUS
				|| dbre.getSpecies() == Species.ORNITHORHYNCHUS_ANATINUS
				|| dbre.getSpecies() == Species.PONGO_ABELII) {
			int mc = DBUtils
					.getRowCount(con,
							"SELECT COUNT(*) FROM individual WHERE individual_type_id <> 2");
			if (mc > 0) {
				ReportManager.problem(this, con,
						"Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager
						.correct(this, con, "Individual type table correct in "
								+ dbre.getSpecies());
			}
		}
		
		if (dbre.getSpecies() == Species.ANOPHELES_GAMBIAE) {
			int mc = DBUtils
					.getRowCount(con,
							"SELECT COUNT(*) FROM individual WHERE individual_type_id <> 2 and individual_type_id <> 3");
			if (mc > 0) {
				ReportManager.problem(this, con,
						"Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager
						.correct(this, con, "Individual type table correct in "
								+ dbre.getSpecies());
			}
			
		}
		
		if (dbre.getSpecies() == Species.HOMO_SAPIENS
				|| dbre.getSpecies() == Species.PAN_TROGLODYTES
				|| dbre.getSpecies() == Species.TETRAODON_NIGROVIRIDIS) {
			int mc = DBUtils
					.getRowCount(con,
							"SELECT COUNT(*) FROM individual WHERE individual_type_id <> 3");
			if (mc > 0) {
				ReportManager.problem(this, con,
						"Individual type incorrect in Individual table");
				result = false;
			} else {
				ReportManager
						.correct(this, con, "Individual type table correct in "
								+ dbre.getSpecies());
			}
		}

		return result;
	} // run

	// --------------------------------------------------------------
}
