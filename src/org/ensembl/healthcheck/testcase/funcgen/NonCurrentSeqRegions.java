package org.ensembl.healthcheck.testcase.funcgen;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author mnuhn
 * 
 * Flags seq regions that are not current and should be removed in the future.
 *
 */
public class NonCurrentSeqRegions extends SingleDatabaseTestCase {
	@Override
	public boolean run(DatabaseRegistryEntry dbre) {
		return checkCountIsZero(
			dbre.getConnection(), 
			"coord_system join seq_region using (coord_system_id)",
			"coord_system.is_current!=true"
		);
	}
}
