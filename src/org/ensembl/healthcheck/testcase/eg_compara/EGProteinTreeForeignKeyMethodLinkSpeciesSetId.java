package org.ensembl.healthcheck.testcase.eg_compara;

import org.ensembl.healthcheck.DatabaseRegistryEntry;

/**
 * Inherits from the top level MLSS checks but only runs checks
 * for protein trees to be used when running post pipeline checks.
 */
public class EGProteinTreeForeignKeyMethodLinkSpeciesSetId extends
	EGForeignKeyMethodLinkSpeciesSetId {

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		return assertProteinTreeMemberOrphans(dbre);
	}
}
