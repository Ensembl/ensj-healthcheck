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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * The default implementation of {@link SqlTemplate} which provides all the
 * basic functionality that should be expected from a class which implements
 * this class. A {@link Connection} is passed into the constructor that is used
 * for all queries. Note that this should not be closed independently. You can
 * initialise with a {@link DatabaseRegistryEntry}
 * 
 * @author ayates
 * @author dstaines
 */
public class ConnectionBasedSqlTemplateImpl implements SqlTemplate {

	public static final int FIRST_COLUMN_INDEX = 1;
	public static final int NO_ROW_LIMIT_CHECKS = -1;

	private final Connection connection;
	private final String uri;

	public String getUri() {
		return uri;
	}

	public ConnectionBasedSqlTemplateImpl(DatabaseRegistryEntry dbre) {
		this(dbre.getConnection());
	}

	public ConnectionBasedSqlTemplateImpl(Connection connection) {
		this.connection = connection;
		try {
			this.uri = connection.getMetaData().getURL();
		} catch (SQLException e) {
			throw new SqlUncheckedException("Could not get URL for connection");
		}
	}
	
	/**
   * {@inheritDoc}
   */
	public <T> List<T> mapResultSetToList(final ResultSet resultSet,
      final RowMapper<T> mapper, final int rowLimit, String sql,
      final Object[] args) throws SqlUncheckedException {
	  List<T> output = new ArrayList<T>();
	  mapResultSetToCollection(resultSet, mapper, rowLimit, sql, args, output);
	  return output;
	}
	
  /**
   * {@inheritDoc}
   */
  public <T> Set<T> mapResultSetToSet(final ResultSet resultSet,
      final RowMapper<T> mapper, final int rowLimit, String sql,
      final Object[] args) throws SqlUncheckedException {
    Set<T> output = new LinkedHashSet<T>();
    mapResultSetToCollection(resultSet, mapper, rowLimit, sql, args, output);
    return output;
  }

	public <T> void mapResultSetToCollection(final ResultSet resultSet,
			final RowMapper<T> mapper, final int rowLimit, String sql,
			final Object[] args, final Collection<T> output) throws SqlUncheckedException {
		int position = 0;
		boolean inspectRowCount = (rowLimit > 0);

		try {
			while (resultSet.next()) {

				if (inspectRowCount && position > rowLimit) {
					String expected = Integer.toString(rowLimit);
					String actual = Integer.toString(position);
					String exceptionMessage = MessageFormat
							.format("Too many rows returned. "
									+ "Expected {0} but actual row count was {1}",
									new Object[] { expected, actual });
					String message = formatExceptionMessage(exceptionMessage,
							sql, args);
					throw new SqlUncheckedException(message);
				}

				output.add(mapper.mapRow(resultSet, position));
				position++;
			}

			if (inspectRowCount && position == 0) {
				String message = formatExceptionMessage(
						"Did not find any rows", sql, args);
				throw new SqlUncheckedException(message);
			}
		} catch (SQLException e) {
			String message = formatExceptionMessage(
					"Encountered problem whilst mapping ResultSet to Object List",
					sql, args);
			throw new SqlUncheckedException(message, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T mapResultSetToSingleObject(ResultSet resultSet,
			RowMapper<T> mapper, String sql, Object[] args)
			throws SqlUncheckedException {
		List<T> results = mapResultSetToList(resultSet, mapper, 1, sql, args);
		return results.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T queryForObject(final String sql, final RowMapper<T> mapper,
			final Object... args) throws SqlUncheckedException {
		return execute(sql, new ResultSetCallback<T>() {
			@Override
			public T process(ResultSet rs) throws SQLException {
				return mapResultSetToSingleObject(rs, mapper, sql, args);
			}
		}, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public int execute(String sql) {
		int updatedRows = -1;
		Statement st = null;
		try {
			st = connection.createStatement();
			updatedRows = st.executeUpdate(sql);
		} catch (SQLException e) {
			createUncheckedException(sql, new Object[] {}, e);
		} finally {
			closeDbObject(st);
		}
		return updatedRows;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T execute(String sql, ResultSetCallback<T> callback,
			Object... args) {
		T object;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			bindParamsToPreparedStatement(ps, args);
			rs = ps.executeQuery();
			object = callback.process(rs);
		} catch (SQLException e) {
			throw createUncheckedException(sql, args, e);
		} finally {
			closeDbObject(rs);
			closeDbObject(ps);
		}
		return object;
	}

	/**
	 * Use this to close down {@link ResultSet} objects with null safety checks
	 */
	protected void closeDbObject(ResultSet resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException e) {
			// ignore closing exceptions here
		}
	}

	/**
	 * Use this to close down {@link Statement} objects with null safety checks
	 */
	protected void closeDbObject(Statement st) {
		try {
			if (st != null)
				st.close();
		} catch (SQLException e) {
			// ignore closing exceptions here
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> queryForList(final String sql,
			final RowMapper<T> mapper, final Object... args)
			throws SqlUncheckedException {
		return execute(sql, new ResultSetCallback<List<T>>() {
			@Override
			public List<T> process(ResultSet rs) throws SQLException {
				return mapResultSetToList(rs, mapper, NO_ROW_LIMIT_CHECKS, sql,
						args);
			}
		}, args);
	}
	
  /**
   * {@inheritDoc}
   */
	public <T> Set<T> queryForSet(final String sql, final RowMapper<T> mapper, 
	    final Object... args) {
	  return execute(sql, new ResultSetCallback<Set<T>>() {
      @Override
      public Set<T> process(ResultSet rs) throws SQLException {
        return mapResultSetToSet(rs, mapper, NO_ROW_LIMIT_CHECKS, sql,
            args);
      }
    }, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T queryForDefaultObject(final String sql, final Class<T> expected,
	    final Object... args) throws SqlUncheckedException {
		DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(
				expected, FIRST_COLUMN_INDEX);
		return queryForObject(sql, mapper, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> queryForDefaultObjectList(final String sql, final Class<T> expected,
	    final Object... args) throws SqlUncheckedException {
		DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(
				expected, FIRST_COLUMN_INDEX);
		return queryForList(sql, mapper, args);
	}
	
  /**
   * {@inheritDoc}
   */
  public <T> Set<T> queryForDefaultObjectSet(final String sql, final Class<T> expected,
      final Object... args) throws SqlUncheckedException {
    DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(
        expected, FIRST_COLUMN_INDEX);
    return queryForSet(sql, mapper, args);
  }

	/**
	 * {@inheritDoc}
	 */
	public <K, T> Map<K, T> queryForMap(final String sql,
			final MapRowMapper<K, T> mapRowMapper, final Object... args)
			throws SqlUncheckedException {

		return execute(sql, new ResultSetCallback<Map<K, T>>() {
			@Override
			public Map<K, T> process(ResultSet rs) throws SQLException {
				Map<K, T> targetMap = mapRowMapper.getMap();
				int position = -1;
				while (rs.next()) {
					position++;
					K key = mapRowMapper.getKey(rs);
					if (targetMap.containsKey(key)) {
						T currentValue = targetMap.get(key);
						mapRowMapper.existingObject(currentValue, rs, position);
					} else {
						T newValue = mapRowMapper.mapRow(rs, position);
						targetMap.put(key, newValue);
					}
				}
				return targetMap;
			}
		}, args);
	}
	
  /**
   * {@inheritDoc}
   */
	public int update(final String sql, final Object... args) {
	  PreparedStatement ps = null;
	  try {
      ps = connection.prepareStatement(sql);
      bindParamsToPreparedStatement(ps, args);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw createUncheckedException(sql, args, e);
    } finally {
      closeDbObject(ps);
    }
	}

	// ----- EXCEPTION HANDLING

	/**
	 * Used to generically raise unchecked exceptions from sql service
	 * exceptions
	 */
	private SqlUncheckedException createUncheckedException(String sql,
			Object[] params, Throwable e) {
		String message = formatExceptionMessage(
				"Could not run statement because of SqlServiceException: "+e.getMessage(), sql,
				params);
		return new SqlUncheckedException(message, e);
	}

	// -------- COMMENTED OUT BECAUSE WE HAVE NO NEED FOR THIS YET

	// private int twoDTraceLimit = 3;
	//
	// private String formatExceptionMessage(String exceptionMessage, String
	// sql,
	// Object[][] params) {
	// List<Object> listArgs = new ArrayList<Object>();
	//
	// int loop = (params.length > twoDTraceLimit) ? twoDTraceLimit
	// : params.length;
	//
	// for (int i = 0; i < loop; i++) {
	// Object[] arg = params[i];
	// listArgs.add(Arrays.toString(arg));
	// }
	//
	// if (params.length > twoDTraceLimit) {
	// listArgs.add("More params lines than can show (" + params.length
	// + ") ...");
	// }
	//
	// Object[] args = listArgs.toArray(new Object[0]);
	// return formatExceptionMessage(exceptionMessage, sql, args);
	// }

	/**
	 * Used to generate the exception messages used through this class
	 */
	private String formatExceptionMessage(String exceptionMessage, String sql,
			Object[] args) {
		String template = "{0} URI => {1} SQL => {2} PARAMS => {3}";
		String paramsString = (ArrayUtils.isEmpty(args)) ? "NONE" : Arrays
				.toString(args);
		Object[] templateArgs = new Object[] { exceptionMessage, getUri(), sql,
				paramsString };
		String message = MessageFormat.format(template, templateArgs);
		return message;
	}

	private void bindParamsToPreparedStatement(PreparedStatement st,
			Object[] arguments) throws SQLException {
		int i = 0;
		if (arguments != null) {

			for (Object arg : arguments) {
				i++;
				if (arg == null) {
					st.setNull(i, Types.NULL);
				} else if (arg instanceof String) {
					st.setString(i, (String) arg);
				} else if (arg instanceof Integer) {
					st.setInt(i, (Integer) arg);
				} else if (arg instanceof Boolean) {
					st.setBoolean(i, (Boolean) arg);
				} else if (arg instanceof Short) {
					st.setShort(i, (Short) arg);
				} else if (arg instanceof Date) {
					st.setTimestamp(i,
							new java.sql.Timestamp(((Date) arg).getTime()));
				} else if (arg instanceof java.sql.Date) {
					st.setDate(i, new java.sql.Date(((Date) arg).getTime()));
				} else if (arg instanceof Double) {
					st.setDouble(i, (Double) arg);
				} else if (arg instanceof Long) {
					st.setLong(i, (Long) arg);
				} else if (arg instanceof BigDecimal) {
					st.setObject(i, arg);
				} else if (arg instanceof BigInteger) {
					st.setObject(i, arg);
				} else { // Object
					try {
						ByteArrayOutputStream bytesS = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bytesS);
						out.writeObject(arg);
						out.close();
						byte[] bytes = bytesS.toByteArray();
						bytesS.close();
						st.setBytes(i, bytes);
					} catch (IOException e) {
						throw new SQLException("Could not serialize object "
								+ arg + " for use in a PreparedStatement ");
					}
				}
			}
		}
	}
}
