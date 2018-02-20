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

package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Test to see if schema uses non-myisam storage engines
 * 
 * @author dstaines
 * 
 */
public class MySQLStorageEngine extends AbstractTemplatedTestCase {

	private final static String MYISAM = "MyISAM";

	private final static String ENGINE_QUERY = "select count(*) from information_schema.tables where table_schema=? and engine<>'" + MYISAM + "'";

	public MySQLStorageEngine() {

                setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	public void types() {
		this.addAppliesToType(DatabaseType.CORE);
		this.addAppliesToType(DatabaseType.VARIATION);
		this.addAppliesToType(DatabaseType.FUNCGEN);
		this.addAppliesToType(DatabaseType.COMPARA);
                this.addAppliesToType(DatabaseType.CDNA);
                this.addAppliesToType(DatabaseType.OTHERFEATURES);
                this.addAppliesToType(DatabaseType.RNASEQ);
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		int count = getTemplate(dbre).queryForDefaultObject(ENGINE_QUERY, Integer.class, dbre.getName());
		if (count > 0) {
			ReportManager.problem(this, dbre.getConnection(), count + " tables from " + dbre.getName() + " do not use " + MYISAM);
			return false;
		} else {
			return true;
		}
	}

}
