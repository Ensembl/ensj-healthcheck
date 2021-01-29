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
 * Check that all species that should have projected xrefs do in fact have them.
 */
public class ProjectedXrefs extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of ProjectedXrefs.
	 */
	public ProjectedXrefs() {

		setDescription("Check that all species that should have projected xrefs do in fact have them.");
		setTeamResponsible(Team.CORE);

	}

	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.EST);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
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
		String species = dbre.getSpecies();

		if (species.equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				|| species.equals(DatabaseRegistryEntry.CAENORHABDITIS_ELEGANS)
				|| species.equals(DatabaseRegistryEntry.DROSOPHILA_MELANOGASTER)
				|| species.equals(DatabaseRegistryEntry.SACCHAROMYCES_CEREVISIAE)
				|| species.equals(DatabaseRegistryEntry.CIONA_INTESTINALIS)
				|| species.equals(DatabaseRegistryEntry.CIONA_SAVIGNYI)) {
			return result;
		}

		// check display xrefs

		int rows = DBUtils.getRowCount(con,
				"SELECT COUNT(*) FROM gene g, xref x WHERE g.display_xref_id=x.xref_id AND x.info_type='PROJECTION'");

		if (rows == 0) {
			ReportManager.problem(this, con, "No genes in " + species + " have projected display_xrefs");
			result = false;
		} else {
			ReportManager.correct(this, con, rows + " genes in " + species + " have projected display_xrefs");
		}

		return result;

	} // run

} // ProjectedXrefs
