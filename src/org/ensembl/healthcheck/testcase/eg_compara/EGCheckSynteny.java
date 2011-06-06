package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.testcase.compara.CheckSynteny;

/**
 * Override of {@link CheckSynteny} which does not assume the lack of
 * rows in synteny_region is a failure.
 * 
 * @author ayates
 */
public class EGCheckSynteny extends CheckSynteny {

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		if (!tableHasRows(con, "synteny_region")) {
			return true;
		}
		return super.run(dbre);
	}
}
