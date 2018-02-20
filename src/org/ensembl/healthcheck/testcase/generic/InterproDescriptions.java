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
 * Check that there are Interpro descriptions, that each one has an xref, and
 * that the xref has a description.
 */

public class InterproDescriptions extends SingleDatabaseTestCase {
	

	/**
	 * Create a new InterproDescriptions testcase.
	 */
	public InterproDescriptions() {

		setDescription("Check that there are Interpro descriptions, that each one has an xref, and that the xref has a description.");
		setTeamResponsible(Team.GENEBUILD);
		removeAppliesToType(DatabaseType.OTHERFEATURES);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

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

		Connection con = dbre.getConnection();

		// check that there are no Interpro accessions without xrefs
		String sql = "SELECT count(*) FROM interpro i LEFT JOIN xref x ON i.interpro_ac=x.dbprimary_acc WHERE x.dbprimary_acc IS NULL";

		int rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {

			ReportManager
					.problem(
							this,
							con,
							"There are "
									+ rows
									+ " rows in the interpro table that have no associated xref");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"All Interpro accessions have xrefs");
		}

		// check that the description field is populated for all of them
		sql = "SELECT COUNT(*) FROM interpro i, xref x WHERE i.interpro_ac=x.dbprimary_acc AND (x.description IS NULL OR x.description = '')";

		rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {

			ReportManager.problem(this, con, "There are " + rows
					+ " Interpro xrefs with missing descriptions");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"All Interpro accessions have xref descriptions");
		}

		return result;

	} // run

} // InterproDescriptions
