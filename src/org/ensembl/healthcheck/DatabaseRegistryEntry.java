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
package org.ensembl.healthcheck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.ConnectionPool;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;
import org.ensembl.healthcheck.util.UtilUncheckedException;

/**
 * Container for information about a database that can be stored in a
 * DatabaseRegistry.
 */
public class DatabaseRegistryEntry implements Comparable<DatabaseRegistryEntry> {

	private static final String COLLECTION_CLAUSE = "_collection";

	/**
	 * Simple read-only bean to store pertinent information about a database.
	 * Objects of this type are held by the {@link DatabaseRegistryEntry} and
	 * attached to {@link ReportLine} objects to improve reporting
	 * 
	 * @author dstaines
	 */
	public static class DatabaseInfo {

		private final String name;
		private final String alias;
		private final Species species;
		private final DatabaseType type;
		private final String schemaVersion;
		private final String genebuildVersion;

		/**
		 * Constructor to set up key properties of {@link DatabaseInfo}
		 * 
		 * @param name
		 * @param species
		 * @param type
		 * @param schemaVersion
		 */
		public DatabaseInfo(String name, String alias, Species species,
				DatabaseType type, String schemaVersion, String genebuildVersion) {
			this.name = name;
			this.alias = alias;
			this.species = species;
			this.type = type;
			this.schemaVersion = schemaVersion;
			this.genebuildVersion = genebuildVersion;
		}

		public String getName() {
			return name;
		}

		public String getAlias() {
			return alias;
		}

		public Species getSpecies() {
			return species;
		}

		public DatabaseType getType() {
			return type;
		}

		public String getSchemaVersion() {
			return schemaVersion;
		}

		public String getGenebuildVersion() {
			return genebuildVersion;
		}

		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}

	}

        // e.g. username_species_type
        protected final static Pattern GB_DB = Pattern
                        .compile("^[a-z0-9]+_([a-z]+)_([A-Za-z]+)");
	// e.g. neurospora_crassa_core_4_56_1a
	protected final static Pattern EG_DB = Pattern
			.compile("^([a-zA-Z0-9_]+)_([a-z]+)_([0-9]+_[0-9]+)_([0-9A-Za-z]+)");
	// e.g. homo_sapiens_core_56_37a
	protected final static Pattern E_DB = Pattern
			.compile("^([a-z]+_[a-z0-9]+(?:_[a-z0-9]+)?)_([a-z]+)_([0-9]+)_([0-9A-Za-z]+)");
	// e.g. prefix_homo_sapiens_funcgen_60_37e
	protected final static Pattern PE_DB = Pattern
			.compile("^[^_]+_([^_]+_[^_]+)_([a-z]+)_([0-9]+)_([0-9A-Za-z]+)");
	// human_core_20, hsapiens_XXX
	protected final static Pattern EEL_DB = Pattern
			.compile("^([^_]+)_([a-z]+)_([0-9]+)");
	// ensembl_compara_bacteria_3_56
	protected final static Pattern EGC_DB = Pattern
			.compile("^(ensembl)_(compara)_[a-z_]+_[0-9]+_([0-9]+)");
	// ensembl_compara_56
	protected final static Pattern EC_DB = Pattern
			.compile("^(ensembl)_(compara)_([0-9]+)");
	// username_ensembl_compara_57
	protected final static Pattern UC_DB = Pattern
			.compile("^[^_]+_(ensembl)_(compara)_([0-9]+)");
	// username_ensembl_compara_master
	protected final static Pattern UCM_DB = Pattern
			.compile("^[^_]+_(ensembl)_(compara)_master");
	// ensembl_ancestral_57
	protected final static Pattern EA_DB = Pattern
			.compile("^(ensembl)_(ancestral)_([0-9]+)");
	// username_ensembl_ancestral_57
	protected final static Pattern UA_DB = Pattern
			.compile("^[^_]+_(ensembl)_(ancestral)_([0-9]+)");
	// ensembl_mart_56
	protected final static Pattern EM_DB = Pattern
			.compile("^([a-z_]+)_(mart)_([0-9])+");
	// username_species_type_version_release
	protected final static Pattern V_DB = Pattern
			.compile("vega_([^_]+_[^_]+)_[^_]+_([^_]+)_([^_]+)");
	protected final static Pattern EE_DB = Pattern
			.compile("^([^_]+_[^_]+)_[a-z]+_([a-z]+)_[a-z]+_([0-9]+)_([0-9A-Za-z]+)");
	// username_species_type_version_release
	protected final static Pattern U_DB = Pattern
			.compile("^[^_]+_([^_]+_[^_]+)_([a-z]+)_([0-9]+)_([0-9A-Za-z]+)");
	protected final static Pattern HELP_DB = Pattern
			.compile("^(ensembl)_(help)_([0-9]+)");
	protected final static Pattern EW_DB = Pattern
			.compile("^(ensembl)_(website)_([0-9]+)");
	protected final static Pattern TAX_DB = Pattern
			.compile("^(ncbi)_(taxonomy)_([0-9]+)");
	protected final static Pattern UD_DB = Pattern
			.compile("^([a-z_]+)_(userdata)");
	protected final static Pattern BLAST_DB = Pattern
			.compile("^([a-z_]+)_(blast)");
        protected final static Pattern MASTER_DB = Pattern
                        .compile("^(master_schema)_([a-z]+)?_([0-9]+)");
	protected final static Pattern MYSQL_DB = Pattern
			.compile("^(mysql|information_schema)");

	protected final static Pattern[] patterns = { EC_DB, UA_DB, UC_DB, UCM_DB,
			EA_DB, EGC_DB, EG_DB, E_DB, PE_DB, EM_DB, EE_DB, EEL_DB, U_DB,
			V_DB, MYSQL_DB, BLAST_DB, UD_DB, TAX_DB, EW_DB, HELP_DB, GB_DB, MASTER_DB };

	/**
	 * Utility for building a {@link DatabaseInfo} object given a name
	 * 
	 * @param name
	 * @return object containing information about a database
	 */
	public static DatabaseInfo getInfoFromName(String name) {
		return getInfoFromName(name, null, null);
	}

	/**
	 * <p>
	 * Returns information about a database. Queries the meta table to determine
	 * the type and schema version of the database.
	 * </p>
	 * 
	 * @param server
	 * @param name
	 * @return DatabaseInfo
	 */
	public static DatabaseInfo getInfoFromDatabase(DatabaseServer server,
			final String name) throws SQLException {
		SqlTemplate template = null;

		try {
			template = new ConnectionBasedSqlTemplateImpl(
					server.getDatabaseConnection(name));
		} catch (NullPointerException e) {

			// This exception can be thrown, if a database name has hashes in
			// it like this one:
			//
			// #mysql50#jhv_gadus_morhua_57_merged_projection_build.bak
			// or
			// #mysql50#jhv_gadus_morhua_57_ref_1.3_asm_buggy
			//
			// A database like this can exist on a MySql server, but
			// connecting to it will cause a NullPointerException to be
			// thrown.
			//
			logger.warning("Unable to connect to " + name + " on " + server);

			// No info will be available for this database.
			//
			return null;
		}

		DatabaseInfo info = null;

		boolean dbHasAMetaTable = template.queryForDefaultObjectList(
				"show tables like 'meta'", String.class).size() == 1;

		if (dbHasAMetaTable) {

			try {
				List<DatabaseInfo> dbInfos = template
						.queryForList(

						// Will return something like ("core", 63)
						//
								"select m1.meta_value, m2.meta_value from meta m1 join meta m2 where m1.meta_key='schema_type' and m2.meta_key='schema_version'",

								new RowMapper<DatabaseInfo>() {

									public DatabaseInfo mapRow(
											ResultSet resultSet, int position)
											throws SQLException {

										String schemaType = resultSet
												.getString(1);
										String schemaVersion = resultSet
												.getString(2);

										return new DatabaseInfo(
												name,
												null,
												Species.UNKNOWN,
												DatabaseType
														.resolveAlias(schemaType),
												schemaVersion, null);
									}
								});

				info = CollectionUtils.getFirstElement(dbInfos, info);

			} catch (SqlUncheckedException e) {

				logger.warning("Can't determine database type and version from "
						+ name + " on " + server+": "+e.getMessage());

				// No info will be available for this database.
				//
				return null;
			} finally {
				
			}
		}
		return info;
	}

	/**
	 * Utility for building a {@link DatabaseInfo} object given a name plus
	 * optional {@link Species} and {@link DatabaseType} to use explicitly
	 * 
	 * @param name
	 * @param species
	 *            (optional)
	 * @param type
	 *            (optional)
	 * @return object containing information about a database
	 */
	public static DatabaseInfo getInfoFromName(String name, Species species,
			DatabaseType type) {

		String schemaVersion = null;
		String genebuildVersion = null;
		String alias = null;
		String typeStr = null;

		Matcher m;

		for (Pattern p : patterns) {
			m = p.matcher(name);
			if (m.matches()) {
				// group 1 = alias
				alias = m.group(1);
				if (m.groupCount() > 1) {
					// group 2 = type
					typeStr = m.group(2);
					if (m.groupCount() > 2) {
						// group 3 = schema_version
						schemaVersion = m.group(3);
						if (m.groupCount() > 3) {
							// group 4 = gb_version
							genebuildVersion = m.group(4);
						}
					}
				}
				if (alias.endsWith(COLLECTION_CLAUSE)) {
					alias = alias.replaceAll(COLLECTION_CLAUSE,
							StringUtils.EMPTY);
				}
				break;
			}
		}

		if (species == null) {
			if (!StringUtils.isEmpty(alias)) {
				species = Species.resolveAlias(alias);
			} else {
				species = Species.UNKNOWN;
			}
		}

		if (type == null) {
			if (!StringUtils.isEmpty(typeStr)) {
				type = DatabaseType.resolveAlias(typeStr);
				if (typeStr.equals("ancestral") && species == Species.UNKNOWN) {
					species = Species.ANCESTRAL_SEQUENCES;
				}
			} else {
				type = DatabaseType.UNKNOWN;
			}
		}

		return new DatabaseInfo(name, alias, species, type, schemaVersion,
				genebuildVersion);

	}

	private final DatabaseInfo info;

	private List<Integer> speciesIds;

	private final DatabaseServer server;

	private DatabaseRegistry databaseRegistry;

	private Connection connection;

	/** The logger to use */
	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	// -----------------------------------------------------------------
	/**
	 * Create a new DatabaseRegistryEntry.
	 * 
	 * @param server
	 *            The database server where this database resides.
	 * @param name
	 *            The name of the database.
	 * @param species
	 *            The species that this database represents. If null, derive it
	 *            from name.
	 * @param type
	 *            The type of this database. If null, derive it from name.
	 */
	public DatabaseRegistryEntry(DatabaseServer server, String name,
			Species species, DatabaseType type) {
		this.server = server;
		DatabaseInfo info = getInfoFromName(name, species, type);
		if (info.getType() == DatabaseType.UNKNOWN) {
			// try and get the info from the database

			DatabaseInfo dbInfo = null;

			try {

				dbInfo = getInfoFromDatabase(server, name);

			} catch (SQLException e) {

				logger.warning(e.getMessage());
			}
			if (dbInfo != null) {
				info = dbInfo;
			}
		}
		this.info = info;
	}

	// -----------------------------------------------------------------

	/**
	 * @return Database name.
	 */
	public final String getName() {
		return info.getName();
	}

	/**
	 * @return Species.
	 */
	public final Species getSpecies() {

		return info.getSpecies();
	}

	/**
	 * @return Database type (core, est etc)
	 */
	public final DatabaseType getType() {
		return info.getType();
	}

	// -----------------------------------------------------------------

	/**
	 * Compares two databases by comparing the names of the species. If they are
	 * the same, then the schema version is used as a secondary sorting
	 * criterion.
	 * 
	 * The schema version is converted to an integer so there is numerical
	 * sorting on the schema version. Otherwise 9 would come after 10.
	 * 
	 * This is important, because the comparing is used in
	 * ComparePreviousVersionBase from which all the ComparePreviousVersion*
	 * inherit.
	 */
	public int compareTo(DatabaseRegistryEntry dbre) {

		int speciesOrdering = getSpecies().compareTo(dbre.getSpecies());

		if (speciesOrdering != 0) {
			return speciesOrdering;
		}

		return new Integer(getSchemaVersion()).compareTo(new Integer(dbre
				.getSchemaVersion()));

		// return getName().compareTo(dbre.getName());
	}

	public String getSchemaVersion() {
		return info.getSchemaVersion();
	}

	public String getGeneBuildVersion() {
		return info.getGenebuildVersion();
	}

	// -----------------------------------------------------------------
	// Return the numeric genebuild version, or -1 if this cannot be deduced
	// (e.g. from a non-standard database name)

	public int getNumericGeneBuildVersion() {

		if (getGeneBuildVersion() == null) {
			return -1;
		}

		String[] bits = getGeneBuildVersion().split("[a-zA-Z]");

		return Integer.parseInt(bits[0]);

	}

	public DatabaseRegistry getDatabaseRegistry() {
		return databaseRegistry;
	}

	public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	/**
	 * Check if the database has multiple species
	 * 
	 * @return true if the database contains more than one species
	 */
	public boolean isMultiSpecies() {
            return this.getName().matches(".*_collection_.*");
	}

	/**
	 * Utility method to determine list of species IDs found within a core
	 * database
	 * 
	 * @param con
	 * @param species
	 * @param type
	 * @return list of numeric IDs
	 */
	public static List<Integer> getSpeciesIds(Connection con, Species species,
			DatabaseType type) {

		List<Integer> speciesId = CollectionUtils.createArrayList();

		// only generic databases have a coord_system table
		if (type == null || !type.isGeneric()) {
			return speciesId;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt
					.executeQuery("SELECT DISTINCT(species_id) FROM meta where species_id is not null");
			if (rs != null) {
				while (rs.next()) {
					speciesId.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			throw new UtilUncheckedException(
					"Problem obtaining list of species IDs", e);
		} finally {
			DBUtils.closeQuietly(rs);
			DBUtils.closeQuietly(stmt);
		}

		return speciesId;

	}

	/**
	 * @return information about the server that this database is found on
	 */
	public DatabaseServer getDatabaseServer() {
		return server;
	}

	public Connection getConnection() {

		if (
			(connection == null) 
			|| !(ConnectionPool.isValidConnection(connection))
		) {

			try {
				connection = server.getDatabaseConnection(getName());
			} catch (SQLException e) {

				logger.warning(e.getMessage());
			}
		}
		return connection;
	}

	/**
	 * Test if this entry is equal to another. Comparison is currently only on
	 * database name.
	 * 
	 * @param dbre
	 * @return true if names are the same.
	 */
	public boolean equals(DatabaseRegistryEntry dbre) {

		return (dbre.getName().equals(getName()));

	}

	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		return (((DatabaseRegistryEntry) o).getName().equals(getName()));

	}

	public String getAlias() {
		return info.getAlias();
	}

	/**
	 * Utility method to determine list of species IDs found within the attached
	 * database
	 * 
	 * @return list of numeric IDs
	 */
	public List<Integer> getSpeciesIds() {
		if (speciesIds == null) {
			speciesIds = getSpeciesIds(getConnection(), getSpecies(), getType());
		}
		return speciesIds;
	}

	public String toString() {
		return getName();
	}

	// -----------------------------------------------------------------

} // DatabaseRegistryEntry
