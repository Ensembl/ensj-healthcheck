package org.ensembl.healthcheck.testcase.eg_compara;

/**
 * Performs checks between the member table and the gene tree member table
 * ensuring all applicable entities are linked
 * 
 * @author ayates
 */
public class EGForeignKeyGeneTreeMemberId extends AbstractEGForeignKeyMemberId {
	
	@Override
	protected String getTargetTable() {
		return "gene_tree_member";
	}
}
