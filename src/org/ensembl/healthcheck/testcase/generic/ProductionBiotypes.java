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
import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the gene and transcript biotypes match the valid current ones in the production database.
 */

public class ProductionBiotypes extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public ProductionBiotypes() {

		addToGroup("production");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that the gene and transcript biotypes match the valid current ones in the production database.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect biotypes.");
		setTeamResponsible(Team.RELEASE_COORDINATOR);

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

		// we'll use a different query depending on the database type
		String databaseType = dbre.getType().getName(); // will be core, otherfeatures etc

		String[] tables = { "gene", "transcript" };

		Connection con = dbre.getConnection();

		Connection productionCon = getProductionDatabase().getConnection();

		for (String table : tables) {

			List<String> dbBiotypes = DBUtils.getColumnValuesList(con, "SELECT DISTINCT(biotype) FROM " + table);

			List<String> productionBiotypes = DBUtils.getColumnValuesList(productionCon, "SELECT name FROM biotype WHERE object_type='" + table + "' AND is_current = 1 AND FIND_IN_SET('" + databaseType
					+ "', db_type) > 0");

			// remove the list of valid biotypes from the list of biotypes in the database, the remainder (if any) are invalid
			Collection<String> dbOnly = CollectionUtils.subtract(dbBiotypes, productionBiotypes);

			if (!dbOnly.isEmpty()) {

				ReportManager.problem(this, con, String.format("%ss in %s have the following biotypes which are missing from %s: %s", StringUtils.capitalize(table), dbre.getName(), getProductionDatabase()
						.getName(), StringUtils.join(dbOnly, ",")));
				result = false;

			} else {

				ReportManager.correct(this, con, "Set of " + table + " biotypes matches the current valid list in the production database.");

			}

		}

		return result;

	} // run

} // ProductionBiotypes
