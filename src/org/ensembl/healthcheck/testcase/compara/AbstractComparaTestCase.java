/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Pair;
import org.ensembl.healthcheck.util.SqlUncheckedException;


/**
 * An extension of the SingleDatabaseTestCase that adds some Compara requirements
 *  - Some foreign keys actually link a table to itself
 *  - We need an easy way of getting the core database of each species
 */

public abstract class AbstractComparaTestCase extends SingleDatabaseTestCase {


	/**
	 * Verify foreign-key relations, and fills ReportManager with useful sql if
	 * necessary.
	 * 
	 * @param con
	 *            A connection to the database to be tested. Should already be
	 *            open.
	 * @param table
	 *            With col1, specifies the first key to check.
	 * @param col1
	 *            First column in table to check.
	 * @param col2
	 *            Second column in table to check.
	 * @param col1_can_be_null
	 *            Whether NULLs in the first column are allowed
	 * @return boolean true if everything is fine false otherwise
	 */
	public boolean checkForOrphansSameTable(Connection con, String table, String col1, String col2, boolean col1_can_be_null) {

		if (con == null) {
			logger.severe("countOrphans: Database connection is null");
			return false;
		}

		String sql = String.format(" FROM %s hc_table1 LEFT JOIN %s hc_table2 ON hc_table1.%s = hc_table2.%s WHERE hc_table2.%s IS NULL", table, table, col1, col2, col2);
		if (col1_can_be_null) {
			sql = sql + String.format(" AND hc_table1.%s IS NOT NULL", col1);
		}

		int orphans = DBUtils.getRowCount(con, "SELECT COUNT(*)" + sql);

		logger.finest("Orphans: " + orphans);

		if (orphans > 0) {
			String[] values = DBUtils.getColumnValues(con, "SELECT hc_table1." + col1 + sql + " LIMIT 20");
			for (String this_value : values) {
				ReportManager.info(this, con, table + "." + col1 + " " + this_value + " is not linked.");
			}

			ReportManager.problem(this, con, "FAILED " + table + " : " + col1 + " -> " + col2 + " using FK " + " relationships");
			ReportManager.problem(this, con, "FAILURE DETAILS: " + orphans + " " + table + " entries are not linked from " + col1 + " to " + col2);
			ReportManager.problem(this, con, "USEFUL SQL: SELECT hc_table1.*" + sql);
			return false;

		} else if (orphans < 0) {
			ReportManager.problem(this, con, "TEST NOT COMPLETED " + table + " : " + col1 + " -> " + col2 + " using FK, look at the StackTrace if any");
			return false;
		}

		return true;

	} // checkForOrphansSameTable

	public static class GenomeEntry {

		private final String name;
		private final Integer genome_db_id;
		private final DatabaseRegistryEntry coreDbre;
		private final Integer species_id;

		/**
		 * Constructor to set up properties of {@link GenomeEntry}
		 *
		 * @param name
		 * @param genome_db_id
		 * @param coreDbre
		 * @param species_id
		 */
		public GenomeEntry(String name, Integer genome_db_id, DatabaseRegistryEntry coreDbre, Integer species_id) {
			this.name = name;
			this.genome_db_id = genome_db_id;
			this.coreDbre = coreDbre;
			this.species_id = species_id;
		}

		public String getName() {
			return name;
		}

		public Integer getGenomeDBID() {
			return genome_db_id;
		}

		public DatabaseRegistryEntry getCoreDbre() {
			return coreDbre;
		}

		public Integer getSpeciesID() {
			return species_id;
		}

		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}

	}


	/**
	 * Get the list of all the genomes found in the Compara database and
	 * their core database info
	 *
	 * @return A list of @link GenomeEntry
	 */
	public final List<GenomeEntry> getAllGenomes(final DatabaseRegistryEntry comparaDbre, final DatabaseRegistry dbr) {

		HashMap<String, Pair<DatabaseRegistryEntry,Integer>> speciesCoreMap = new HashMap(); //<String, DatabaseRegistryEntry>();

		for (DatabaseRegistryEntry entry : dbr.getAllEntries()) {
			// We need to check the database name because some _cdna_
			// databases have the DatabaseType.CORE type
			// We also need to check the version number
			if (entry.getType().equals(DatabaseType.CORE) && entry.getName().contains("_core_") && entry.getSchemaVersion().equals(comparaDbre.getSchemaVersion())) {
				// There can be multiple species in the same core database
				for (Integer species_id : entry.getSpeciesIds()) {
					String sql = "SELECT meta_value FROM meta WHERE meta_key = \"species.production_name\" AND species_id = " + species_id;
					String production_name = getRowColumnValue(entry.getConnection(), sql);
					speciesCoreMap.put(production_name, new Pair<DatabaseRegistryEntry,Integer>(entry,species_id));
					ReportManager.info(this, comparaDbre.getConnection(), entry.toString() + " == " + production_name + " (" + species_id + ")");
				}
				try {
					entry.getConnection().close();
				} catch (SQLException se) {
					throw new SqlUncheckedException("Could not compare two result sets", se);
				}
			}
		}

		String sql = "SELECT name, genome_db_id FROM genome_db WHERE first_release IS NOT NULL AND last_release IS NULL AND genome_component IS NULL"
			+ " AND name <> 'ancestral_sequences'";
		List<String[]> data = DBUtils.getRowValuesList(comparaDbre.getConnection(), sql);

		Vector<GenomeEntry> genomeEntries = new Vector<GenomeEntry>();

		for (String[] row: data) {
			String species = row[0];
			Integer gdb_id = Integer.valueOf(row[1]);
			if (speciesCoreMap.containsKey(species)) {
				genomeEntries.add(new GenomeEntry(species, gdb_id, speciesCoreMap.get(species).a, speciesCoreMap.get(species).b));
				ReportManager.info(this, comparaDbre.getConnection(), species + "(" + gdb_id + ") : " + speciesCoreMap.get(species).a + " (" + speciesCoreMap.get(species).b + ")");
			} else {
				genomeEntries.add(new GenomeEntry(species, gdb_id, null, null));
				ReportManager.info(this, comparaDbre.getConnection(), species + "(" + gdb_id + ") : no core database");
			}
		}

		return genomeEntries;

	} // getSpeciesCoreDbMap


	/**
	 * Tells whether the current database being tested is a Master database
	 *
	 * @return a boolean
	 */
	public boolean isMasterDB(Connection con) {
		return DBUtils.getShortDatabaseName(con).contains(System.getProperty("compara_master.database"));
	}


	/**
	 * Return the DatabaseRegistryEntry of the Compara database
	 * corresponding to the previous release (n-1)
	 */
	public DatabaseRegistryEntry getLastComparaReleaseDbre(DatabaseRegistryEntry currentReleaseDbre) {

		// Get compara DB connection
		DatabaseRegistryEntry[] allSecondaryComparaDBs = DBUtils.getSecondaryDatabaseRegistry("compara").getAll(DatabaseType.COMPARA);

		String division_name = getComparaDivisionName(currentReleaseDbre);
		int previous_version_number = Integer.parseInt(currentReleaseDbre.getSchemaVersion())-1;

		for (DatabaseRegistryEntry this_other_Compara_dbre : allSecondaryComparaDBs) {
			if (Integer.parseInt(this_other_Compara_dbre.getSchemaVersion()) == previous_version_number
					&& division_name.equals(getComparaDivisionName(this_other_Compara_dbre))) {
				return this_other_Compara_dbre;
			}
		}
		return null;
	}


	// ensembl_compara_bacteria_3_56
	protected final static Pattern EGC_DB = Pattern.compile("^([^_]+_)?ensembl_compara_([a-z_]+)_[0-9]+_[0-9]+");
	// ensembl_compara_56
	protected final static Pattern EC_DB = Pattern.compile("^([^_]+_)?ensembl_compara_[0-9]+");

	String getComparaDivisionName(DatabaseRegistryEntry compara_dbre) {
		Matcher m = EGC_DB.matcher(compara_dbre.getName());
		if (m.matches()) {
			if (m.groupCount() > 1) {
				return m.group(2);
			} else {
				return m.group(1);
			}
		} else {
			m = EC_DB.matcher(compara_dbre.getName());
			if (m.matches()) {
				return "ensembl";
			} else {
				ReportManager.problem(this, compara_dbre.getConnection(), "Cannot find the division name of this database: '" + compara_dbre.getName() + "'");
				return null;
			}
		}
	}

} // AbstractComparaTestCase
