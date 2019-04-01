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

/**
 * Healthchecks for all Compara databases
 */
public class ComparaShared extends GroupOfTests {

	public ComparaShared() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.CheckGenomeDB.class,
			org.ensembl.healthcheck.testcase.compara.CheckMethodLinkSpeciesSetTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetSizeByMethod.class,
			org.ensembl.healthcheck.testcase.compara.CheckDuplicatedTaxaNames.class,
			org.ensembl.healthcheck.testcase.compara.CheckFirstLastRelease.class,
			org.ensembl.healthcheck.testcase.compara.CheckTopLevelDnaFrag.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagSpeciesNames.class,

			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.compara.MetaSpeciesID.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class
		);
	}
}
