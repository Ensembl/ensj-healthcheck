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
 * These are the tests that register themselves as compara_homology. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.eg_compara.CheckEmptyLeavesTrees </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckGeneGainLossData </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckHomology </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckMethodLinkSpeciesSetTag </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSequenceTable </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckSpeciesTreeNodeTag </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.CheckTableSizes </li>  
 *   <li> org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb </li>
 *   <li> org.ensembl.healthcheck.testcase.eg_compara.EGCheckNoTreeStableIds </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyCAFETables </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId </li>  
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyGeneTrees </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyId </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdHomology </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.ForeignKeySpeciesTreeTables </li>
 *   <li> org.ensembl.healthcheck.testcase.eg_compara.MemberProductionCounts </li>
 *   <li> org.ensembl.healthcheck.testcase.compara.Meta </li> 
 *   <li> org.ensembl.healthcheck.testcase.compara.SingleDBCollations </li> 
 * </ul>
 *
 * @author Thomas Maurel
 *
 */
public class ComparaHomology extends GroupOfTests {
	
	public ComparaHomology() {

		addTest(
			org.ensembl.healthcheck.testcase.compara.CheckFlatProteinTrees.class,
			org.ensembl.healthcheck.testcase.compara.CheckGeneGainLossData.class,
			org.ensembl.healthcheck.testcase.compara.CheckHomology.class,
			org.ensembl.healthcheck.testcase.compara.CheckMethodLinkSpeciesSetTag.class,
			org.ensembl.healthcheck.testcase.compara.CheckSequenceTable.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesTreeNodeTag.class,
			org.ensembl.healthcheck.testcase.compara.CheckTableSizes.class,
			org.ensembl.healthcheck.testcase.compara.DuplicateGenomeDb.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyCAFETables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyFamilyId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyGeneTrees.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyHomologyId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMasterTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMemberTables.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkSpeciesSetId.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeyMLSSIdHomology.class,
			org.ensembl.healthcheck.testcase.compara.ForeignKeySpeciesTreeTables.class,
			org.ensembl.healthcheck.testcase.compara.Meta.class,
			org.ensembl.healthcheck.testcase.compara.MethodLinkSpeciesSetIdStatsHomology.class,
			org.ensembl.healthcheck.testcase.compara.SingleDBCollations.class,
			org.ensembl.healthcheck.testcase.generic.MySQLStorageEngine.class,
			org.ensembl.healthcheck.testcase.eg_compara.CheckEmptyLeavesTrees.class,
			org.ensembl.healthcheck.testcase.eg_compara.EGCheckNoTreeStableIds.class,
			org.ensembl.healthcheck.testcase.compara.CheckSpeciesSetCountsByMethod.class,
			org.ensembl.healthcheck.testcase.eg_compara.MemberProductionCounts.class
		);
	}
}
