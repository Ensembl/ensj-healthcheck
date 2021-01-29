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
 * File: SharedDisplayXref.java
 * Created by: dstaines
 * Created on: Feb 5, 2010
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

/**
 * Test to find if gene display xrefs are shared between species. If this
 * happens, it can lead to species-specific synonyms being applied to the wrong species.
 * 
 * @author dstaines
 * 
 */
public class SharedDisplayXref extends AbstractEgCoreTestCase {
	
	private static final String SHARED_GENE_COUNT = "select x.dbprimary_acc,count(distinct(cs.species_id)) "
			+ "from xref x join gene g on (g.display_xref_id=x.xref_id and "
			+ "g.biotype='protein_coding') join seq_region sr using (seq_region_id) "
			+ "join coord_system cs using (coord_system_id) "
			+ "group by x.dbprimary_acc having count(distinct(cs.species_id))>1";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		for (Entry<String, Integer> e : getTemplate(dbre).queryForMap(
				SHARED_GENE_COUNT, new MapRowMapper<String, Integer>() {

					public void existingObject(Integer currentValue,
							ResultSet resultSet, int position)
							throws SQLException {
					}

					public String getKey(ResultSet resultSet)
							throws SQLException {
						return resultSet.getString(1);
					}

					public Map<String, Integer> getMap() {
						return CollectionUtils.createHashMap();
					}

					public Integer mapRow(ResultSet resultSet, int position)
							throws SQLException {
						return resultSet.getInt(2);
					}
				}).entrySet()) {
			ReportManager.problem(this, dbre.getConnection(), "Gene name "
					+ e.getKey() + " shared between " + e.getValue()
					+ " species");
			result = false;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to find if gene display xrefs are shared between species. " +
				"If this happens, it can lead to species-specific synonyms being applied to the wrong species.";
	}

}
