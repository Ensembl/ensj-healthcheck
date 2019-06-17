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
 * Healthchecks for the genomic Compara databases
 * (and only this kind of database)
 */
public class ComparaGenomicOnly extends GroupOfTests {
	
	public ComparaGenomicOnly() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.AlignmentCoordinates.class,
			org.ensembl.healthcheck.testcase.compara.CheckConservationScore.class,
			org.ensembl.healthcheck.testcase.compara.CheckConservationScorePerBlock.class,
			org.ensembl.healthcheck.testcase.compara.CheckConstrainedElementTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignGenomeDBs.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignTreeTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckMLSSIDConsistencyInGenomicAlign.class,
			org.ensembl.healthcheck.testcase.compara.CheckPairAlignerUniqueMethod.class,
			org.ensembl.healthcheck.testcase.compara.CheckMSANames.class,
			org.ensembl.healthcheck.testcase.compara.CheckSynteny.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyConservationTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignmentTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyTables.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagGERPMSA.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagHighCoverageMSA.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagMaxAlign.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsMultipleAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsPairwiseAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsSynteny.class,
			org.ensembl.healthcheck.testcase.compara.MultipleGenomicAlignBlockIds.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignCoverage.class
		);
	}
}
