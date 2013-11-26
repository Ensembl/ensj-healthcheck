/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableDnafrag;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableGenomeDb;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMappingSession;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMethodLink;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMethodLinkSpeciesSet;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableMethodLinkSpeciesSetTag;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableNcbiTaxaName;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableNcbiTaxaNode;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableSpeciesSet;
import org.ensembl.healthcheck.testcase.eg_compara.ControlledTableSpeciesSetTag;

public class ControlledComparaTables extends GroupOfTests {
	
	public ControlledComparaTables() {
		
		addTest(
				ControlledTableDnafrag.class,
				ControlledTableGenomeDb.class,
				ControlledTableMappingSession.class,
				ControlledTableMethodLink.class,
				ControlledTableMethodLinkSpeciesSet.class,				
				ControlledTableMethodLinkSpeciesSetTag.class,
				ControlledTableNcbiTaxaName.class,
				ControlledTableNcbiTaxaNode.class,
				ControlledTableSpeciesSet.class,
				ControlledTableSpeciesSetTag.class				
		);		
	}
}
