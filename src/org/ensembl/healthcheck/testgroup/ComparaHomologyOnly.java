/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
 * Healthchecks for the homology Compara databases
 * (and only this kind of database)
 */
public class ComparaHomologyOnly extends GroupOfTests {
	
	public ComparaHomologyOnly() {

		addTest(
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
			org.ensembl.healthcheck.testcase.eg_compara.EGHighConfidence.class,
			org.ensembl.healthcheck.testcase.compara.MemberProductionCounts.class
		);
	}
}
