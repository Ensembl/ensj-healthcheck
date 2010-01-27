package org.ensembl.healthcheck.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of the {@link MapRowMapper} intended as an
 * extension point to help when creating these call back objects. This version
 * will return a HashMap of the given generic types.
 *
 * @author ayates
 * @author dstaines
 */
public abstract class AbstractMapRowMapper<K, T> implements MapRowMapper<K, T>{

	public Map<K, T> getMap() {
		return new HashMap<K,T>();
	}
}
