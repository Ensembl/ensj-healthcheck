/*
  Copyright (C) 2003 EBI, GRL
 
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
package org.ensembl.healthcheck;

import java.util.logging.Logger;

/**
 * Container for information about a database that can be stored in a DatabaseRegistry.
 *
 */
public class DatabaseRegistryEntry {

	String name;
	Species species;
	DatabaseType type;

	protected static Logger logger = Logger.getLogger("HealthCheckLogger");

	// -----------------------------------------------------------------
	/**
	 * Fully-qualified constructor.
	 */
	public DatabaseRegistryEntry(String name, Species species, DatabaseType type) {

		this.name = name;
		this.species = species;
		this.type = type;

	}

	// -----------------------------------------------------------------
	/**
	 * Create a DatabaseRegistryEntry from just the database name; the species 
	 * and type are estimated from the database name. Note these can be 
	 * overridden later by setSpecies/setType if they are specified on the 
	 * command-line.
	 */
	public DatabaseRegistryEntry(String name) {

		this.name = name;
		species = setSpeciesFromName(name);
		type = setTypeFromName(name);

	}

	// -----------------------------------------------------------------
	/**
	 * Attempt to figure out species from database name.
	 */
	public Species setSpeciesFromName(String name) {

		Species result = Species.UNKNOWN;
		String[] bits = name.split("_");
		String alias;

		// there are many different possibilities for database naming; the most
		// likely are catered for here

		// homo_sapiens_core_20_34a
		if (bits.length >= 2) {
			alias = bits[0] + "_" + bits[1];
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

		// TODO other permutations?

		if (result.equals(Species.UNKNOWN)) {
			logger.warning("Can't deduce species from database name " + name);
		}

		return result;

	}

	//	-----------------------------------------------------------------
	/**
	 * Attempt to figure out database type from database name.
	 */
	public DatabaseType setTypeFromName(String name) {

		DatabaseType result = DatabaseType.UNKNOWN;
		String[] bits = name.split("_");
		String alias;

		// there are many different possibilities for database naming; the most
		// likely are catered for here

		// homo_sapiens_core_20_34a
		if (bits.length >= 4) {
			alias = bits[2];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
		}

		// human_core_20
		if (bits.length >= 3) {
			alias = bits[1];
			if (DatabaseType.resolveAlias(alias) != DatabaseType.UNKNOWN) {
				return DatabaseType.resolveAlias(alias);
			}
		}

		// TODO other permutations?

		if (result.equals(DatabaseType.UNKNOWN)) {
			logger.warning("Can't deduce database type from database name " + name);
		}

		return result;

	}

	// -----------------------------------------------------------------

	/**
	 * @return Database name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name New database name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Species.
	 */
	public Species getSpecies() {
		return species;
	}

	/**
	 * @param species New Species.
	 */
	public void setSpecies(Species species) {
		this.species = species;
	}

	/**
	 * @return Database type (core, est etc)
	 */
	public DatabaseType getType() {
		return type;
	}

	/**
	 * @param type New database type (core, est etc)
	 */
	public void setType(DatabaseType type) {
		this.type = type;
	}

	// -----------------------------------------------------------------

} // DatabaseRegistryEntry
