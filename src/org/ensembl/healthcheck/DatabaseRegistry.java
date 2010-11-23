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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.healthcheck.util.DBUtils;

/**
 * Class that stores information about which databases are available.
 */
public class DatabaseRegistry implements Iterable {

	// Entries is explicitly specified as an ArrayList rather than the list
	// because the order is important
	private ArrayList<DatabaseRegistryEntry> entries = new ArrayList<DatabaseRegistryEntry>();

	// these global settings override guessing if they are specified
	private Species globalSpecies = null;

	private DatabaseType globalType = null;

	/** The logger to use */
	private static Logger logger = Logger.getLogger("HealthCheckLogger");

	// -----------------------------------------------------------------
	/**
	 * Create a new DatabaseRegistry. DatabaseRegistryEntry objects for the databases matching regexp are created and added to the
	 * registry.
	 * 
	 * @param regexps
	 *          The regular expressions matching the databases to use. If null, match everything.
	 * @param isSecondary
	 *          If true, this is a secondary database registry.
	 */
	public DatabaseRegistry(List<String> regexps, DatabaseType globalType, Species globalSpecies, boolean isSecondary) {

		if (!isSecondary) {

			this.globalType = globalType;
			this.globalSpecies = globalSpecies;

		}

		List<DatabaseServer> servers = isSecondary ? DBUtils.getSecondaryDatabaseServers() : DBUtils.getMainDatabaseServers();

		for (DatabaseServer server : servers) {

			if (regexps == null || regexps.size() == 0) {
				
				String[] names = DBUtils.listDatabases(server.getServerConnection(), null);
				addEntriesToRegistry(server, names, isSecondary);

			} else {

				Iterator<String> it = regexps.iterator();
				while (it.hasNext()) {
					
					String regexp = it.next();
					String[] names = DBUtils.listDatabases(server.getServerConnection(), regexp);

					addEntriesToRegistry(server, names, isSecondary);

				}
			}
		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Create a new DatabaseRegistry from an array of DatabaseRegistryEntries.
	 * 
	 * @param dbres
	 *          The entries to use.
	 */
	public DatabaseRegistry(final DatabaseRegistryEntry[] dbres) {

		for (int i = 0; i < dbres.length; i++) {

			entries.add(dbres[i]);

		}

	}

	// -------------------------------------------------------------------------
	/**
	 * Create a new DatabaseRegistry from a list of DatabaseRegistryEntries.
	 * 
	 * @param dbres
	 *          The entries to use.
	 */
	public DatabaseRegistry(List<DatabaseRegistryEntry> dbres) {

		Iterator<DatabaseRegistryEntry> it = dbres.iterator();
		while (it.hasNext()) {

			entries.add(it.next());

		}

	}

	// -----------------------------------------------------------------

	private void addEntriesToRegistry(DatabaseServer server, final String[] names, boolean isSecondary) {

		for (String name : names) {

			DatabaseRegistryEntry dbre = new DatabaseRegistryEntry(server, name, globalSpecies, globalType);

			if (!this.contains(dbre)) {

				//logger.finest(dbre.getName() + " appears to be type " + dbre.getType() + " and species " + dbre.getSpecies());

				dbre.setDatabaseRegistry(this);
				
				entries.add(dbre);
				
				logger.finest("Added DatabaseRegistryEntry for " + name + " to " + (isSecondary ? "secondary" : "main") + " DatabaseRegistry");
				
			} else {

				logger.finest("Registry already contains an entry for " + dbre.getName() + ", skipping");

			}
		}

	}

	// -----------------------------------------------------------------
	/**
	 * Add a new DatabaseRegistryEntry to this registry.
	 * 
	 * @param dbre
	 *          The new DatabaseRegistryEntry.
	 */
	public final void add(final DatabaseRegistryEntry dbre) {

		entries.add(dbre);
		dbre.setDatabaseRegistry(this);

	}

	// -----------------------------------------------------------------
	/**
	 * Get all of the DatabaseRegistryEntries stored in this DatabaseRegistry.
	 * 
	 * @return The DatabaseRegistryEntries stored in this DatabaseRegistry.
	 */
	public final DatabaseRegistryEntry[] getAll() {

		return (DatabaseRegistryEntry[]) entries.toArray(new DatabaseRegistryEntry[entries.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * Get a list of all of the DatabaseRegistryEntries stored in this DatabaseRegistry.
	 * 
	 * @return The DatabaseRegistryEntries stored in this DatabaseRegistry.
	 */
	public final List<DatabaseRegistryEntry> getAllEntries() {

		return entries;

	}
	// -----------------------------------------------------------------
	/**
	 * Get all of the DatabaseRegistryEntries for a particular species
	 * 
	 * @param species
	 *          The species to look for.
	 * @return The DatabaseRegistryEntries for species..
	 */
	public final DatabaseRegistryEntry[] getAll(final Species species) {

		List<DatabaseRegistryEntry> result = new ArrayList<DatabaseRegistryEntry>();
		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (dbre.getSpecies().equals(species)) {
				result.add(dbre);
			}
		}

		return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * Get all of the DatabaseRegistryEntries for a particular database type.
	 * 
	 * @param type
	 *          The type to look for.
	 * @return The DatabaseRegistryEntries for type.
	 */
	public final DatabaseRegistryEntry[] getAll(final DatabaseType type) {

		List<DatabaseRegistryEntry> result = new ArrayList<DatabaseRegistryEntry>();
		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (dbre.getType().equals(type)) {
				result.add(dbre);
			}
		}

		return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * Get all of the DatabaseRegistryEntries for a particular database type and species.
	 * 
	 * @param type
	 *          The type to look for.
	 * @param species
	 *          The Species to look for.
	 * @return The DatabaseRegistryEntries that match type and species..
	 */
	public final DatabaseRegistryEntry[] getAll(final DatabaseType type, final Species species) {

		List<DatabaseRegistryEntry> result = new ArrayList<DatabaseRegistryEntry>();
		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (dbre.getType().equals(type) && dbre.getSpecies().equals(species)) {
				result.add(dbre);
			}
		}

		return (DatabaseRegistryEntry[]) result.toArray(new DatabaseRegistryEntry[result.size()]);

	}

	// ---------------------------------------------------------------------
	/**
	 * Get a single, named DatabaseRegistryEntry.
	 * 
	 * @param name
	 *          The name to look for.
	 * @return The matching DatabaseRegistryEntry, or null if none is found.
	 */
	public final DatabaseRegistryEntry getByExactName(String name) {

		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (dbre.getName().equals(name)) {
				return dbre;
			}
		}

		logger.warning("Can't find database matching name " + name);
		
		return null;
	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the types of databases in the registry.
	 * 
	 * @return An array containing each DatabaseType found in the registry.
	 */
	public final DatabaseType[] getTypes() {

		List<DatabaseType> types = new ArrayList<DatabaseType>();

		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (!types.contains(dbre.getType())) {
				types.add(dbre.getType());
			}
		}

		return (DatabaseType[]) types.toArray(new DatabaseType[types.size()]);

	}

	// -------------------------------------------------------------------------
	/**
	 * Get a list of the species in the registry.
	 * 
	 * @return An array containing each Species found in the registry.
	 */
	public final Species[] getSpecies() {

		List<Species> species = new ArrayList<Species>();

		Iterator<DatabaseRegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			DatabaseRegistryEntry dbre = it.next();
			if (!species.contains(dbre.getSpecies())) {
				species.add(dbre.getSpecies());
			}
		}

		return (Species[]) species.toArray(new Species[species.size()]);

	}

	// -----------------------------------------------------------------
	/**
	 * @return The number of DatabaseRegistryEntries in this registry.
	 */
	public final int getEntryCount() {

		return entries.size();

	}

	// -----------------------------------------------------------------
	/**
	 * @return True if this registry contains a particular DatabaseRegistryEntry (note equals() method in DatabaseRegistryEntry
	 *         determines this behaviour).
	 */
	public boolean contains(DatabaseRegistryEntry dbre) {

		for (DatabaseRegistryEntry entry : entries) {

			if (entry.equals(dbre)) {
				return true;
			}
		}

		return false;

	}

	// -----------------------------------------------------------------
	/**
	 * @return List of entries from this registry that match a regexp. Note that this will be a subset of the entries that the
	 *         registry was created from (based on another regexp!)
	 */
	public List<DatabaseRegistryEntry> getMatching(String regexp) {

		List<DatabaseRegistryEntry> result = new ArrayList<DatabaseRegistryEntry>();

		for (DatabaseRegistryEntry entry : entries) {

			if (entry.getName().matches(regexp)) {
				result.add(entry);
			}

		}

		return result;

	}

	/**
	 * Implement Iterable interface
	 */
	public Iterator<DatabaseRegistryEntry> iterator() {
		
		return new DatabaseRegistryIterator(this);
		
	}

	// -----------------------------------------------------------------

} // DatabaseRegistry
