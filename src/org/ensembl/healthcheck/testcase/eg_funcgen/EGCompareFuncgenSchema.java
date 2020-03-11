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

package org.ensembl.healthcheck.testcase.eg_funcgen;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.eg_core.EGAbstractCompareSchema;

/**
 * @author jallen
 * 
 * <p>
 * 	Test for correctness of funcgen schemas, suggests a patch, if the schemas 
 * differ.
 * </p>
 *
 */
public class EGCompareFuncgenSchema extends EGAbstractCompareSchema {

	public EGCompareFuncgenSchema() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	@Override
	public void types() {
		setAppliesToType(DatabaseType.FUNCGEN);
	}
	
	@Override
	protected String getDefinitionFileKey() {
		return "funcgen_schema.file";
	}

	@Override
	protected String getMasterSchemaKey() {
		return "master.funcgen_schema";
	}

	@Override
	protected boolean assertSchemaCompatibility(
			Connection masterCon,
			Connection checkCon
	) {		
		return 
			assertSchemaTypesCompatible(masterCon, checkCon);
	}
}
