/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.compara;

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class CheckSpeciesSetCountsByMethod extends AbstractComparaTestCase {

	public CheckSpeciesSetCountsByMethod() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks that the species-sets have the expected number of genomes");
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		boolean result = true;
		result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_ORTHOLOGUES", 2);
		if (! isMasterDB(dbre.getConnection())) {
			result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_PARALOGUES", 1);
		}
		result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_HOMOEOLOGUES", 2);
		result &= assertSpeciesSetCountForMLSS(dbre, "BLASTZ_NET", 2);
		result &= assertSpeciesSetCountForMLSS(dbre, "LASTZ_NET", 2);
		result &= assertSpeciesSetCountForMLSS(dbre, "TRANSLATED_BLAT_NET", 2);
		return result;
	}

	protected boolean assertSpeciesSetCountForMLSS(DatabaseRegistryEntry dbre, String methodLinkType, int expectedCount) {
		String sql = String.format(
			"SELECT method_link_species_set_id, name, COUNT(*) AS cnt" +
				" FROM method_link_species_set JOIN method_link USING (method_link_id) JOIN species_set USING (species_set_id)" +
				" WHERE type = '%s'" +
				" GROUP BY method_link_species_set_id" +
				" HAVING COUNT(*) != %d",
			methodLinkType,
			expectedCount);
		List <String[]> badMLSSs = DBUtils.getRowValuesList(dbre.getConnection(), sql);
		if (badMLSSs.size() > 0) {
			for (String [] thisBadMLSS : badMLSSs) {
				ReportManager.problem(this, dbre.getConnection(), 
						String.format("The MLSS '%s' (ID %s) has %s GenomeDBs in its species-set instead of %d",
							thisBadMLSS[1], thisBadMLSS[0], thisBadMLSS[2], expectedCount));
			}
			return false;
		} else {
			return true;
		}
	}
}
