/**
 * StringTupleRowMapper
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Helper class for building a String {@link Pair} from the first two columns in a query
 * @author dstaines
 *
 */
public class StringPairRowMapper implements RowMapper<Pair<String,String>> {

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.util.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public Pair<String, String> mapRow(ResultSet resultSet, int position) throws SQLException {
		return CollectionUtils.pair(resultSet.getString(1),resultSet.getString(2));
	}

}
