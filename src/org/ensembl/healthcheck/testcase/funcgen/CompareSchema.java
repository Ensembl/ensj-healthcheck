/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Test case to compare table structures between several schemas.
 * 
 * Has several ways of deciding which schema to use as the "master" to compare
 * all the others against:
 * <p>
 * <ol>
 * <li>If the property schema.file in database.properties exists, the table.sql
 * file it points to</li>
 * <li>If the schema.file property is not present, the schema named by the
 * property schema.master is used</li>
 * <li>If neither of the above properties are present, the (arbitrary) first
 * schema is used as the master.</li>
 * </ol>
 */

public class CompareSchema extends MultiDatabaseTestCase {

	/**
	 * Creates a new instance of CompareSchemaTestCase.
	 */
	public CompareSchema() {
		// can we set this dynamically give the dbtype?
		// This could be generated from the meta table?
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		setDescription("Compare two databases (table names, column names and types, and indexes. Note that there are occasionally pipeline tables (such as runnable, job, job_status etc) that are still present. It is wise to check that they are no longer needed before removing them.");

	}

	/**
	 * Can also be used on variation databases (and maybe others)
	 */
	public void types() {

		// addAppliesToType(DatabaseType.VARIATION);

	}

	/**
	 * Compare each database with the master.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean result = true;

		Connection masterCon = null;
		Statement masterStmt = null;

		String definitionFile = null;
		String masterSchema = null;

		DatabaseRegistryEntry[] databases = dbr.getAll();

		// Need to change this to funcgen_schema.file?
		definitionFile = System.getProperty("schema.file");
		if (definitionFile == null) {
			logger
					.info("CompareSchema: No schema definition file found! Set schema.file property in database.properties if you want to use a table.sql file or similar.");

			masterSchema = System.getProperty("master.funcgen_schema");
			if (masterSchema != null) {
				logger.info("Will use " + masterSchema + " as specified master schema for comparisons.");
			} else {
				logger.info("CompareSchema: No master schema defined file found! Set master.schema property in database.properties if you want to use a master schema.");
			}
		} else {
			logger.fine("Will use schema definition from " + definitionFile);
		}

		try {

			if (definitionFile != null) { // use a schema definition file to
				// generate a temporary database

				logger.info("About to import " + definitionFile);
				masterCon = importSchema(definitionFile);
				logger.info("Got connection to " + DBUtils.getShortDatabaseName(masterCon));

			} else if (masterSchema != null) {
				// use the defined schema name as the master
				// get connection to master schema
				masterCon = getSchemaConnection(masterSchema);
				logger.fine("Opened connection to master schema in " + DBUtils.getShortDatabaseName(masterCon));

			} else {
				// just use the first one to compare with all the others
				if (databases.length > 0) {
					masterCon = databases[0].getConnection();
					logger.info("Using " + DBUtils.getShortDatabaseName(masterCon) + " as 'master' for comparisions.");
				} else {
					logger.warning("Can't find any databases to check against");
				}

			}

			masterStmt = masterCon.createStatement();

			for (int i = 0; i < databases.length; i++) {

				if (appliesToType(databases[i].getType())) {

					Connection checkCon = databases[i].getConnection();

					if (checkCon != masterCon) {

						logger.info("Comparing " + DBUtils.getShortDatabaseName(checkCon) + " with " + DBUtils.getShortDatabaseName(masterCon));

						// check that both schemas have the same tables
						if (!compareTablesInSchema(checkCon, masterCon)) {
							// if not the same, this method will generate a
							// report
							ReportManager.problem(this, checkCon, "Table name discrepancy detected, skipping rest of checks");
							result = false;
							continue;
						}

						Statement dbStmt = checkCon.createStatement();

						// check each table in turn
						String[] tableNames = getTableNames(masterCon);
						for (int j = 0; j < tableNames.length; j++) {

							String table = tableNames[j];
							String sql = "SHOW CREATE TABLE " + table;
							ResultSet masterRS = masterStmt.executeQuery(sql);
							ResultSet dbRS = dbStmt.executeQuery(sql);
							boolean showCreateSame = DBUtils.compareResultSets(dbRS, masterRS, this, " [" + table + "]", false, false, table, true);
							if (!showCreateSame) {

								// do more in-depth analysis of database structure
								result &= compareTableStructures(checkCon, masterCon, table);

							}

							masterRS.close();
							dbRS.close();

						} // while table

						dbStmt.close();

					} // if checkCon != masterCon

				} // if appliesToType

			} // for database

			masterStmt.close();

		} catch (SQLException se) {

			logger.severe(se.getMessage());

		} finally {

			// avoid leaving temporary DBs lying around if something bad happens
			if (definitionFile == null && masterCon != null) {
				// double-check to make sure the DB we're going to remove is a
				// temp one
				String dbName = DBUtils.getShortDatabaseName(masterCon);
				if (dbName.indexOf("_temp_") > -1) {
					removeDatabase(masterCon);
					logger.info("Removed " + DBUtils.getShortDatabaseName(masterCon));
				}
			}

		}

		return result;

	} // run

	// -------------------------------------------------------------------------

	private boolean compareTableStructures(Connection con1, Connection con2, String table) {

		boolean result = true;

		try {

			Statement s1 = con1.createStatement();
			Statement s2 = con2.createStatement();

			// compare DESCRIBE <table>
			ResultSet rs1 = s1.executeQuery("DESCRIBE " + table);
			ResultSet rs2 = s2.executeQuery("DESCRIBE " + table);
			// DESC columns: 1: Field, 2: Type, 3: Allowed Null?, 4: Key, 5: Default, 6:Extra  
			int[] columns = { 1, 2, 3 };
			boolean describeSame = DBUtils.compareResultSets(rs1, rs2, this, " table descriptions for", true, false, table, columns, true);

			result &= describeSame;

			// compare indicies via SHOW INDEX <table>
			result &= compareIndices(con1, table, con2, table);

			s1.close();
			s2.close();

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return result;
	}

	// -------------------------------------------------------------------------
	/**
	 * Compare the indices on 2 tables; order is not important.
	 */
	private boolean compareIndices(Connection con1, String table1, Connection con2, String table2) {

		boolean result = true;

		try {

			Statement s1 = con1.createStatement();
			Statement s2 = con2.createStatement();

			ResultSet rs1 = s1.executeQuery("SHOW INDEX FROM " + table1);
			ResultSet rs2 = s2.executeQuery("SHOW INDEX FROM " + table2);

			// read ResultSets into concatenated Strings, then sort and compare
			// this wouldn't be necessary if we could ORDER BY in SHOW INDEX
			SortedSet rows1 = new TreeSet();
			while (rs1.next()) {
				if (rs1.getInt("Seq_in_index") >= 1) { // cuts down number of messages
					String str1 = table1 + ":" + rs1.getString("Key_name") + ":" + rs1.getString("Column_name") + ":"
							+ rs1.getString("Non_unique") + ":" + rs1.getString("Seq_in_index");
					rows1.add(str1);
				}
			}

			SortedSet rows2 = new TreeSet();
			while (rs2.next()) {
				if (rs2.getInt("Seq_in_index") >= 1) {
					String str2 = table2 + ":" + rs2.getString("Key_name") + ":" + rs2.getString("Column_name") + ":"
							+ rs2.getString("Non_unique") + ":" + rs2.getString("Seq_in_index");
					rows2.add(str2);
				}
			}

			rs1.close();
			rs2.close();
			s1.close();
			s2.close();

			HashMap problems1 = new HashMap();
			HashMap problems2 = new HashMap();

			// compare rows1 and rows2
			Iterator it1 = rows1.iterator();
			while (it1.hasNext()) {
				String row1 = (String) it1.next();
				if (!rows2.contains(row1)) {
					result = false;
					String[] indices = row1.split(":");
					String table = indices[0];
					String index = indices[1];
					String seq = indices[4];
					problems1.put(table + ":" + index, seq);
				}
			}

			Iterator p1 = problems1.keySet().iterator();
			while (p1.hasNext()) {
				String s = (String) p1.next();
				String[] indices = s.split(":");
				String table = indices[0];
				String index = indices[1];
				ReportManager.problem(this, con1, DBUtils.getShortDatabaseName(con1) + " " + table + " has index " + index
						+ " which is different or absent in " + DBUtils.getShortDatabaseName(con2));
			}

			// and the other way around
			Iterator it2 = rows2.iterator();
			while (it2.hasNext()) {
				String row2 = (String) it2.next();
				if (!rows1.contains(row2)) {
					result = false;
					String[] indices = row2.split(":");
					String table = indices[0];
					String index = indices[1];
					String seq = indices[4];
					problems2.put(table + ":" + index, seq);
				}
			}

			Iterator p2 = problems2.keySet().iterator();
			while (p2.hasNext()) {
				String s = (String) p2.next();
				String[] indices = s.split(":");
				String table = indices[0];
				String index = indices[1];
				ReportManager.problem(this, con2, DBUtils.getShortDatabaseName(con2) + " " + table + " has index " + index
						+ " which is different or absent in " + DBUtils.getShortDatabaseName(con1));
			}

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return result;
	}
	// -------------------------------------------------------------------------

} // CompareSchema
