/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.compara.ForeignKeyDnafragId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomeDbId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignBlockId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyGenomicAlignId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyMethodLinkId;
import org.ensembl.healthcheck.testcase.compara.ForeignKeyTaxonId;
import org.ensembl.healthcheck.testcase.compara.MultipleGenomicAlignBlockIds;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckEmptyLocators;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckNoTreeStableIds;
import org.ensembl.healthcheck.testcase.eg_compara.EGCheckSynteny;
import org.ensembl.healthcheck.testcase.eg_compara.EGForeignKeyMethodLinkSpeciesSetId;
import org.ensembl.healthcheck.testcase.eg_compara.MemberXrefAssociation;
import org.ensembl.healthcheck.testcase.eg_compara.MemberProductionCounts;

/**
 * Group of tests for EnsemblGenomes compara databases
 * 
 * @author dstaines
 * 
 */
public class EGCompara extends GroupOfTests {

	public EGCompara() {

		setDescription("Group of tests for EnsemblGenomes compara databases.");

		addTest(
				EGCommon.class, 
				EGComparaGeneTree.class, 
				EGCheckSynteny.class,
				EGForeignKeyMethodLinkSpeciesSetId.class,
				EGCheckNoTreeStableIds.class,
				ForeignKeyDnafragId.class, 
				ForeignKeyGenomeDbId.class,
				ForeignKeyGenomicAlignBlockId.class,
				ForeignKeyGenomicAlignId.class, 
				ForeignKeyMethodLinkId.class,
				ForeignKeyTaxonId.class, 
				EGCheckEmptyLocators.class,
        MemberXrefAssociation.class,
        MemberProductionCounts.class,
				MultipleGenomicAlignBlockIds.class,
				ControlledComparaTables.class
		);
	}
}
