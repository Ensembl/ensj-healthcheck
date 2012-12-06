package org.ensembl.healthcheck.testcase.eg_compara;

public class ControlledTableNcbiTaxaNode extends AbstractControlledTable {
	
	@Override protected String getControlledTableName() {
		return "ncbi_taxa_node";	
	}
	
	@Override protected ComparisonStrategy getComparisonStrategy() {
		return ComparisonStrategy.Checksum;
	}

}
