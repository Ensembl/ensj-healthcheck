/*
 Copyright (C) 2003 EBI, GRL
 
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
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all DNA and protein align features have an external_db_id set.
 */

public class AlignFeatureExternalDB extends SingleDatabaseTestCase {

	/**
	 * Create a new AlignFeatureExteralDB testcase.
	 */
	public AlignFeatureExternalDB() {

		addToGroup("post_genebuild");
		addToGroup("post-compara-handover");
                addToGroup("pre-compara-handover");
		
		setDescription("Check that all DNA and protein align features have an external_db_id set.");
		setPriority(Priority.AMBER);
		setEffect("Needed for web display.");
		setFix("Run ensembl-personal/genebuilders/scripts/assign_external_db_ids.pl to set values.");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] tables = { "protein_align_feature", "dna_align_feature" };

		for (int i = 0; i < tables.length; i++) {

			result &= checkNoNulls(con, tables[i], "external_db_id");
			result &= checkNoZeroes(con, tables[i], "external_db_id");

		}

		return result;

	} // run

}
