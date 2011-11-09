package org.ensembl.healthcheck.util;

import java.util.LinkedHashMap;

/**
 * This is a very poor implementation of a LRU cache in Java since it relies
 * on using normal references and therefore will keep its data in memory
 * for as long as there is a hard reference to the instance. If you require
 * a LRU cache which is more memory sensitive (when you are about to
 * run out of memory clear the cache) then please do not use this 
 * implementation.
 * 
 * <b>Do not use this with high cache retention values.</b>
 * 
 * @param <K> Key class
 * @param <V> Value class
 */
public class PoorLruMap<K,V> extends LinkedHashMap<K, V> {

	/**
   * Used for serialisation 
   */
  private static final long serialVersionUID = -6727011357359376671L;
  
	private final int maxEntries;
	
	/**
	 * Default constructor
	 * 
	 * @param maxEntries The number of entries to store as a maximum  
	 */
	public PoorLruMap(int maxEntries) {
		super();
		this.maxEntries = maxEntries;
  }
	
	/**
	 * Returns true if the map's current size is bigger than the maximum
	 * size of the cache as given during construction.
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
	  return size() > maxEntries; 
	}
}
