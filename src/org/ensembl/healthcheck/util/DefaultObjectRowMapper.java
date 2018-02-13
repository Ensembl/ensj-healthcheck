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

package org.ensembl.healthcheck.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;

/**
 * Non-generic row mapper which will convert any row into a default supported
 * Object for the given column index. This currently supports:
 *
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Integer}</li>
 * <li>{@link Long}</li>
 * <li>{@link Float}</li>
 * <li>{@link Double}</li>
 * <li>{@link Boolean} (assumes that 1 (true) or 0 (false) can represent
 * booleans)</li>
 * <li>{@link Number} (can be used if required)</li>
 * <li>{@link Date}</li>
 * </ul>
 *
 * If the mapper is given a type it does not understand it will throw a
 * SQLException detailing the conversion problem
 *
 * @author ayates
 * @author dstaines
 */
public class DefaultObjectRowMapper<T> implements RowMapper<T> {

	private final Class<T> expectedType;

	private final int columnIndex;

	public DefaultObjectRowMapper(Class<T> expectedType, int columnIndex) {
		this.expectedType = expectedType;
		this.columnIndex = columnIndex;
	}

	/**
	 * Shortcut where the column index is set to 1 i.e. the first column of a
	 * result set
	 */
	public DefaultObjectRowMapper(Class<T> expectedType) {
		this.expectedType = expectedType;
		this.columnIndex = 1;
	}

	public T mapRow(ResultSet resultSet, int position) throws SQLException {
		T output = convertIntoRequested(resultSet, columnIndex);
		return output;
	}

	/**
	 * Performs the business of converting from an expected type into a given
	 * object
	 */
	@SuppressWarnings("unchecked")
	protected T convertIntoRequested(ResultSet resultSet, int columnIndex)
			throws SQLException {

		Object result = null;

		if (String.class.isAssignableFrom(expectedType)) {
			result = resultSet.getString(columnIndex);
		} else if (Integer.class.isAssignableFrom(expectedType)) {
			result = resultSet.getInt(columnIndex);
		} else if (Long.class.isAssignableFrom(expectedType)) {
			result = resultSet.getLong(columnIndex);
		} else if (Float.class.isAssignableFrom(expectedType)) {
			result = resultSet.getFloat(columnIndex);
		} else if (Double.class.isAssignableFrom(expectedType)) {
			result = resultSet.getDouble(columnIndex);
		} else if (BigDecimal.class.isAssignableFrom(expectedType)) {
			result = resultSet.getBigDecimal(columnIndex);
		} else if (Boolean.class.isAssignableFrom(expectedType)) {
			result = resultSet.getBoolean(columnIndex);
		} else if (Number.class.isAssignableFrom(expectedType)) {
			Object number = resultSet.getObject(columnIndex);
			if (number != null) {
				if (Number.class.isAssignableFrom(number.getClass())) {
					result = number;
				} else {
					String actualClass = number.getClass().getName();
					throw new SQLException("Cannot cast " + actualClass
							+ " into a Number.");
				}
			}
		} else if (Timestamp.class.isAssignableFrom(expectedType)) {
			result = resultSet.getTimestamp(columnIndex);
		} else if (Date.class.isAssignableFrom(expectedType)) {
			// using getDate() would lose the hours/mins/secs, so we use
			// timestamp instead
			result = resultSet.getTimestamp(columnIndex);
		} else {
			// If we get to this point then ... well just throw an exception
			String simpleName = this.getClass().getSimpleName();
			String targetType = expectedType.getSimpleName();
			String template = "{0} does not know how to work with expected type {1}";
			String message = MessageFormat.format(template, new Object[] {
					simpleName, targetType });
			throw new SQLException(message);
		}

		// Our only cast to the expected type T
		return (T) result;
	}
}
