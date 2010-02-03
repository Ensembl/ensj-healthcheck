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
import org.ensembl.healthcheck.util.UtilUncheckedException;

/**
 * Container for information about a database that can be stored in a
 * DatabaseRegistry.
 */
public class DatabaseRegistryEntry implements Comparable<DatabaseRegistryEntry> {

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

	// e.g. neurospora_crassa_core_4_56_1a
	protected final static Pattern EG_DB = Pattern
			.compile("^([a-z_]+)_([a-z]+)_[0-9]+_([0-9]+)_([0-9A-Za-z]+)");
	// e.g. homo_sapiens_core_56_37a
	protected final static Pattern E_DB = Pattern
			.compile("^([^_]+_[^_]+)_([a-z]+)_([0-9]+)_([0-9A-Za-z]+)");
	// human_core_20, hsapiens_XXX
	protected final static Pattern EEL_DB = Pattern
			.compile("^([^_]+)_([a-z]+)_([0-9A-Za-z]+)");
	// ensembl_compara_bacteria_3_56
	protected final static Pattern EGC_DB = Pattern
			.compile("^(ensembl)_(compara)_[a-z_]+_[0-9]+_([0-9]+)");
	// ensembl_compara_56
	protected final static Pattern EC_DB = Pattern
			.compile("^(ensembl)_(compara)_([0-9]+)");
	// username_ensembl_ancestral_57
	protected final static Pattern UC_DB = Pattern
			.compile("^[^_]+_(ensembl)_(compara)_([0-9]+)");
	// username_ensembl_ancestral_57
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
			.compile("^username_([^_]+_[^_]+)_([a-z]+)_([0-9]+)_([0-9A-Za-z]+)");
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
	protected final static Pattern MYSQL_DB = Pattern
			.compile("^(mysql|information_schema)");

	protected final static Pattern[] patterns = {
		EC_DB, UA_DB, UC_DB, EA_DB, EGC_DB, EG_DB, E_DB, EM_DB, EE_DB, EEL_DB, U_DB, V_DB, MYSQL_DB,
			BLAST_DB, UD_DB, TAX_DB, EW_DB, HELP_DB
	};

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

		DatabaseInfo info = new DatabaseInfo(name, alias, species, type,
				schemaVersion, genebuildVersion);

		return info;

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
		this.info = getInfoFromName(name, species, type);

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

	public int compareTo(DatabaseRegistryEntry dbre) {

		return getName().compareTo(dbre.getName());

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
		return getSpeciesIds().size() > 1;
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

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT DISTINCT(species_id) FROM meta where species_id is not null");
			if (rs != null) {
				while (rs.next()) {
					speciesId.add(rs.getInt(1));
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw new UtilUncheckedException(
					"Problem obtaining list of species IDs", e);
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

		if (connection == null) {

			connection = server.getDatabaseConnection(getName());

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
			speciesIds = getSpeciesIds(connection, getSpecies(), getType());
		}
		return speciesIds;
	}

	public String toString() {
		return getName();
	}

	// -----------------------------------------------------------------

} // DatabaseRegistryEntry
