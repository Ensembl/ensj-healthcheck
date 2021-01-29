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

/**
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test to find duplicate meta key entries
 * @author dstaines
 * 
 */
public class DuplicateMetaKeys extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value,count(*) from meta where meta_key=? group by meta_value";

	private final List<String> metaKeys;

	private final MapRowMapper<String, Integer> mapper = new MapRowMapper<String, Integer>() {

		public void existingObject(Integer currentValue, ResultSet resultSet,
				int position) throws SQLException {
			throw new SQLException(
					"Duplicate key found - aggregate expression not working");
		}

		public String getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getString(1);
		}

		public Map<String, Integer> getMap() {
			return CollectionUtils.createHashMap();
		}

		public Integer mapRow(ResultSet resultSet, int position)
				throws SQLException {
			return resultSet.getInt(2);
		}
	};

	public DuplicateMetaKeys() {
		metaKeys = TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/duplicate_meta_keys.txt");
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String metaKey : metaKeys) {
			Map<String, Integer> map = template.queryForMap(META_QUERY, mapper,
					metaKey);
			for (Entry<String, Integer> e : map.entrySet()) {
				if (e.getValue() > 1) {
					ReportManager
							.problem(this, dbre.getConnection(),
									"Duplicate value " + e.getKey() + " for "
											+ metaKey + " found for "
											+ e.getValue() + " species");
				}
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find duplicate meta key/value pairs";
	}

}
