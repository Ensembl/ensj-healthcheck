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

		addToGroup("variation");
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
				|| dbre.getSpecies() == Species.ANOPHELES_GAMBIAE
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
