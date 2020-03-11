/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
