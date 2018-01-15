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

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test to check whether the meta table contains the expected keys
 * 
 * @author dstaines
 * 
 */
public abstract class AbstractEgMeta extends AbstractEgCoreTestCase {

	protected final static String META_QUERY = "select distinct meta_key from meta where species_id=?";

	protected final static String SPECIES_ID_QUERY = "select distinct(species_id) from meta where species_id>0 order by species_id";

	protected final MapRowMapper<String, List<String>> mapper = new MapRowMapper<String, List<String>>() {

		public void existingObject(List<String> currentValue,
				ResultSet resultSet, int position) throws SQLException {
			String string = resultSet.getString(2);
			if(!StringUtils.isEmpty(string)) {
				currentValue.add(string);
			}
		}

		public String getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getString(1);
		}

		public Map<String, List<String>> getMap() {
			return CollectionUtils.createHashMap();
		}

		public List<String> mapRow(ResultSet resultSet, int position)
				throws SQLException {
			List<String> vals = CollectionUtils.createArrayList();
			existingObject(vals, resultSet, position);
			return vals;
		}

	};

	private final List<String> metaKeys;
	
	public AbstractEgMeta() {
	  this(TestCaseUtils.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/meta_keys.txt"));
	}

	public AbstractEgMeta(List<String> metaKeys) {
		super();
		this.metaKeys = metaKeys;
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (int speciesId : template.queryForDefaultObjectList(
				SPECIES_ID_QUERY, Integer.class)) {
			passes &= testKeys(dbre, speciesId, getKeys(template, speciesId), metaKeys);
		}
		return passes;
	}

	protected List<String> getKeys(SqlTemplate template, int speciesId) {
		return template.queryForDefaultObjectList(META_QUERY, String.class, speciesId);
	}

	protected abstract boolean testKeys(DatabaseRegistryEntry dbre, int speciesId,
			List<String> speciesKeys, List<String> testKeys);
	

}
