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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check mappings from oligo probes to genome.
 * 
 * Even though we *don't* provide oligo data for all species the healthcheck
 * follows the convention of failing if the data is missing.
 * 
 * Note - currently disabled (doesn't apply to any dbs), may be migrated to eFG eventually.
 */
public class OligoProbes2Genome extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of OligoProbes2Genome
	 */
	public OligoProbes2Genome() {

		addToGroup("post_genebuild");
		addToGroup("release");

	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);

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

		Connection con = dbre.getConnection();

		if (testOligoTablesPopulated(dbre)) {
			return testProbsetSizesSet(con) & testOligoArraysInExternalDB(con) & testOligoFeatureInMetaCoord(con);
		} else {
			return false;
		}

	}

	private boolean testOligoArraysInExternalDB(Connection con) {

		boolean result = true;

		// We have to do some guessing and pattern matching to find the
		// external database corresponding to this OligoArray because the
		// names used in external_db.db_name do not quite match oligo_array.name.

		// 1 - get set of external_db.db_names
		String[] xdbNames = getColumnValues(con, "SELECT db_name FROM external_db");
		Set xdbNamesSet = new HashSet();
		for (int i = 0; i < xdbNames.length; i++)
			xdbNamesSet.add(xdbNames[i].toLowerCase());

		// 2 - check to see if every oligo_array.name is in the set of
		// external_db.db_names.
		String[] oligoArrayNames = getColumnValues(con, "SELECT name FROM oligo_array");
		for (int i = 0; i < oligoArrayNames.length; i++) {

			String name = oligoArrayNames[i];
			Set possibleExternalDBNames = new HashSet();
			possibleExternalDBNames.add(name.toLowerCase());
			possibleExternalDBNames.add(name.toLowerCase().replace('-', '_'));
			possibleExternalDBNames.add(("Affy_" + name).toLowerCase().replace('-', '_'));
			possibleExternalDBNames.add(("afyy_" + name).toLowerCase().replace('-', '_'));

			possibleExternalDBNames.retainAll(xdbNamesSet);

			if (possibleExternalDBNames.size() == 0) {
				ReportManager.problem(this, con, "OligoArray (oligo_array.name) " + name + " has no corresponding entry in external_db");
				result = false;
			}

		}

		return result;
	}

	/**
	 * Checks that all oligo_* tables are populated.
	 * 
	 * If at least one is not then the test fails.
	 * 
	 * @param con
	 * @return true if all oligo_* tables have rows, otherwise false.
	 */

	private boolean testOligoTablesPopulated(DatabaseRegistryEntry dbre) {

		List emptyTables = new ArrayList();

		String[] tables = { "oligo_array", "oligo_probe", "oligo_feature" };

		Species species = dbre.getSpecies();
		Connection con = dbre.getConnection();

		if (species == Species.HOMO_SAPIENS || species == Species.MUS_MUSCULUS || species == Species.RATTUS_NORVEGICUS
				|| species == Species.GALLUS_GALLUS || species == Species.DANIO_RERIO) {

			for (int i = 0; i < tables.length; i++)
				if (Integer.parseInt(getRowColumnValue(con, "SELECT count(*) from " + tables[i])) == 0)
					emptyTables.add(tables[i]);

		}
		if (emptyTables.size() == 0)
			return true;
		else {
			ReportManager.problem(this, con, "Empty table(s): " + emptyTables);
			return false;
		}

	}

	private boolean testProbsetSizesSet(Connection con) {

		boolean result = true;

		try {
			String sql = "SELECT name, probe_setsize FROM oligo_array";
			for (ResultSet rs = con.createStatement().executeQuery(sql); rs.next();) {
				int probesetSize = rs.getInt("probe_setsize");
				if (probesetSize < 1) {
					ReportManager.problem(this, con, "oligo_array.probe_setsize not set for " + rs.getString("name"));
					result = false;
				}
			}
		} catch (SQLException e) {
			result = false;
			e.printStackTrace();
		}

		return result;
	}

	private boolean testOligoFeatureInMetaCoord(Connection con) {

		boolean result = true;

		int oligos = getRowCount(con, "SELECT COUNT(*) FROM oligo_feature");
		if (oligos > 0) {
			String sql = "select count(*) from meta_coord where table_name='oligo_feature'";
			if (getRowCount(con, sql) == 0) {
				ReportManager.problem(this, con, "No entry for oligo_feature in meta_coord table. ");
				result = false;
			}
		}
		return result;
	}

}
