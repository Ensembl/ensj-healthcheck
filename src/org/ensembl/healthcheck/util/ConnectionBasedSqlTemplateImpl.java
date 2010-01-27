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
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

/**
 * The default implementation of {@link SqlTemplate} which provides all the
 * basic functionality that should be expected from a class which implements
 * this class. A {@link Connection} is passed into the constructor that is used for all
 * queries. Note that this should not be closed independently.
 *
 * @author ayates
 * @author dstaines
 */
public class ConnectionBasedSqlTemplateImpl implements SqlTemplate {

	public class SqlTemplateUncheckedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public SqlTemplateUncheckedException(String message, Throwable cause) {
			super(message, cause);
		}

		public SqlTemplateUncheckedException(String message) {
			super(message);
		}

	}

	private final Connection connection;
	private final String uri;

	public String getUri() {
		return uri;
	}

	public ConnectionBasedSqlTemplateImpl(Connection connection){
		this.connection = connection;
		try {
			this.uri = connection.getMetaData().getURL();
		} catch (SQLException e) {
			throw new SqlTemplateUncheckedException("Could not get URL for connection");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> mapResultSetToList(ResultSet resultSet,
			RowMapper<T> mapper, final int rowLimit, String sql, Object[] args)
			throws SqlTemplateUncheckedException {
		List<T> output = new ArrayList<T>();
		int position = 0;
		boolean inspectRowCount = (rowLimit > 0);

		try {
			while (resultSet.next()) {

				if (inspectRowCount && position > rowLimit) {
					String expected = Integer.toString(rowLimit);
					String actual = Integer.toString(position);
					String exceptionMessage = MessageFormat
							.format(
									"Too many rows returned. "
											+ "Expected {0} but actual row count was {1}",
									new Object[] { expected, actual });
					String message = formatExceptionMessage(exceptionMessage,
							sql, args);
					throw new SqlTemplateUncheckedException(message);
				}

				output.add(mapper.mapRow(resultSet, position));
				position++;
			}

			if (inspectRowCount && position == 0) {
				String message = formatExceptionMessage(
						"Did not find any rows", sql, args);
				throw new SqlTemplateUncheckedException(message);
			}
		} catch (SQLException e) {
			String message = formatExceptionMessage(
					"Encountered problem whilst mapping ResultSet to Object List",
					sql, args);
			throw new SqlTemplateUncheckedException(message, e);
		}

		return output;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T mapResultSetToSingleObject(ResultSet resultSet,
			RowMapper<T> mapper, String sql, Object[] args)
			throws SqlTemplateUncheckedException {
		List<T> results = mapResultSetToList(resultSet, mapper, 1, sql, args);
		return results.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... args)
			throws SqlTemplateUncheckedException {
		ResultSet resultSet = null;
		try {
			resultSet = executeSql(sql, args);
			T output = mapResultSetToSingleObject(resultSet, mapper, sql, args);
			return output;
		} finally {
			closeDbObject(resultSet);
		}
	}

	public ResultSet executeSql(String sql, Object[] args) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			bindParamsToPreparedStatement(ps, args);
			rs = ps.executeQuery();
		} catch (SQLException e) {
			throw createUncheckedException(sql, args, e);
		} finally {
			//closeDbObject(ps);
		}
		return rs;
	}

	protected void closeDbObject(ResultSet resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException e) {
			// ignore closing exceptions here
		}
	}

	protected void closeDbObject(PreparedStatement resultSet) {
		try {
			if (resultSet != null)
				resultSet.close();
		} catch (SQLException e) {
			// ignore closing exceptions here
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> queryForList(String sql, RowMapper<T> mapper,
			Object... args) throws SqlTemplateUncheckedException {
		ResultSet resultSet = null;
		try {
			resultSet = executeSql(sql, args);
			List<T> output = mapResultSetToList(resultSet, mapper, -1, sql,
					args);
			return output;
		} finally {
			closeDbObject(resultSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T queryForDefaultObject(String sql, Class<T> expected,
			Object... args) throws SqlTemplateUncheckedException {
		DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(
				expected, 1);
		return queryForObject(sql, mapper, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> List<T> queryForDefaultObjectList(String sql, Class<T> expected,
			Object... args) throws SqlTemplateUncheckedException {
		DefaultObjectRowMapper<T> mapper = new DefaultObjectRowMapper<T>(
				expected, 1);
		return queryForList(sql, mapper, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <K, T> Map<K, T> queryForMap(String sql,
			MapRowMapper<K, T> mapRowMapper, Object... args)
			throws SqlTemplateUncheckedException {

		Map<K, T> targetMap = mapRowMapper.getMap();

		ResultSet resultSet = null;
		try {
			resultSet = executeSql(sql, args);
			int position = -1;
			while (resultSet.next()) {
				position++;
				K key = mapRowMapper.getKey(resultSet);
				if (targetMap.containsKey(key)) {
					T currentValue = targetMap.get(key);
					mapRowMapper.existingObject(currentValue, resultSet,
							position);
				} else {
					T newValue = mapRowMapper.mapRow(resultSet, position);
					targetMap.put(key, newValue);
				}
			}
		} catch (SQLException e) {
			throw new SqlTemplateUncheckedException(
					"Cannot map from result set into Map", e);
		} finally {
			closeDbObject(resultSet);
		}

		return targetMap;
	}

	// ----- EXCEPTION HANDLING

	/**
	 * Used to generically raise unchecked exceptions from sql service
	 * exceptions
	 */
	private SqlTemplateUncheckedException createUncheckedException(String sql,
			Object[] params, Throwable e) {
		String message = formatExceptionMessage(
				"Could not run statement because of SqlServiceException", sql,
				params);
		return new SqlTemplateUncheckedException(message, e);
	}

	private int twoDTraceLimit = 3;

	private String formatExceptionMessage(String exceptionMessage, String sql,
			Object[][] params) {
		List<Object> listArgs = new ArrayList<Object>();

		int loop = (params.length > twoDTraceLimit) ? twoDTraceLimit
				: params.length;

		for (int i = 0; i < loop; i++) {
			Object[] arg = params[i];
			listArgs.add(Arrays.toString(arg));
		}

		if (params.length > twoDTraceLimit) {
			listArgs.add("More params lines than can show (" + params.length
					+ ") ...");
		}

		Object[] args = listArgs.toArray(new Object[0]);
		return formatExceptionMessage(exceptionMessage, sql, args);
	}

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
				if (arg == null) {
					st.setNull(++i, Types.NULL);
				} else if (arg instanceof String) {
					st.setString(++i, (String) arg);
				} else if (arg instanceof Integer) {
					st.setInt(++i, (Integer) arg);
				} else if (arg instanceof Boolean) {
					st.setBoolean(++i, (Boolean) arg);
				} else if (arg instanceof Short) {
					st.setShort(++i, (Short) arg);
				} else if (arg instanceof Date) {
					st.setTimestamp(++i, new java.sql.Timestamp(((Date) arg)
							.getTime()));
				} else if (arg instanceof java.sql.Date) {
					st.setDate(++i, new java.sql.Date(((Date) arg).getTime()));
				} else if (arg instanceof Double) {
					st.setDouble(++i, (Double) arg);
				} else if (arg instanceof Long) {
					st.setLong(++i, (Long) arg);
				} else if (arg instanceof BigDecimal) {
					st.setObject(++i, arg);
				} else if (arg instanceof BigInteger) {
					st.setObject(++i, arg);
				} else { // Object
					try {
						ByteArrayOutputStream bytesS = new ByteArrayOutputStream();
						ObjectOutputStream out = new ObjectOutputStream(bytesS);
						out.writeObject(arg);
						out.close();
						byte[] bytes = bytesS.toByteArray();
						bytesS.close();
						st.setBytes(++i, bytes);
					} catch (IOException e) {
						throw new SQLException("Could not serialize object "
								+ arg + " for use in a PreparedStatement ");
					}
				}
			}
		}
	}

}
