/*
  Copyright (C) 2004 EBI, GRL
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.healthcheck.testcase.compara;

import java.sql.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class ForeignKeyTaxonId extends SingleDatabaseTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public ForeignKeyTaxonId() {

		addToGroup("compara_db_constraints");
		setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");

	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		int orphans = 0;

		Connection con = dbre.getConnection();

		if (tableHasRows(con, "taxon")) {
			orphans = countOrphans(con, "member", "taxon_id", "taxon", "taxon_id", true);
			if (orphans == 0) {
				ReportManager.correct(this, con, "taxon <- member relationships PASSED");
			} else if (orphans > 0) {
				ReportManager.problem(this, con, "taxon <- member has unlinked entries FAILED");
			} else {
				ReportManager.problem(this, con, "taxon <- member TEST NOT COMPLETED, look at the StackTrace if any");
			}

			orphans = countOrphans(con, "genome_db", "taxon_id", "taxon", "taxon_id", true);
			if (orphans == 0) {
				ReportManager.correct(this, con, "taxon <- genome_db relationships PASSED");
			} else if (orphans > 0) {
				ReportManager.problem(this, con, "taxon <- genome_db has unlinked entries FAILED");
			} else {
				ReportManager.problem(this, con, "taxon <- genome_db TEST NOT COMPLETED, look at the StackTrace if any");
			}
		} else {
			ReportManager.correct(this, con, "NO ENTRIES in taxon table, so nothing to test IGNORED");
		}

		result &= (orphans == 0);

		return result;

	}

} // ForeignKeyTaxonId
