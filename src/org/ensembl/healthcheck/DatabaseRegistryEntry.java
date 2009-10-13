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
import java.sql.Statement;
import java.util.logging.Logger;

import org.ensembl.healthcheck.util.DBUtils;

/**
 * Container for information about a database that can be stored in a DatabaseRegistry.
 * 
 */
public class DatabaseRegistryEntry implements Comparable<DatabaseRegistryEntry> {

	private String name;

	private Species species;

	private String schemaVersion, geneBuildVersion;

	private DatabaseType type;

	private boolean isMultiSpecies = false;

	private DatabaseServer server;

	private DatabaseRegistry databaseRegistry;

	private Connection connection;
	
	/** The logger to use */
	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	// -----------------------------------------------------------------
	/**
	 * Create a new DatabaseRegistryEntry. 
	 * 
	 * @param server
	 *          The database server where this database resides.
	 * @param name
	 *          The name of the database.
	 * @param species
	 *          The species that this database represents. If null, guess it from name.
	 * @param type
	 *          The type of this database. If null, guess it from name.
	 */
	public DatabaseRegistryEntry(DatabaseServer server, String name, Species species, DatabaseType type) {

		this.name = name;

		connection = server.getDatabaseConnection(name);
		
		isMultiSpecies = checkMultiSpecies(connection);

		if (species != null) {
			this.species = species;
		} else {
			this.species = setSpeciesFromName(name);
		}

		if (type != null) {
			this.type = type;
		} else {
			this.type = setTypeFromName(name);
		}

	}

	public DatabaseRegistryEntry() {
		
	}
	
	// -----------------------------------------------------------------
	
	/**
	 * Attempt to figure out species from database name. Also set schema and genebuild versions as appropriate.
	 * 
	 * @param name
	 *          The name to use.
	 * @return The species corresponding to name, or Species.UNKNOWN.
	 */
	public final Species setSpeciesFromName(final String name) {

		Species result = Species.UNKNOWN;
		String[] bits = name.split("_");
		String alias;

		// schema version should be the first number (never the first bit though), except in multi-species databases
		for (int i = 1; i < bits.length; i++) {
			if (bits[i].matches("^[0-9]+$")) {
				this.schemaVersion = isMultiSpecies() ? bits[i + 1] : bits[i];
				break;
			}
		}

		// there are many different possibilities for database naming; the most
		// likely are catered for here

		// homo_sapiens_core_20_34a
		if (bits.length >= 2) {
			alias = bits[0] + "_" + bits[1];
			if (bits.length == 5) {
				this.geneBuildVersion = bits[4];
			}

			if (Species.resolveAlias(alias) != Species.UNKNOWN) {
				return Species.resolveAlias(alias);
			}
		}

		// homo_sapiens_core_expression_est_37_35j
		if (bits.length >= 7) {
			alias = bits[0] + "_" + bits[1];
			this.geneBuildVersion = bits[6];
			if (Species.resolveAlias(alias) != Species.UNKNOWN) {
				return Species.resolveAlias(alias);
			}
		}

		// human_core_20, hsapiens_XXX
		if (bits.length > 1) {
			alias = bits[0];
			if (Species.resolveAlias(alias) != Species.UNKNOWN) {
				return Species.resolveAlias(alias);
			}
		}

		// compara, mart, go doesn't really have a species. It can be ensembl_type
		// or username_ensembl_type
		if (bits.length >= 2
				&& (bits[1].equalsIgnoreCase("compara") || bits[1].equalsIgnoreCase("go") || bits[1].equalsIgnoreCase("mart") || bits[1].equalsIgnoreCase("compara") || bits[1].equalsIgnoreCase("go") || bits[1]
						.equalsIgnoreCase("mart"))) {
			return Species.UNKNOWN;
		}

		// Vega naming convention e.g. vega_homo_sapiens_ext_20040821_v19
		if (bits.length > 3 && bits[0].equalsIgnoreCase("vega")) {
			alias = bits[1] + "_" + bits[2];
			if (Species.resolveAlias(alias) != Species.UNKNOWN) {
				return Species.resolveAlias(alias);
			}
		}

		// username_species_type_version_release
		if (bits.length > 3) {
			alias = bits[1] + "_" + bits[2];
			if (bits.length == 5) {
				this.geneBuildVersion = bits[4];
			}
			if (Species.resolveAlias(alias) != Species.UNKNOWN) {
				return Species.resolveAlias(alias);
			}

		}

		// ensembl help databases
		if (name.startsWith("ensembl_help_")) {

			return Species.HELP;

		}

		// system/reserved databases
		if (name.equals("mysql")) {

			return Species.SYSTEM;

		}

		// ensembl_website databases
		if (name.startsWith("ensembl_website")) {

			return Species.ENSEMBL_WEBSITE;

		}

		// ncbi_taxonomy databases
		if (name.startsWith("ncbi_taxonomy")) {

			return Species.NCBI_TAXONOMY;

		}

		// other permutations?

		if (result.equals(Species.UNKNOWN) && name.length() > 0) {
			logger.finest("Can't deduce species from database name " + name);
		}

		return result;

	}

	// -----------------------------------------------------------------
	/**
	 * Attempt to figure out database type from database name.
	 * 
	 * @param name
	 *          The database name to use.
	 * @return The database type corresponding to name, or DatabaseType.UNKNOWN.
	 */
	public final DatabaseType setTypeFromName(final String name) {

		DatabaseType result = DatabaseType.UNKNOWN;
		String[] bits = name.split("_");
		String alias;

		// there are many different possibilities for database naming; the most
		// likely are catered for here

		// homo_sapiens_core_20_34a
		if (bits.length >= 4) {
			// Try to match homo_sapiens_core_expression_est_35_35h before
			alias = bits[3];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
			alias = bits[2];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
		}

		// human_core_20, ensembl_compara_20_1
		if (bits.length >= 3) {
			alias = bits[1];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
		}

		// Vega naming convention e.g. vega_homo_sapiens_ext_20040821_v19
		if (bits.length > 3 && bits[0].equalsIgnoreCase("vega")) {
			return DatabaseType.VEGA;
		}

		// username_species_type_version_release
		if (bits.length > 3) {
			alias = bits[3];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
		}

		// system/reserved databases
		if (name.equals("mysql")) {

			return DatabaseType.SYSTEM;

		}

		// ensembl_website databases
		if (name.startsWith("ensembl_website")) {

			return DatabaseType.ENSEMBL_WEBSITE;

		}

		// ncbi_taxonomy databases
		if (name.startsWith("ncbi_taxonomy")) {

			return DatabaseType.NCBI_TAXONOMY;

		}

		// ensembl genomes collection databases, e.g. hornead_escherichia_shigella_collection_core_0_52_1a or
		// escherichia_shigella_collection_core_0_52_1a
		if (name.contains("collection")) {
			return DatabaseType.CORE;
		}

		// other permutations?

		if (result.equals(DatabaseType.UNKNOWN) && name.length() > 0) {
			logger.finest("Can't deduce database type from database name " + name + "; use -type argument to specify it explicitly");
		}

		return result;

	}

	// -----------------------------------------------------------------

	/**
	 * @return Database name.
	 */
	public final String getName() {

		return name;
	}

	/**
	 * @param name
	 *          New database name.
	 */
	public final void setName(final String name) {

		this.name = name;
	}

	/**
	 * @return Species.
	 */
	public final Species getSpecies() {

		return species;
	}

	/**
	 * @param species
	 *          New Species.
	 */
	public final void setSpecies(final Species species) {

		this.species = species;
	}

	/**
	 * @return Database type (core, est etc)
	 */
	public final DatabaseType getType() {

		return type;
	}

	/**
	 * @param type
	 *          New database type (core, est etc)
	 */
	public final void setType(final DatabaseType type) {

		this.type = type;
	}

	// -----------------------------------------------------------------

	public int compareTo(DatabaseRegistryEntry dbre) {

		return getName().compareTo(dbre.getName());

	}

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public String getGeneBuildVersion() {
		return geneBuildVersion;
	}

	public void setGeneBuildVersion(String geneBuildVersion) {
		this.geneBuildVersion = geneBuildVersion;
	}

	// -----------------------------------------------------------------
	// Return the numeric genebuild version, or -1 if this cannot be deduced (e.g. from a non-standard database name)

	public int getNumericGeneBuildVersion() {

		if (geneBuildVersion == null) {
			return -1;
		}

		String[] bits = geneBuildVersion.split("[a-zA-Z]");

		return Integer.parseInt(bits[0]);

	}

	public DatabaseRegistry getDatabaseRegistry() {
		return databaseRegistry;
	}

	public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
		this.databaseRegistry = databaseRegistry;
	}

	public boolean isMultiSpecies() {
		return isMultiSpecies;
	}

	public void setMultiSpecies(boolean isMultiSpecies) {
		this.isMultiSpecies = isMultiSpecies;
	}

	// -----------------------------------------------------------------
	/**
	 * Check if this database is a multi-species one or not
	 */
	public boolean checkMultiSpecies(Connection con) {

		boolean result = false;
		// variation databases do not have coord_system table
		if (name.matches("^.*_variation_.*$") || name.matches("^.*_compara_.*$")) {
			return result;
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT(species_id)) FROM coord_system");
			if (rs != null) {
				if (rs.first()) {
					int speciesCount = rs.getInt(1);
					if (speciesCount > 1) {
						result = true;
						logger.finest("Found a multi-species database, " + speciesCount + " species.");
					}
				} else {
					result = false;
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}

	public DatabaseServer getDatabaseServer() {
		return server;
	}

	public void setDatabaseServer(DatabaseServer server) {
		this.server = server;
	}
	
	public Connection getConnection() {
		
		return connection;
		
	}
	
	/**
	 * Test if this entry is equal to another. Comparison is currently only on database name.
	 * @param dbre
	 * @return true if names are the same.
	 */
	public boolean equals(DatabaseRegistryEntry dbre) {
	
		return (dbre.getName().equals(name));
	
	}

	// -----------------------------------------------------------------

} // DatabaseRegistryEntry
