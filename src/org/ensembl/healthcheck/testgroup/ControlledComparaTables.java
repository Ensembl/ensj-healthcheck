/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

public class ControlledComparaTables extends GroupOfTests {
	
	public ControlledComparaTables() {
		
		addTest(
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
