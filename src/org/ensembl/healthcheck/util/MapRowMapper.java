/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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
import java.util.Map;

/**
 * Used to provide a different form of {@link ResultSet} to domain object
 * mapping. As a pose to {@link RowMapper} which assumes that each row of a
 * result set is a unique domain object, this Object assumes that each domain
 * object defines a unique instance of which the backing query can return more
 * than one row & we need to aggregate this data together.
 *
 * <p>
 * One important difference is the usage of
 * {@link RowMapper#mapRow(ResultSet, int)} in this interface which is fired
 * ONLY when there is a new object. This happens when
 * {@link #getKey(ResultSet)} returns a key which
 * {@link Map#containsKey(Object)} returns false & therefore must be a new
 * entry.
 *
 * @author ayates
 * @author dstaines (adapted for pure JDBC use)
 * @param <K>
 *            The type of Key for the output map we need
 * @param <T>
 *            The type of Value for the output map
 */
public interface MapRowMapper<K, T> extends RowMapper<T> {

	/**
	 * Delegates responsibility for the creation of the map to be the
	 * responsibility of the mapper since the mapper is the capture of the
	 * client's requirements. This method should return an instance of a Map
	 * implementing class. The calling mapper will not make any attempt to clear
	 * this Object before usage.
	 */
	Map<K, T> getMap();

	/**
	 * Called on each iteration to return what is the unique Key for this given
	 * iteration of the backing result set
	 */
	K getKey(ResultSet resultSet) throws SQLException;

	/**
	 * Called when {@link #getKey(ResultSet)} returns a hit
	 *
	 * @param currentValue
	 *            The current value as extracted from the backing map
	 * @param resultSet
	 *            The result set iterated onto the current row
	 * @param position
	 *            The current position in this iterator
	 */
	void existingObject(T currentValue, ResultSet resultSet, int position)
			throws SQLException;
}
