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

public class ControlledTables extends GroupOfTests {
	
	public ControlledTables() {
		
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
