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

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find translations on genes with biotypes for which they are inappropriate";
	}

}
