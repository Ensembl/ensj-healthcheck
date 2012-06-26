/**
 * StringListMapRowMapper
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Concrete utility mapper for a 1:n string mapper
 * @author dstaines
 * 
 */
public class StringListMapRowMapper extends
		AbstractStringMapRowMapper<List<String>> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.util.MapRowMapper#existingObject(java.lang.Object
	 * , java.sql.ResultSet, int)
	 */
	@Override
	public void existingObject(List<String> currentValue, ResultSet resultSet,
			int position) throws SQLException {
		currentValue.add(resultSet.getString(2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.util.RowMapper#mapRow(java.sql.ResultSet,
	 * int)
	 */
	@Override
	public List<String> mapRow(ResultSet resultSet, int position)
			throws SQLException {
		List<String> list = CollectionUtils.createArrayList();
		existingObject(list, resultSet, position);
		return list;
	}

}
