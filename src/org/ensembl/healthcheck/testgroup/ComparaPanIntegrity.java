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

package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;
import org.ensembl.healthcheck.testcase.compara.CheckSynteny;
import org.ensembl.healthcheck.testcase.compara.CheckDuplicatedTaxaNames;
import org.ensembl.healthcheck.testcase.compara.CheckTopLevelDnaFrag;
import org.ensembl.healthcheck.testcase.compara.MLSSTagStatsHomology;
import org.ensembl.healthcheck.testcase.compara.MLSSTagThresholdDs;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables;
/**
 * Healthchecks for the genomic Compara databases
 * (and only this kind of database)
 */
public class ComparaPanIntegrity extends ComparaIntegrity {

	public ComparaPanIntegrity() {

		setDescription("Group of test for the Pan compara database");
		addTest(
			org.ensembl.healthcheck.testcase.eg_compara.ForeignKeyPanMasterTables.class
		);
		removeTest(CheckSynteny.class, CheckDuplicatedTaxaNames.class, CheckTopLevelDnaFrag.class, MLSSTagStatsHomology.class, MLSSTagThresholdDs.class, ForeignKeyMasterTables.class);
	}
}
