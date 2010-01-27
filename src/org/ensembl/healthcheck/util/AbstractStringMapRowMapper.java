package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementation of map row mapper which expects the first column is the
 * keyed value & is a String.
 *
 * @author ayates
 * @author dstaines
 * @param <T> The type of object which this map stores as its value
 */
public abstract class AbstractStringMapRowMapper<T> extends AbstractMapRowMapper<String, T> {

	/**
	 * Returns the first column of the result set as a String & is the
	 * result key
	 */
	public String getKey(ResultSet resultSet) throws SQLException {
		return resultSet.getString(1);
	}
}
