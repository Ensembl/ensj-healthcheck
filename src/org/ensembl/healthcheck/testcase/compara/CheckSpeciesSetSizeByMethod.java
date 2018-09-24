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

package org.ensembl.healthcheck.testcase.compara;

import java.util.List;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class CheckSpeciesSetSizeByMethod extends AbstractComparaTestCase {

	// Maps method_link_species_set_id to size
	protected HashMap<String,String> sizeExceptions = new HashMap<String,String>();

	// Singletons and pairs should follow a certain pattern
	protected Pattern unaryPattern = Pattern.compile("^([A-Z]\\.?[a-z0-9]{2,3}) ");
	protected Pattern binaryPattern = Pattern.compile("^([A-Z]\\.?[a-z0-9]{2,3})-([A-Z]\\.?[a-z0-9]{2,3}) ");

	public CheckSpeciesSetSizeByMethod() {
		setTeamResponsible(Team.COMPARA);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks that the species-sets have the expected number of genomes");
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		populateExceptions(dbre);
		boolean result = true;
		result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_ORTHOLOGUES", 2);
		if (! isMasterDB(dbre.getConnection())) {
			// In the master db, we still have between-species paralogues
			result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_PARALOGUES", 1);
		}
		result &= assertSpeciesSetCountForMLSS(dbre, "ENSEMBL_HOMOEOLOGUES", 1);
		result &= assertSpeciesSetCountForMLSS(dbre, "BLASTZ_NET", 2);
		result &= assertSpeciesSetCountForMLSS(dbre, "LASTZ_NET", 2);
		result &= assertSpeciesSetCountForMLSS(dbre, "TRANSLATED_BLAT_NET", 2);
		return result;
	}

	protected void populateExceptions(DatabaseRegistryEntry dbre) {
		String sql = "SELECT method_link_species_set_id, value FROM method_link_species_set_tag WHERE tag = 'species_set_size'";
		for(String[] a : DBUtils.getRowValuesList(dbre.getConnection(), sql)) {
			sizeExceptions.put(a[0], a[1]);
		}
	}

	protected boolean assertMLSSNameNomenclature(DatabaseRegistryEntry dbre, String[] mlss, int expectedCount) {
		Pattern p = null;
		if (expectedCount == 1) {
			p = unaryPattern;
		} else if (expectedCount == 2) {
			p = binaryPattern;
		}
		if (p != null) {
			Matcher m = p.matcher(mlss[1]);
			if (!m.find()) {
				ReportManager.problem(this, dbre.getConnection(),
						String.format("The MLSS '%s' (ID %s) doesn't follow the name nomemclature '%s'",
							mlss[1], mlss[0], m.pattern()));
				return false;
			}
		}
		return true;
	}

	protected boolean assertSpeciesSetCountForMLSS(DatabaseRegistryEntry dbre, String methodLinkType, int expectedCount) {
		String sql = String.format(
			"SELECT method_link_species_set_id, name, COUNT(*) AS cnt" +
				" FROM method_link_species_set JOIN method_link USING (method_link_id) JOIN species_set USING (species_set_id)" +
				" WHERE type = '%s'" +
				" GROUP BY method_link_species_set_id",
			methodLinkType);
		List <String[]> allMLSSs = DBUtils.getRowValuesList(dbre.getConnection(), sql);
		boolean result = true;
		for (String [] thisMLSS : allMLSSs) {
			int expected = sizeExceptions.containsKey(thisMLSS[0]) ? Integer.parseInt(sizeExceptions.get(thisMLSS[0])) : expectedCount;
			result &= assertMLSSNameNomenclature(dbre, thisMLSS, expected);
			if (Integer.parseInt(thisMLSS[2]) == expected) {
				continue;
			}

			result = false;
			ReportManager.problem(this, dbre.getConnection(),
					String.format("The MLSS '%s' (ID %s) has %s GenomeDBs in its species-set instead of %d",
						thisMLSS[1], thisMLSS[0], thisMLSS[2], expected));
		}
		return result;
	}
}
