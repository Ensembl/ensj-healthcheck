/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * A {@link MapRowMapper} which works in a similar vein to 
 * {@link DefaultObjectRowMapper}. Here the constructor takes two class
 * types; the first is the expected object for the key and the second is
 * the expected object for the value. Each of these is passed into a
 * {@link DefaultObjectRowMapper} instance which does the mappings at
 * positions 1 and 2 in the JDBC {@link ResultSet}.
 * 
 * @author ayates
 *
 * @param <K> The Object type expected for the key
 * @param <V> The Object type expected for the value
 */
public class DefaultMapRowMapper<K,V> extends AbstractMapRowMapper<K,V> {
	
	private final RowMapper<K> keyMapper;
	private final RowMapper<V> valueMapper;
	
	public DefaultMapRowMapper(Class<K> keyClass, Class<V> valueClass) {
		super();
		this.keyMapper = new DefaultObjectRowMapper<K>(keyClass, 1);
		this.valueMapper = new DefaultObjectRowMapper<V>(valueClass, 2);
	}

	/**
	 * Throws a {@link SQLException} as this is an unsupported operation
	 */
	public void existingObject(Object currentValue, ResultSet resultSet,
			int position) throws SQLException {
		throw new SQLException("Can only map a key once; if you need to map it more than once override");
	}

	/**
	 * Returns the value being mapped by the keyMapper at position 1.
	 */
	public K getKey(ResultSet resultSet) throws SQLException {
		return keyMapper.mapRow(resultSet, 0);
	}

	/**
	 * Returns the value being mapped by the valueMapper at position 2.
	 */
	public V mapRow(ResultSet resultSet, int position) throws SQLException {
		return valueMapper.mapRow(resultSet, position);
	}

}
