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
		setDescription("Check that the gene and transcript biotypes match the valid current ones in the production database.");
		setPriority(Priority.AMBER);
		setEffect("Unknown/incorrect biotypes.");
		setTeamResponsible("Release coordinator");

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

		String[] tables = { "gene", "transcript" };

		Connection con = dbre.getConnection();

		Connection productionCon = getProductionDatabase().getConnection();

		for (String table : tables) {

			List<String> dbBiotypes = Arrays.asList(getColumnValues(con, "SELECT DISTINCT(biotype) FROM " + table));

			List<String> productionBiotypes = Arrays.asList(getColumnValues(productionCon, "SELECT name FROM biotype WHERE object_type='" + table + "' AND is_current = 1"));

			// remove the list of valid biotypes from the list of biotypes in the database, the remainder (if any) are invalid
			Collection<String> dbOnly = CollectionUtils.subtract(dbBiotypes, productionBiotypes);

			if (!dbOnly.isEmpty()) {
				
				ReportManager.problem(this, con, dbre.getName() + " contains the following biotypes which are missing from " + getProductionDatabase().getName() + ": " + StringUtils.join(dbOnly, ","));
				result = false;
	
			} else {
				
				ReportManager.correct(this, con, "Set of " + table + " biotypes matches the current valid list in the production database.");
				
			}

		}

		return result;

	} // run

} // ProductionBiotypes
