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

public class ForeignKeyDnafragId extends EnsTestCase {

	/**
	 * Create an OrphanTestCase that applies to a specific set of databases.
	 */
	public ForeignKeyDnafragId() {

		addToGroup("compara_db_constraints");
		setDescription("Check for broken foreign-key relationships in ensembl_compara databases.");
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		int orphans = 0;

		Connection con = dbre.getConnection();
		// 4 tests to check dnafrag_id used as foreign key

		if (tableHasRows(con, "dnafrag")) {
			orphans = countOrphans(con, "dnafrag_region", "dnafrag_id", "dnafrag", "dnafrag_id", true);
			if (orphans == 0) {
				ReportManager.correct(this, con, "dnafrag_region -> dnafrag relationships PASSED");
			} else if (orphans > 0) {
				ReportManager.problem(this, con, "dnafrag_region has unlinked entries in dnafrag FAILED");
			} else {
				ReportManager.problem(this, con, "dnafrag_region -> dnafrag TEST NOT COMPLETED, look at the StackTrace if any");
			}

			orphans = countOrphans(con, "genomic_align_block", "consensus_dnafrag_id", "dnafrag", "dnafrag_id", true);
			if (orphans == 0) {
				ReportManager.correct(this, con, "consensus_dnafrag_id in genomic_align_block -> dnafrag relationships PASSED");
			} else if (orphans > 0) {
				ReportManager.problem(this, con, "consensus_dnafrag_id in genomic_align_block has unlinked entries in dnafrag FAILED");
			} else {
				ReportManager.problem(this, con, "genomic_align_block -> dnafrag relationships TEST NOT COMPLETED, look at the StackTrace if any");
			}

			orphans = countOrphans(con, "genomic_align_block", "query_dnafrag_id", "dnafrag", "dnafrag_id", true);
			if (orphans == 0) {
				ReportManager.correct(this, con, "query_dnafrag_id in genomic_align_block -> dnafrag relationships PASSED");
			} else if (orphans > 0) {
				ReportManager.problem(this, con, "query_dnafrag_id in genomic_align_block has unlinked entries in dnafrag FAILED");
			} else {
				ReportManager.problem(this, con, "genomic_align_block -> dnafragTEST NOT COMPLETED, look at the StackTrace if any");
			}
		} else {
			ReportManager.correct(this, con, "NO ENTRIES in dnafrag table, so nothing to test IGNORED");
		}

		result &= (orphans == 0);

		return result;

	}

} // OrphanTestCase
