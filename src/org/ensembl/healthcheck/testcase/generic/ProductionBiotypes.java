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

/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;


/**
 * Check that the gene and transcript biotypes match the valid current ones in the production database.
 */

public class ProductionBiotypes extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionBiotypes() {

		setDescription("Check that the gene and transcript biotypes match the valid current ones in the production database.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect biotypes.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This test Does not apply to sangervega dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
  public boolean run(DatabaseRegistryEntry dbre) {
    String databaseType = dbre.getType().getName(); // will be core, otherfeatures etc
    Set<String> coreBiotypes = getBiotypesDb(dbre, new String[]{"gene", "transcript"});
    Set<String> productionBiotypes = getBiotypesProduction(dbre, databaseType);
    return checkBiotypeExists(dbre, coreBiotypes, productionBiotypes, "production");
  }

  private <T extends CharSequence> boolean checkBiotypeExists(DatabaseRegistryEntry dbre, Collection<T> core, Collection<T> production, String type) {
    Set<T> missing = new HashSet<T>(core);
    missing.removeAll(production);
    if(missing.isEmpty()) {
      ReportManager.correct(this, dbre.getConnection(), "Set of biotypes matches the current valid list in the production database.");
      return true;
    }
    for(CharSequence name: missing) {
      String msg = String.format("The biotype '%s' is missing from %s", name, type);
      ReportManager.problem(this, dbre.getConnection(), msg);
    }
    return false;
  }

  private Set<String> getBiotypesDb(DatabaseRegistryEntry dbre, String[] tables) {
    SqlTemplate t = DBUtils.getSqlTemplate(dbre);
    Set<String> results = new HashSet<String>();
    for (String table : tables) {
      String sql = "SELECT DISTINCT(biotype) FROM " + table;
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }

  private Set<String> getBiotypesProduction(DatabaseRegistryEntry dbre, String databaseType) {
    SqlTemplate t = DBUtils.getSqlTemplate(getProductionDatabase());
    String[] tables = { "gene", "transcript" };
    Set<String> results = new HashSet<String>();
    for (String table : tables) {
      String sql = "SELECT name FROM biotype WHERE object_type='" + table + "' AND FIND_IN_SET('" + databaseType + "', db_type) > 0";
      results.addAll(t.queryForDefaultObjectList(sql, String.class));
    }
    return results;
  }
  
}
