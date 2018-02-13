/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
