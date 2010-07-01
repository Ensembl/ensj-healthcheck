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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.DatabaseType;

/**
 * Check that all the non-optional meta keys listed in the production database are present, and that all the meta keys are valid.
 */

public class ProductionMeta extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionMeta() {

		addToGroup("production");
		addToGroup("release");
		setDescription("Check that all the non-optional meta keys listed in the production database are present, and that all the meta keys are valid.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect meta keys.");
		setTeamResponsible("Release coordinator");

	}

    /**
	 * This test Does not apply to sanger_vega dbs
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

		boolean result = true;

		Connection con = dbre.getConnection();

		Connection productionCon = getProductionDatabase().getConnection();

		// we'll use a different query depending on the database type; also some keys are only for certain species
		String databaseType = dbre.getType().getName(); // will be core, otherfeatures etc
		String species = dbre.getSpecies().toString();  // will be homo_sapiens etc

		List<String> dbMetaKeys = getColumnValuesList(con, "SELECT DISTINCT(meta_key) FROM meta");

		// First check that keys present in database are all valid and current
		List<String> productionMetaKeys = getColumnValuesList(productionCon, "SELECT name FROM meta_key WHERE is_current = 1 AND FIND_IN_SET('" + databaseType + "', db_type) > 0 AND (FIND_IN_SET('" + species + "', only_for_species) > 0 OR only_for_species IS NULL)");

		// remove the list of valid keys from the list of keys in the database, the remainder (if any) are invalid
		Collection<String> dbOnly = CollectionUtils.subtract(dbMetaKeys, productionMetaKeys);

		if (!dbOnly.isEmpty()) {

			ReportManager.problem(this, con, dbre.getName() + " contains the following meta keys which are missing from " + getProductionDatabase().getName() + ": " + StringUtils.join(dbOnly, ","));
			result = false;

		} else {

			ReportManager.correct(this, con, "Set of meta keys matches the current valid list in the production database.");

		}
		
		// now check that all non-optional keys in production database appear here
		dbMetaKeys = getColumnValuesList(con, "SELECT DISTINCT(meta_key) FROM meta");

		productionMetaKeys = getColumnValuesList(productionCon, "SELECT name FROM meta_key WHERE is_current = 1 AND is_optional = 0 AND FIND_IN_SET('" + databaseType + "', db_type) > 0 AND (FIND_IN_SET('" + species + "', only_for_species) > 0 OR only_for_species IS NULL)");
		
		// remove the keys in the database from the non-optional list, any remaining in the non-optional list are missing from the database
		Collection<String> productionOnly = CollectionUtils.subtract(productionMetaKeys, dbMetaKeys);

		if (!productionOnly.isEmpty()) {

			ReportManager.problem(this, con, "Missing required meta keys: " + StringUtils.join(productionOnly, ","));
			result = false;

		} else {

			ReportManager.correct(this, con, "All current required meta keys are present.");

		}
		return result;

	} // run

} // ProductionBiotypes
