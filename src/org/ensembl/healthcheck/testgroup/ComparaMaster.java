/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
 * Healthchecks for the Compara master database
 */
public class ComparaMaster extends GroupOfTests {

	public ComparaMaster() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.CheckGenomeDB.class,
			org.ensembl.healthcheck.testcase.compara.CheckTaxon.class,
			org.ensembl.healthcheck.testcase.compara.CheckTopLevelDnaFrag.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag.class,

			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId.class,

			org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class,

			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetCountsByMethod.class

		);
	}
}
