package org.ensembl.healthcheck.testcase.eg_compara;

/**
 * Performs checks between the member table and the protein tree member table
 * ensuring all applicable entities are linked
 * 
 * @author ayates
 */
public class EGForeignKeyProteinTreeMemberId extends AbstractEGForeignKeyMemberId {
	
	@Override
	protected String getTargetTable() {
		return "protein_tree_member";
	}
}
