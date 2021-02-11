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

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all the non-optional meta keys listed in the production database are present, and that all the meta keys are valid.
 */

public class ProductionMeta extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionMeta() {

		setDescription("Check that all the non-optional meta keys listed in the production database are present, and that all the meta keys are valid.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect meta keys.");
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
  @SuppressWarnings("unchecked")
  public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		DatabaseRegistryEntry prodDbre = getProductionDatabase();

		// we'll use a different query depending on the database type; also some keys are only for certain species
		String databaseType = dbre.getType().getName(); // will be core, otherfeatures etc

		List<String> dbMetaKeys = DBUtils.getColumnValuesList(con, "SELECT DISTINCT(meta_key) FROM meta");

		// First check that keys present in database are all valid and current
		List<String> productionMetaKeys =
				DBUtils.getColumnValuesList(prodDbre.getConnection(),
                    "SELECT mk.name " +
                    "FROM meta_key mk " +
                    "WHERE FIND_IN_SET('" + databaseType + "', mk.db_type) > 0 AND " +
                    "mk.is_current = 1");

		// remove the list of valid keys from the list of keys in the database, the remainder (if any) are invalid
		Collection<String> dbOnly = (Collection<String>)CollectionUtils.subtract(dbMetaKeys, productionMetaKeys);

		if (!dbOnly.isEmpty()) {
		  for(String key: dbOnly) {
		    String msg = String.format("Meta key '%s' is not in the allowed meta key list from production", key);
		    ReportManager.problem(this, con, msg);
		  }
			result = false;

		} else {

			ReportManager.correct(this, con, "Set of meta keys matches the current valid list in the production database.");

		}

		// now check that all non-optional keys in production database appear here
		dbMetaKeys = DBUtils.getColumnValuesList(con, "SELECT DISTINCT(meta_key) FROM meta");

		productionMetaKeys =
				DBUtils.getColumnValuesList(prodDbre.getConnection(),
                    "SELECT mk.name " +
                    "FROM meta_key mk " +
                    "WHERE FIND_IN_SET('" + databaseType + "', mk.db_type) > 0 AND " +
                    "mk.is_current = 1 AND " +
                    "mk.is_optional = 0");

		// remove the keys in the database from the non-optional list, any remaining in the non-optional list are missing from the
		// database
		Collection<String> productionOnly = (Collection<String>)CollectionUtils.subtract(productionMetaKeys, dbMetaKeys);

		if (!productionOnly.isEmpty()) {
		  for(String key: productionOnly) {
        String msg = String.format("Missing required meta key: %s", key);
        ReportManager.problem(this, con, msg);
      }
			result = false;

		} else {

			ReportManager.correct(this, con, "All current required meta keys are present.");

		}
		return result;

	} // run

} // ProductionBiotypes
