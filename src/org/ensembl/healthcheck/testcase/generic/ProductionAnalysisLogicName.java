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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

public class ProductionAnalysisLogicName extends AbstractTemplatedTestCase {

	public ProductionAnalysisLogicName() {
		addToGroup("production");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		addToGroup("post-projection");

		setDescription(
				"Check that the content of the analysis logic names in the core databases are subsets of production");
		setPriority(Priority.AMBER);
		setEffect("Discrepancies between tables can cause problems");
		setFix("Resync tables");
		setTeamResponsible(Team.GENEBUILD);
		setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
		removeAppliesToType(DatabaseType.VEGA);
	}

	@Override
  protected boolean runTest(DatabaseRegistryEntry dbre) {
    boolean result = true;
    String species = dbre.getSpecies();
    if (species == null || species.equalsIgnoreCase("unknown")) {
      species = dbre.getAlias();
    }    
    String databaseType = dbre.getType().getName();
    Set<String> coreLogicNames = getLogicNamesDb(dbre);
    Set<String> productionLogicNames = getLogicNamesFromProduction(dbre, species, databaseType);
    Set<String> coreDbVersion = getDbVersionCore(dbre);
    Set<String> productionDbVersion = getDbVersionProduction(dbre, coreLogicNames);
    result &= checkHasDbVersion(dbre, productionDbVersion, coreDbVersion, databaseType);
    result &= checkHasDbVersion(dbre, coreDbVersion, productionDbVersion, "production");
    if(!dbre.isMultiSpecies()) {
    	// checks are inappropriate for a multispecies database where individual mappings
    	// are not stored in the production database
    	result &= testForIdentity(dbre, coreLogicNames, productionLogicNames, "production");
    	result &= testForIdentity(dbre, productionLogicNames, coreLogicNames, databaseType);
    }
    return result;
  }

	/**
	 * Minuses the elements in the second collection from the first. Anything
	 * remaining in the first collection cannot exist in the second set
	 */
	private <T extends CharSequence> boolean testForIdentity(DatabaseRegistryEntry dbre, Collection<T> core,
			Collection<T> toRemove, String type) {
		Set<T> missing = new HashSet<T>(core);
		missing.removeAll(toRemove);
		if (missing.isEmpty()) {
			return true;
		}
		for (CharSequence name : missing) {
			String msg = String.format("The logic name '%s' is missing from %s", name, type);
			ReportManager.problem(this, dbre.getConnection(), msg);
		}
		return false;
	}

	private <T extends CharSequence> boolean checkHasDbVersion(DatabaseRegistryEntry dbre, Collection<T> core,
			Collection<T> production, String type) {
		Set<T> missing = new HashSet<T>(core);
		missing.removeAll(production);
		if (missing.isEmpty()) {
			return true;
		}
		for (CharSequence name : missing) {
			String msg = String.format("Analysis '%s' in %s db should have a dbversion", name, type);
			ReportManager.problem(this, dbre.getConnection(), msg);
		}
		return false;
	}

	private Set<String> getLogicNamesDb(DatabaseRegistryEntry dbre) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String sql = "select logic_name from analysis join analysis_description using (analysis_id)";
		List<String> results = t.queryForDefaultObjectList(sql, String.class);
		return new HashSet<String>(results);
	}

	private Set<String> getLogicNamesFromProduction(DatabaseRegistryEntry dbre, String species, String databaseType) {
		SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
		String sql = "select logic_name from analysis_description ad, species s, analysis_web_data aw where ad.analysis_description_id = aw.analysis_description_id and aw.species_id = s.species_id and s.db_name = '"
				+ species + "' and aw.db_type = '" + databaseType + "' and s.is_current = 1 and ad.is_current = 1";
		List<String> results = t.queryForDefaultObjectList(sql, String.class);
		return new HashSet<String>(results);
	}

	private Set<String> getDbVersionCore(DatabaseRegistryEntry dbre) {
		SqlTemplate t = DBUtils.getSqlTemplate(dbre);
		String sql = "SELECT logic_name FROM analysis where !isnull(db_version)";
		List<String> results = t.queryForDefaultObjectList(sql, String.class);
		return new HashSet<String>(results);
	}

	private Set<String> getDbVersionProduction(DatabaseRegistryEntry dbre, Set<String> analysisList) {
		SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
		Set<String> results = new HashSet<String>();
		for (String analysis : analysisList) {
			String sql = "SELECT logic_name FROM analysis_description where logic_name = '" + analysis
					+ "' and db_version = 1";
			results.addAll(t.queryForDefaultObjectList(sql, String.class));
		}
		return results;
	}

}
