/**
 * StringMapRowMapper
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dstaines
 *
 */
public class StringMapRowMapper extends AbstractStringMapRowMapper<String> {

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.util.MapRowMapper#existingObject(java.lang.Object, java.sql.ResultSet, int)
	 */
	@Override
	public void existingObject(String currentValue, ResultSet resultSet,
			int position) throws SQLException {
		throw new SQLException("Single value per key expected");
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.util.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public String mapRow(ResultSet resultSet, int position) throws SQLException {
		return resultSet.getString(2);
	}

}
