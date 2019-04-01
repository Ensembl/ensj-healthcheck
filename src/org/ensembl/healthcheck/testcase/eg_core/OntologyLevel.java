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
 * File: OntologyLevel
 * Created by: dstaines
 * Created on: May 27, 2009
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

/**
 * Test to find ontology dbs attached to Gene but not Transcript/Translation
 * 
 * @author dstaines
 * 
 */
public class OntologyLevel extends AbstractEgCoreTestCase {

	private static final String TRANSLATION = "Translation";
	private static final String TRANSCRIPT = "Transcript";
	private final static String QUERY = "select e.db_name, ox.ensembl_object_type "
			+ "from ontology_xref oo join object_xref ox using (object_xref_id) "
			+ "join xref x using (xref_id) join external_db e using (external_db_id) "
			+ "group by e.db_name,ox.ensembl_object_type";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		Map<String, List<String>> names = getTemplate(dbre).queryForMap(QUERY,
				new MapRowMapper<String, List<String>>() {

					public List<String> mapRow(ResultSet resultSet, int position)
							throws SQLException {
						List<String> l = CollectionUtils.createArrayList();
						existingObject(l, resultSet, position);
						return l;
					}

					public Map<String, List<String>> getMap() {
						return CollectionUtils.createHashMap();
					}

					public String getKey(ResultSet resultSet)
							throws SQLException {
						return resultSet.getString(1);
					}

					public void existingObject(List<String> currentValue,
							ResultSet resultSet, int position)
							throws SQLException {
						currentValue.add(resultSet.getString(2));
					}
				});
		for (Entry<String, List<String>> e : names.entrySet()) {
			if (!e.getValue().contains(TRANSCRIPT)
					&& !e.getValue().contains(TRANSLATION)) {
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"Ontology db "
										+ e.getKey()
										+ " is not attached to "
										+ TRANSCRIPT
										+ " or "
										+ TRANSLATION
										+ ": this will not be displayed in the current web interface");
				passes = false;
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find ontology dbs attached to Gene but not Transcript/Translation";
	}

}
