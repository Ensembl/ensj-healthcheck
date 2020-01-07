/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * Test to find duplicate meta key entries
 * 
 * @author dstaines
 * 
 */
public class GeneGC extends AbstractEgCoreTestCase {

	public GeneGC() {
		super();
		removeAppliesToType(DatabaseType.OTHERFEATURES);
	}

	private static final String ATTRIB_TYPE_QUERY = "SELECT attrib_type_id FROM attrib_type WHERE code='GeneGC'";
	private final static String GC_QUERY = "select attrib_type_id,count(*) from gene "
			+ "join gene_attrib using (gene_id) join attrib_type using (attrib_type_id) "
			+ "join seq_region using (seq_region_id) join coord_system using (coord_system_id) "
			+ "where code like 'GeneGC' and species_id=? group by attrib_type_id";
	private static final String ATTR_ID = "142";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);

		// 1. check for GC attribute as correct value
		List<Integer> gcAtt = template.queryForDefaultObjectList(
				ATTRIB_TYPE_QUERY, Integer.class);
		if (gcAtt.size() > 1) {
			ReportManager.problem(this, dbre.getConnection(),
					"More than one GeneGC attrib_type found");
			passes = false;
		} else {
			Integer att = CollectionUtils.getFirstElement(gcAtt, null);
			if (att != null) {
				if (att != Integer.parseInt(ATTR_ID)) {
					ReportManager.problem(this, dbre.getConnection(),
							"GeneGC attrib_type has ID " + att + " instead of "
									+ ATTR_ID);
					passes = false;
				}
			} else if(dbre.getType()==DatabaseType.OTHERFEATURES) {
				ReportManager.problem(this, dbre.getConnection(),
						"No GeneGC attrib_type found");
				passes = false;
			}
		}

		for (int speciesId : dbre.getSpeciesIds()) {
			Map<String, Integer> count = template.queryForMap(GC_QUERY,
					TestCaseUtils.countMapper, speciesId);
			if (count.size() > 1) {
				ReportManager.problem(this, dbre.getConnection(),
						"More than one GeneGC count attrib found for genes on species "
								+ speciesId);
				passes = false;
			} else {
				Entry<String, Integer> e = CollectionUtils.getFirstElement(
						count.entrySet(), null);
				if (e == null) {
					ReportManager.problem(this, dbre.getConnection(),
							"No GeneGC attrib found for genes on species "
									+ speciesId);
					passes = false;
				} else {
					if (!ATTR_ID.equals(e.getKey())) {
						ReportManager.problem(this, dbre.getConnection(),
								"GeneGC attrib has ID " + e.getKey()
										+ " instead of " + ATTR_ID
										+ " for species " + speciesId);
						passes = false;
					}
					if (e.getValue() < 1) {
						ReportManager.problem(this, dbre.getConnection(),
								"No values found for GeneGC attrib (ID "
										+ e.getKey() + ") for species "
										+ speciesId);
						passes = false;
					}
				}
			}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if GeneGC has been run and has correct analysis_id";
	}

}
