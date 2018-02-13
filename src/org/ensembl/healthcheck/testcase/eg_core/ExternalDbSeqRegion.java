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

package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

public class ExternalDbSeqRegion extends AbstractEgCoreTestCase {

	private final static String CHECK_SQL = "select count(*) from coord_system cs " +
			"join seq_region s using (coord_system_id) " +
			"join seq_region_attrib sa using (seq_region_id) " +
			"join attrib_type at using (attrib_type_id) " +
			"where cs.attrib like '%sequence_level%' and at.code='external_db'";

	private final static String FIX_SQL ="insert into seq_region_attrib " +
			"select s.seq_region_id,at.attrib_type_id,1 " +
			"from coord_system cs join seq_region s using (coord_system_id) " +
			"left join attrib_type at ON (at.code='external_db') " +
			"where cs.attrib like '%sequence_level%'";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		int n = getTemplate(dbre).queryForDefaultObject(CHECK_SQL, Integer.class);
		if(n==0) {
			ReportManager.problem(this, dbre.getConnection(), "No external_db attributes found on seq_level regions. Useful SQL to add attribs to ENA: "+FIX_SQL);
			return false;
				} else {
				return true;	
				}
		}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test for external-db attributes on seq_level regions";
	}

}
