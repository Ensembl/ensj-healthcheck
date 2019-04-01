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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that cigar lines in the identity_xref table are in the same format as
 * they are in the alignment tables, i.e. start with a number rather than a
 * letter.
 */

public class IdentityXrefCigarLines extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Ditag.
	 */
	public IdentityXrefCigarLines() {

		setDescription("Check that cigar lines in the identity_xref table are in the same format, as they are in the alignment tables, i.e. start with a number rather than a letter");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);

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
	 * Test various things about ditag features.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int rows = DBUtils
				.getRowCount(con,
						"SELECT COUNT(*) FROM identity_xref WHERE cigar_line REGEXP '^[MDI]'");

		if (rows > 0) {

			ReportManager
					.problem(
							this,
							con,
							rows
									+ " cigar lines in identity_xref appear to be in the wrong format (number first)");
			result = false;

		} else {

			ReportManager
					.correct(this, con,
							"All cigar lines in identity_xref are in the correct format");
		}

		return result;

	}

	// ----------------------------------------------------------------------

}
