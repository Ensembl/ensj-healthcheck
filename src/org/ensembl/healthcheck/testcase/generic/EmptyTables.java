/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Check that all tables have data.
 */
public class EmptyTables extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of EmptyTablesTestCase
	 */
	public EmptyTables() {

		addToGroup("post_genebuild");
		// by default tests in the generic package apply to core, vega, est and estgene databases
		removeAppliesToType(DatabaseType.EST);
		removeAppliesToType(DatabaseType.ESTGENE);
		setDescription("Checks that all tables have data");

	}

	//---------------------------------------------------------------------

	/**
	 * Define what tables are to be checked. 
	 */
	private String[] getTablesToCheck(DatabaseRegistryEntry dbre) {

		String[] tables = getTableNames(dbre.getConnection());

		// the following tables are allowed to be empty
		String[] allowedEmpty = {"alt_allele", "assembly_exception", "dnac"};
		tables = remove(tables, allowedEmpty);
		
		// only rat has entries in QTL tables
		if (dbre.getSpecies() != Species.RATTUS_NORVEGICUS) {
			String[] qtlTables = {"qtl", "qtl_feature", "qtl_synonym"};
			tables = remove(tables, qtlTables);
		}

		// TODO more database type/species checks

		return tables;

	}

	//---------------------------------------------------------------------

	/**
	 * Check that every table has more than 0 rows.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] tables = getTablesToCheck(dbre);
		Connection con = dbre.getConnection();

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			//logger.finest("Checking that " + table + " has rows");

			if (!tableHasRows(con, table)) {

				ReportManager.problem(this, con, table + " has zero rows");
				result = false;

			}
		}

		return result;

	} // run

	// -----------------------------------------------------------------

	private String[] remove(String[] tables, String table) {

		String[] result = new String[tables.length - 1];
		int j = 0;
		for (int i = 0; i < tables.length; i++) {
			if (!tables[i].equalsIgnoreCase(table)) {
				result[j++] = tables[i];
			}
		}

		return result;

	}

	//	-----------------------------------------------------------------

	private String[] remove(String[] src, String[] tablesToRemove) {

		String[] result = src;

		for (int i = 0; i < tablesToRemove.length; i++) {
			result = remove(result, tablesToRemove[i]);
		}

		return result;

	}

	// -----------------------------------------------------------------

} // EmptyTablesTestCase
