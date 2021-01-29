/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * Check that HGNC_curated_genes xrefs are on genes, _transcript are on
 * transcript etc.
 */

public class HGNCTypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of HGNCTypes.
	 */
	public HGNCTypes() {

		setDescription("Check that HGNC_curated_genes xrefs are on genes, _transcript are on transcript etc");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// note these are looking for the *wrong* assignments
		result &= checkType(con, "HGNC_curated_gene", "Transcript");
		result &= checkType(con, "HGNC_automatic_gene", "Transcript");
		result &= checkType(con, "HGNC_curated_transcript", "Gene");
		result &= checkType(con, "HGNC_curated_transcript", "Gene");

		return result;

	}

	// ----------------------------------------------------------------------

	private boolean checkType(Connection con, String source, String wrongObject) {

		boolean result = true;

		int rows = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(*) FROM xref x, external_db e, object_xref ox WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND e.db_name='"
								+ source
								+ "' AND ox.ensembl_object_type='"
								+ wrongObject + "'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " " + source
					+ " xrefs are assigned to " + wrongObject.toLowerCase()
					+ "s");
			result = false;

		} else {

			ReportManager.correct(this, con, "All " + source
					+ " xrefs assigned to correct object type");
		}

		return result;

	}

	// ----------------------------------------------------------------------

}
