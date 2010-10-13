/**
 * File: EgMetaTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to find duplicate taxids entries
 * 
 * @author dstaines
 * 
 */
public class DuplicateTaxonomyIds extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_value,count(*) from meta where meta_key='species.taxonomy_id' group by meta_value having count(*)>1";

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

	public DuplicateTaxonomyIds() {
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		Map<String, Integer> map = template.queryForMap(META_QUERY, mapper);
		for (Entry<String, Integer> e : map.entrySet()) {
			if (e.getValue() > 1) {
				ReportManager.problem(this, dbre.getConnection(),
						"Duplicate taxonomy ID " + e.getKey() + " found for "
								+ e.getValue() + " species");
				passes = false;
			}
		}
		return passes;
	}

}
