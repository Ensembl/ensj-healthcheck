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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Test case to compare table structures between several schemas.
 * 
 * Has several ways of deciding which schema to use as the "master" to compare all the others against:
 * <p>
 * <ol>
 * <li>If the property schema.file in database.properties exists, the table.sql file it points to</li>
 * <li>If the schema.file property is not present, the schema named by the property schema.master is used</li>
 * <li>If neither of the above properties are present, the (arbitrary) first schema is used as the master.</li>
 * </ol>
 */

public class CompareVariationSchema extends MultiDatabaseTestCase {
	
	private HashMap exceptions;
	
	/**
	 * Creates a new instance of CompareSchemaTestCase.
	 */
	public CompareVariationSchema() {

		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Will check if database schema is correct");
		setTeamResponsible(Team.VARIATION);
		this.defineExceptions();
	}

	/**
	 * Define a few exceptions for species
	 */
	private void defineExceptions() {
		
		HashMap speciesExceptions;
		ArrayList tables;
		this.exceptions = new HashMap();
		
		// Define the exceptions by species and the relationship with the master schema
		
		/*
		 * Exceptions common for all species
		 */
		speciesExceptions = new HashMap();
		this.exceptions.put(new String("all"),speciesExceptions);
		
		// Tables that are missing from master schema but required to be present in species
		tables = new ArrayList();
		tables.add("subsnp_map");
		speciesExceptions.put("requiredInSpecies",tables);

		/** 
		 * Exceptions for human
		 */
		speciesExceptions = new HashMap();
		this.exceptions.put(Species.HOMO_SAPIENS,speciesExceptions);
		
		// Tables that are in the master schema but may be missing from the species
		tables = new ArrayList();
		tables.add("tmp_individual_genotype_single_bp");
		speciesExceptions.put(new String("notRequiredInSpecies"),tables);

		/** 
		 * Exceptions for chimp
		 */
		speciesExceptions = new HashMap();
		this.exceptions.put(Species.PAN_TROGLODYTES,speciesExceptions);
		
		// Tables that are in the master schema but may be missing from the species
		tables = new ArrayList();
		tables.add("tmp_individual_genotype_single_bp");
		speciesExceptions.put(new String("notRequiredInSpecies"),tables);

		/** 
		 * Exceptions for mouse
		 */
		speciesExceptions = new HashMap();
		this.exceptions.put(Species.MUS_MUSCULUS,speciesExceptions);
		
		// Tables that are missing from master schema but required to be present in species
		tables = new ArrayList();
		tables.add("strain_gtype_poly");
		speciesExceptions.put(new String("requiredInSpecies"),tables);

		/** 
		 * Exceptions for rat
		 */
		speciesExceptions = new HashMap();
		this.exceptions.put(Species.RATTUS_NORVEGICUS,speciesExceptions);
		
		// Tables that are missing from master schema but required to be present in species
		tables = new ArrayList();
		tables.add("strain_gtype_poly");
		speciesExceptions.put(new String("requiredInSpecies"),tables);
		
	}
	
	private boolean isExcepted(Species sp, String table, String condition) {
		// Get the excepted tables for this species and condition and check whether the supplied table is excepted
		ArrayList tables = this.getExceptionTables(sp,condition);
		return tables.contains(table);
	}
	
	private ArrayList getExceptionTables(Species sp, String condition) {
		
		ArrayList tables = new ArrayList();
		if (this.exceptions == null) {
			return tables;
		}
		
		// Check the exceptions that concerns all species as well as the species-specific ones 
		Object[] excKey = new Object[] {new String("all"),sp};

		HashMap speciesExceptions;
		for (int i=0; i<excKey.length; i++) {
		
			speciesExceptions = (HashMap) this.exceptions.get(excKey[i]);
			if (speciesExceptions != null) {				
				ArrayList aL = (ArrayList) speciesExceptions.get(condition);
				if (aL != null) {
					tables.addAll(aL);
				}
			}
		}
		
		return tables;
		
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
		definitionFile = System.getProperty("schema.file");
		if (definitionFile == null) {
			logger.info("CompareSchema: No schema definition file found! Set schema.file property in database.properties if you want to use a table.sql file or similar.");

			masterSchema = System.getProperty("master.variation_schema");
			if (masterSchema != null) {
				logger.info("Will use " + masterSchema + " as specified master schema for comparisons.");
			} else {
				logger.info("CompareVariationSchema: No master schema defined file found! Set master.variation_schema property in database.properties if you want to use a master schema.");
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
					Species species = databases[i].getSpecies();
					
					if (checkCon != masterCon) {

						logger.info("Comparing " + DBUtils.getShortDatabaseName(masterCon) + " with " + DBUtils.getShortDatabaseName(checkCon));

						// check that both schemas have the same tables
						if (!compareVariationTables(masterCon, checkCon, species)) {
							// if there is a problem in that species (eg rat), report error
							ReportManager.problem(this, checkCon, "Table name discrepancy detected, skipping rest of checks");
							result = false;
							continue;
						}

						Statement dbStmt = checkCon.createStatement();

						// check each table in turn
						String[] tableNames = getTableNames(masterCon);
						// exclude the genotype table
						for (int j = 0; j < tableNames.length; j++) {
							String table = tableNames[j];
							
							// Check if this table is in the list of exceptions
							if (isExcepted(species,table,"notRequiredInSpecies")) {
								ReportManager.info(this, checkCon, "Table " + table + " is in the list of 'notRequiredInSpecies' exceptions for " + species.getAlias() + ", will not check table structure.");
								continue;
							}
							
							/*
							if (species != Species.HOMO_SAPIENS && (table.equals("variation_annotation") || table.equals("phenotype"))) {
								// only human has these two tables
								continue;
							}
							*/
							String sql = "SHOW CREATE TABLE " + table;
							ResultSet masterRS = masterStmt.executeQuery(sql);
							ResultSet dbRS = dbStmt.executeQuery(sql);
							boolean showCreateSame = DBUtils.compareResultSets(masterRS, dbRS, this, " [" + table + "]", true, false, table, true);
							if (!showCreateSame) {

								// do more in-depth analysis of database structure
								result &= compareTableStructures(masterCon, checkCon, table);
							}

							masterRS.close();
							dbRS.close();

						} // while table

						dbStmt.close();

					} // if checkCon != masterCon
					if (result) {
						// display some information the HC run with on problem
						ReportManager.correct(this, checkCon, "CompareVariationSchema run with no problem");
					}
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

	// Will return true if both schemas have the same tables (we have to consider the genotype)
	private boolean compareVariationTables(Connection schema1, Connection schema2, Species species) {
		boolean result = true;

		String name1 = DBUtils.getShortDatabaseName(schema1);
		String name2 = DBUtils.getShortDatabaseName(schema2);

		// check each table in turn
		String[] tables = getTableNames(schema1);
		// if the species is human or chimp, remove the genotype from the list of the master schema
		for (int i = 0; i < tables.length; i++) {
			String table = tables[i];

			// if the table is in the list of exceptions, skip it
			if (isExcepted(species,table,"notRequiredInSpecies")) {
				ReportManager.info(this, schema2, "Table " + table + " is in the list of 'notRequiredInSpecies' exceptions for " + species.getAlias() + ", will not check presence.");
				continue;
			}
			if (!checkTableExists(schema2, table)) {
				ReportManager.problem(this, schema2, "Table " + table + " exists in " + name1 + " but not in " + name2);
				result = false;
			}
		}
		// and now the other way
		tables = getTableNames(schema2);
		boolean strainTable = false;
		for (int i = 0; i < tables.length; i++) {
			String table = tables[i];
			
			// Don't report the table as missing if it is in the exception list of required tables not in master schema 
			if (!checkTableExists(schema1, table) && !isExcepted(species,table,"requiredInSpecies")) {
				ReportManager.problem(this, schema2, "Table " + table + " exists in " + name2 + " but not in " + name1);
				result = false;
			}
		}
		
		// Check the required tables that are not listed in the master schema
		ArrayList requiredTables = this.getExceptionTables(species,"requiredInSpecies");
		for (int i=0; i<requiredTables.size(); i++) {
			
			String table = (String) requiredTables.get(i);
			if (!checkTableExists(schema2, table)) {
				
				ReportManager.problem(this, schema2, "Table " + table + " does not exist in " + name2);
				result = false;
				
			}
		}
		
		return result;
	} // end method

	private boolean compareTableStructures(Connection con1, Connection con2, String table) {

		boolean result = true;

		try {

			Statement s1 = con1.createStatement();
			Statement s2 = con2.createStatement();

			// compare DESCRIBE <table>
			ResultSet rs1 = s1.executeQuery("DESCRIBE " + table);
			ResultSet rs2 = s2.executeQuery("DESCRIBE " + table);
			int[] columns = { 1, 2 };
			boolean describeSame = DBUtils.compareResultSets(rs1, rs2, this, "", true, false, table, columns, true);

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
					String str1 = table1 + ":" + rs1.getString("Key_name") + ":" + rs1.getString("Column_name") + ":" + rs1.getString("Non_unique") + ":" + rs1.getString("Seq_in_index");
					rows1.add(str1);
				}
			}

			SortedSet rows2 = new TreeSet();
			while (rs2.next()) {
				if (rs2.getInt("Seq_in_index") >= 1) {
					String str2 = table2 + ":" + rs2.getString("Key_name") + ":" + rs2.getString("Column_name") + ":" + rs2.getString("Non_unique") + ":" + rs2.getString("Seq_in_index");
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
				ReportManager.problem(this, "", DBUtils.getShortDatabaseName(con1) + " " + table + " has index " + index + " which is different or absent in " + DBUtils.getShortDatabaseName(con2));
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
				ReportManager.problem(this, "", DBUtils.getShortDatabaseName(con2) + " " + table + " has index " + index + " which is different or absent in " + DBUtils.getShortDatabaseName(con1));
			}

		} catch (SQLException se) {
			logger.severe(se.getMessage());
		}

		return result;
	}

	// -------------------------------------------------------------------------

	/**
	 * This only applies to variation databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VEGA);

	}

} // CompareSchema
