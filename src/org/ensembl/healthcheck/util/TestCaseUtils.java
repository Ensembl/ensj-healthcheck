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

/**
 * File: TestCaseUtils.java
 * Created by: dstaines
 * Created on: Mar 16, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * @author dstaines
 * 
 */
public class TestCaseUtils {

	public static String resourceToInList(String resourceName) {
		return TestCaseUtils.resourceToInList(resourceName, true);
	}

	public static String resourceToInList(String resourceName, boolean quoted) {
		return TestCaseUtils.listToInList(TestCaseUtils
				.resourceToStringList(resourceName), quoted);
	}

	public static String listToInList(List<String> ss) {
		return TestCaseUtils.listToInList(ss, true);
	}

	public static String listToInList(List<String> ss, boolean quoted) {
		if (quoted) {
			ss = TestCaseUtils.quoteList(ss);
		}
		return StringUtils.join(ss.iterator(), ',');
	}

	public static List<String> quoteList(List<String> in) {
		return TestCaseUtils.quoteList(in, "'", "'");
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
	 * @return name
	 */
	public static String getBinomialName(SqlTemplate template, int speciesId) {
		List<String> bin = template.queryForDefaultObjectList(
				TestCaseUtils.BINOMIAL_QUERY, String.class, speciesId);
		return bin.get(1) + " " + bin.get(0);
	}

	public static String getBinomialNameMulti(SqlTemplate template,
			int speciesId) {
		List<String> bin = template.queryForDefaultObjectList(
				TestCaseUtils.BINOMIAL_QUERY, String.class, speciesId);
		return bin.get(0);
	}

	public final static Pattern BINOMIAL_PATTERN = Pattern
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

	public final static String BINOMIAL_QUERY = "select meta_value from meta where meta_key='species.classification' and species_id=? order by meta_id limit 2";
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

}
