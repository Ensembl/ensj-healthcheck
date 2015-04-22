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
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckMethodLinkSpeciesSetTag </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag </li>  
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSynteny </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckTableSizes </li> 
 *   <li> org.ensembl.healthcheck.testcase.eg_compara.EGForeignKeyMethodLinkSpeciesSetId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignBlockId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignId </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignNodeId </li>  
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyRegionId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.Meta </li> 
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
			org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetIdStatsPairwiseAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetIdStatsMultipleAlignment.class,
			org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetIdStatsSynteny.class,
			org.ensembl.healthcheck.testcase.compara.CheckConservationScore.class,
			org.ensembl.healthcheck.testcase.compara.CheckGenomicAlignGenomeDBs.class,
			org.ensembl.healthcheck.testcase.compara.CheckMethodLinkSpeciesSetTag.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetTag.class,
			org.ensembl.healthcheck.testcase.compara.CheckSynteny.class,
			org.ensembl.healthcheck.testcase.compara.CheckTableSizes.class,
			org.ensembl.healthcheck.testcase.compara.CheckWGASpeciesTree.class,
			org.ensembl.healthcheck.testcase.eg_compara.EGForeignKeyMethodLinkSpeciesSetId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignBlockId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignNodeId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetIdGenomicAlignBlock.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdGenomic.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySyntenyRegionId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId.class,
			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagGERPMSA.class,
			org.ensembl.healthcheck.testcase.compara.MLSSTagMaxAlign.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class
		);
	}
}
