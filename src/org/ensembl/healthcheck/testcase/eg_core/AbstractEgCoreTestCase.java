/**
 * File: SampleMetaTestCase.java
 * Created by: dstaines
 * Created on: May 1, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.ConnectionBasedSqlTemplateImpl;
import org.ensembl.healthcheck.util.InputOutputUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Base class for EnsemblGenomes healthchecks including some new helpers
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractEgCoreTestCase extends SingleDatabaseTestCase {

	public static final MapRowMapper<String, String> singleValueMapper = new MapRowMapper<String, String>() {

		public void existingObject(String currentValue, ResultSet resultSet,
				int position) throws SQLException {
			throw new RuntimeException("Duplicate meta key found for "
					+ getKey(resultSet));
		}

		public String getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getString(1);
		}

		public Map<String, String> getMap() {
			return CollectionUtils.createHashMap();
		}

		public String mapRow(ResultSet resultSet, int position)
				throws SQLException {
			String value = resultSet.getString(2);
			if (StringUtils.isEmpty(value)) {
				throw new RuntimeException("Key " + getKey(resultSet)
						+ " has empty value");
			}
			return value;
		}

	};

	public static final MapRowMapper<String, Integer> countMapper = new MapRowMapper<String, Integer>() {

		public void existingObject(Integer currentValue, ResultSet resultSet,
				int position) throws SQLException {
			throw new RuntimeException("Duplicate key found for "
					+ getKey(resultSet));
		}

		public String getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getString(1);
		}

		public Map<String, Integer> getMap() {
			return CollectionUtils.createHashMap();
		}

		public Integer mapRow(ResultSet resultSet, int position)
				throws SQLException {
			Integer value = resultSet.getInt(2);
			if (value == null) {
				throw new RuntimeException("Key " + getKey(resultSet)
						+ " has empty value");
			}
			return value;
		}

	};

	public final static String EG_GROUP = "ensembl_genomes";
	private final static String BINOMIAL_QUERY = "select meta_value from meta where meta_key='species.classification' and species_id=? order by meta_id limit 2";

	public AbstractEgCoreTestCase() {
		super();
		this.addToGroup(EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
	}

	protected SqlTemplate getTemplate(DatabaseRegistryEntry dbre) {
		return new ConnectionBasedSqlTemplateImpl(dbre.getConnection());
	}

	protected abstract boolean runTest(DatabaseRegistryEntry dbre);

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean passes = false;
		try {
			passes = runTest(dbre);
			if (passes) {
				ReportManager
						.correct(this, dbre.getConnection(), "Test passed");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			ReportManager
					.problem(this, dbre.getConnection(),
							"Test failed due to unexpected exception "
									+ e.getMessage());
		}
		return passes;
	}

	public static String resourceToInList(String resourceName) {
		return resourceToInList(resourceName, true);
	}

	public static String resourceToInList(String resourceName, boolean quoted) {
		return listToInList(resourceToStringList(resourceName), quoted);
	}

	public static String listToInList(List<String> ss) {
		return listToInList(ss, true);
	}

	public static String listToInList(List<String> ss, boolean quoted) {
		if (quoted) {
			ss = quoteList(ss);
		}
		return StringUtils.join(ss.iterator(), ',');
	}

	public static List<String> quoteList(List<String> in) {
		return quoteList(in, "'", "'");
	}

	public static List<String> quoteList(List<String> in, String before,
			String after) {
		List<String> out = CollectionUtils.createArrayList(in.size());
		for (String s : in) {
			out.add(before + s + after);
		}
		return out;
	}

	public static List<String> resourceToStringList(String resourceName) {
		return Arrays.asList(InputOutputUtils
				.slurpTextClasspathResourceToString(resourceName).split("\\n"));
	}

	/**
	 * @param template
	 * @param speciesId
	 * @return
	 */
	public static String getBinomialName(SqlTemplate template, int speciesId) {
		List<String> bin = template.queryForDefaultObjectList(BINOMIAL_QUERY,
				String.class, speciesId);
		return bin.get(1) + " " + bin.get(0);
	}

	private final static Pattern BINOMIAL_PATTERN = Pattern
			.compile("^[A-Z][a-z]+ [a-z]+$");

	/**
	 * @param name
	 *            binomial name to check
	 * @return true if name matches classic binomial name (no special characters
	 *         etc.)
	 */
	public static boolean isValidBinomial(String name) {
		return BINOMIAL_PATTERN.matcher(name).matches();
	}

}
