/**
 * File: InterproFeatureTest.java
 * Created by: dstaines
 * Created on: May 6, 2009
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
import org.ensembl.healthcheck.util.TemplateBuilder;


/**
 * Test to see if interpro xrefs and features agree
 * @author dstaines
 *
 */
public class InterproFeature extends AbstractEgCoreTestCase {

	private final static String GET_XREFS = "select t.translation_id,count(*) from translation t "
			+ "join object_xref ox on (ox.ensembl_object_type='Translation' and "
			+ "ox.ensembl_id=t.translation_id) join xref x using (xref_id) where x.external_db_id=1200 group by t.translation_id";

	private final static String GET_FEATURES = "select t.translation_id,count(*) from translation t "
			+ "join protein_feature pf using (translation_id) join analysis a using (analysis_id) "
			+ " where a.logic_name in "
			+ "($inlist$)"
			+ " group by t.translation_id";

	MapRowMapper<Integer, Integer> featureMapper = new MapRowMapper<Integer, Integer>() {

		public void existingObject(Integer currentValue, ResultSet resultSet,
				int position) throws SQLException {
			throw new UnsupportedOperationException();
		}

		public Integer getKey(ResultSet resultSet) throws SQLException {
			return resultSet.getInt(1);
		}

		public Map<Integer, Integer> getMap() {
			return CollectionUtils.createHashMap();
		}

		public Integer mapRow(ResultSet resultSet, int position)
				throws SQLException {
			return resultSet.getInt(2);
		}

	};
	private final String feature_query;
	public InterproFeature() {
		super();
		feature_query = TemplateBuilder.template(GET_FEATURES, "inlist", resourceToInList("/org/ensembl/healthcheck/testcase/eg_core/interpro_names.txt"));
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.healthcheck.testcase.SingleDatabaseTestCase#run(org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		SqlTemplate srv = getTemplate(dbre);
		boolean passes = true;
		// 1. get features for each translation into a map
		Map<Integer, Integer> features = srv.queryForMap(feature_query,
				featureMapper);
		// 2. get xrefs into a map
		Map<Integer, Integer> xrefs = srv.queryForMap(GET_XREFS, featureMapper);
		for (Entry<Integer, Integer> e : xrefs.entrySet()) {
			if(features.containsKey(e.getKey())) {
			int nFeatures = features.get(e.getKey());
			int nXrefs = e.getValue();
			if (nFeatures < nXrefs) {
				passes = false;
				// note failure
				ReportManager.warning(this, dbre.getConnection(), nXrefs
						+ " InterPro xrefs but only " + nFeatures
						+ " interpro features found for translation "
						+ e.getKey());
			}
			}
		}
		if (passes) {
			ReportManager.correct(this, dbre.getConnection(),
					"Sufficient InterPro features found for translations with InterPro xrefs");
		}
		return passes;
	}

}
