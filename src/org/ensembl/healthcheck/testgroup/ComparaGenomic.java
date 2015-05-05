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
 * These are the tests that register themselves as compara_genomic. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.eg_compara.EGMethodLinkSpeciesSetIdStats </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckConservationScore </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignGenomeDBs </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.MLSSTagSpeciesNames </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag </li>  
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSynteny </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckTableSizes </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignmentTables </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckMLSSIDConsistencyInGenomicAlign </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignTreeTable </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeySpeciesTreeTables </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyTables </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.Meta </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.MetaSpeciesID </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.MLSSTagGERPMSA </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.MLSSTagMaxAlign </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.SingleDBCollations </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class ComparaGenomic extends GroupOfTests {
	
	public ComparaGenomic() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsPairwiseAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsMultipleAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagStatsSynteny.class,
			org.ensembl.healthcheck.testcase.compara.CheckConservationScore.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignGenomeDBs.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagSpeciesNames.class,
			org.ensembl.healthcheck.testcase.compara.CheckPairAlignerUniqueMethod.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag.class,
			org.ensembl.healthcheck.testcase.compara.CheckSynteny.class,
			org.ensembl.healthcheck.testcase.compara.CheckTableSizes.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetSizeByMethod.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyConservationTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignmentTables.class,
			org.ensembl.healthcheck.testcase.compara.CheckMLSSIDConsistencyInGenomicAlign.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignTreeTable.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySpeciesTreeTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyTables.class,
			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.compara.MetaSpeciesID.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagGERPMSA.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagMaxAlign.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagHighCoverageMSA.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class
		);
	}
}
