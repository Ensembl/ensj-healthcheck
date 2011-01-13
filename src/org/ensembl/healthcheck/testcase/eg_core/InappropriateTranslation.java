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
import org.ensembl.healthcheck.util.TestCaseUtils;

public class InappropriateTranslation extends AbstractEgCoreTestCase {

	private final static String TRANSLATION_QUERY = "select biotype,count(*) from transcript join translation using (transcript_id) group by biotype";
	private final List<String> permittedTypes;
	private final MapRowMapper<String, Integer> mapper = new MapRowMapper<String, Integer>() {

		public void existingObject(Integer currentValue, ResultSet resultSet,
				int position) throws SQLException {
			throw new SQLException("Duplicate entry found for "
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
			return resultSet.getInt(2);
		}

	};

	public InappropriateTranslation() {
		this.permittedTypes = TestCaseUtils
				.resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/allowed_translations.txt");
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean success = true;
		Map<String, Integer> results = this.getTemplate(dbre).queryForMap(
				TRANSLATION_QUERY, mapper);
		for (Entry<String, Integer> e : results.entrySet()) {
			if (!permittedTypes.contains(e.getKey())) {
				ReportManager.problem(this, dbre.getConnection(), e.getValue()
						+ " " + e.getKey()
						+ " transcripts found with translations");
				success = false;
			}
		}
		return success;
	}

}
