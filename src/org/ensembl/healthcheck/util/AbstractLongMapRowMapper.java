package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides a similar function as {@link AbstractStringMapRowMapper} but returns
 * the first column as the key value but as a Long. This same method can be used
 * for int/Integer based result sets
 *
 * @author ayates
 * @author dstaines
 */
public abstract class AbstractLongMapRowMapper<T> extends
		AbstractMapRowMapper<Long, T> {

	/**
	 * Returns the first column as the keyed value & is a Long
	 */
	public Long getKey(ResultSet resultSet) throws SQLException {
		return resultSet.getLong(1);
	}

}
