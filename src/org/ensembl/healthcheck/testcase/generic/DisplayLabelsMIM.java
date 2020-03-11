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

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

public class DisplayLabelsMIM extends AbstractIntegerTestCase{

	public DisplayLabelsMIM() {
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.CORE);
		this.setDescription("Tests for improper import of MIM data.");
	}
	
	protected String getSql (){
		return "SELECT x.display_label, count(x.xref_id) FROM xref x, external_db e WHERE e.`db_name` "
				+ "IN ('MIM','MIM_MORBID','MIM_GENE') AND e.external_db_id = x.external_db_id "
				+ "AND x.display_label REGEXP '\\n'";
	}
	
	protected boolean testValue(int count) {
		if (count == 0) { return true; }
		return false;
	}
	
	protected String getErrorMessage (int count) {
		return "Found linefeeds in MIM xrefs";
	}
}
