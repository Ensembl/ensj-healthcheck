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

public class DescriptionXrefs extends AbstractIntegerTestCase {

	public DescriptionXrefs() {
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.CORE);
		this.setDescription("Tests for {ECO: } blocks from Uniprot in descriptions.");
	}
	@Override
	protected String getSql() {
		return "SELECT count(*) FROM xref x WHERE x.description like '%{ECO:%}%'";
	}

	@Override
	protected boolean testValue(int value) {
		if (value > 0) {return false;};
		return true;
	}

	@Override
	protected String getErrorMessage(int value) {
		return "ECO evidence codes found in "+value+" xref descriptions";
	}

}
