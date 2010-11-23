package org.ensembl.healthcheck;

import java.util.Iterator;

/**
 * Iterator for the entries in a DatabaseRegistry. They are already stored as a list so the internal list iterator is used.
 *
 */
public class DatabaseRegistryIterator implements Iterator<DatabaseRegistryEntry> {

	Iterator<DatabaseRegistryEntry> entryIterator;
	
	public DatabaseRegistryIterator(DatabaseRegistry dbr) {
		
		entryIterator = dbr.getAllEntries().iterator();
		
	}
	
	public boolean hasNext() {
		
		return entryIterator.hasNext();
		
	}

	public DatabaseRegistryEntry next() {
		
		return (DatabaseRegistryEntry) entryIterator.next();
		
	}

	public void remove() {

		entryIterator.remove();
		
	}

}
