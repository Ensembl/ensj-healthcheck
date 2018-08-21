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

package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * Healthchecks for the genomic Compara databases
 * (and only this kind of database)
 */
public class ComparaIntegrity extends GroupOfTests {

	public ComparaIntegrity() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.CheckGenomeDB.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetSizeByMethod.class,
			org.ensembl.healthcheck.testcase.compara.CheckDuplicatedTaxaNames.class,
			org.ensembl.healthcheck.testcase.compara.CheckFirstLastRelease.class,
			org.ensembl.healthcheck.testcase.compara.CheckTopLevelDnaFrag.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagSpeciesNames.class,
			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.compara.MetaSpeciesID.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.compara.CheckConservationScore.class,
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
			org.ensembl.healthcheck.testcase.compara.CheckCAFETable.class,
			org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees.class,
			org.ensembl.healthcheck.testcase.compara.CheckGeneGainLossData.class,
			org.ensembl.healthcheck.testcase.compara.CheckHomology.class,
			org.ensembl.healthcheck.testcase.compara.CheckJSONObjects.class,
			org.ensembl.healthcheck.testcase.compara.CheckSequenceTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesTreeNodeAttr.class,
			org.ensembl.healthcheck.testcase.compara.CheckComparaStableIDs.class,
			org.ensembl.healthcheck.testcase.compara.CheckOrthologQCThresholds.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyCAFETables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGeneAlignTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGeneTreeTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdHomology.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsHomology.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagThresholdDs.class,
			org.ensembl.healthcheck.testcase.eg_compara.CheckEmptyLeavesTrees.class,
			org.ensembl.healthcheck.testcase.compara.MemberProductionCounts.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySpeciesTreeTables.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableDnafrag.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableGenomeDb.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMappingSession.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMethodLink.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMethodLinkSpeciesSet.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableNcbiTaxaName.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableNcbiTaxaNode.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableSpeciesSet.class,
			org.ensembl.healthcheck.testcase.eg_compara.ControlledTableSpeciesSetTag.class
		);
	}
}
