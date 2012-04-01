/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the number of entries in the seq_region_attrib table specifying
 * that the MT chromosome should use codon table 2 matches the number of MT
 * chromosomes (may be several different assemblies)
 */
public class MTCodonTable extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of MTCodonTable
	 */
	public MTCodonTable() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");

		setDescription("Check that the number of entries in the seq_region_attrib table specifying that the MT chromosome should use codon table 2 matches the number of MT chromosomes (may be several different assemblies)");
		setPriority(Priority.AMBER);
		setFix("Add seq_region_attribs");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * Run the test.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		int numMTs = DBUtils
				.getRowCount(con,
						"SELECT COUNT(DISTINCT(seq_region_id)) FROM seq_region WHERE name = 'MT'");

		int numAttribs = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(DISTINCT(sra.seq_region_id)) FROM seq_region sr, seq_region_attrib sra, attrib_type att WHERE sr.seq_region_id=sra.seq_region_id AND sra.attrib_type_id=att.attrib_type_id AND att.code = 'codon_table' AND sra.value = '2' AND sr.name = 'MT'");

		if (numMTs != numAttribs) {

			ReportManager
					.problem(
							this,
							con,
							"There are "
									+ numMTs
									+ " seq_region entries named 'MT' but "
									+ numAttribs
									+ " seq_region_attribs specifying that they should use codon table 2");
			result = false;
		}

		return result;

	} // run

	// -----------------------------------------------------------------------

} // MTCodonTable

