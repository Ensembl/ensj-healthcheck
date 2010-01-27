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

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.MapRowMapper;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to check whether the meta table contains the expected keys
 * @author dstaines
 *
 */
public class EgMeta extends AbstractEgCoreTestCase {

	private final static String META_QUERY = "select meta_key,meta_value from meta where species_id=?";

	private final static String SPECIES_ID_QUERY = "select distinct(species_id) from meta where species_id>0 order by species_id";

	private final MapRowMapper<String, List<String>> mapper = new MapRowMapper<String, List<String>>() {

		public void existingObject(List<String> currentValue,
				ResultSet resultSet, int position) throws SQLException {
			String string = resultSet.getString(2);
			if (StringUtils.isEmpty(string)) {
				throw new RuntimeException("Meta key "
						+ resultSet.getString(1) + " has empty value");
			}
			currentValue.add(string);
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

	public EgMeta() {
		super();
		metaKeys = resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/meta_keys.txt");
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (int speciesId : template.queryForDefaultObjectList(
				SPECIES_ID_QUERY, Integer.class)) {
			ReportManager.info(this, dbre.getConnection(),
					"Testing meta for species " + speciesId);
			Map<String, Boolean> metaKeyOut = CollectionUtils.createHashMap();
			for (String key : metaKeys) {
				metaKeyOut.put(key, false);
			}
			for (Entry<String, List<String>> meta : template.queryForMap(
					META_QUERY, mapper, speciesId).entrySet()) {
				metaKeyOut.put(meta.getKey(), true);
			}
			for (Entry<String, Boolean> e : metaKeyOut.entrySet()) {
				if (!e.getValue()) {
					passes = false;
					ReportManager.problem(this, dbre.getConnection(),
							"Meta table for " + speciesId
									+ " does not contain a value for "
									+ e.getKey());
				}
			}
		}
		return passes;
	}

}
