/*
 * Copyright (C) 2006 WTSI & EBI
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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that HGNC_curated_genes xrefs are on genes, _transcript are on transcript etc.
 */

public class HGNCTypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of HGNCTypes.
	 */
	public HGNCTypes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");
		
		setDescription("Check that HGNC_curated_genes xrefs are on genes, _transcript are on transcript etc");
		setTeamResponsible(Team.CORE);

	}

	/**
	 * This test only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// note these are looking for the *wrong* assignments
		result &= checkType(con, "HGNC_curated_gene", "Transcript");
		result &= checkType(con, "HGNC_automatic_gene", "Transcript");
		result &= checkType(con, "HGNC_curated_transcript", "Gene");
		result &= checkType(con, "HGNC_curated_transcript", "Gene");

		return result;

	}

	// ----------------------------------------------------------------------

	private boolean checkType(Connection con, String source, String wrongObject) {

		boolean result = true;

		int rows = getRowCount(con, "SELECT COUNT(*) FROM xref x, external_db e, object_xref ox WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND e.db_name='" + source
				+ "' AND ox.ensembl_object_type='" + wrongObject + "'");

		if (rows > 0) {

			ReportManager.problem(this, con, rows + " " + source + " xrefs are assigned to " + wrongObject.toLowerCase() + "s");
			result = false;

		} else {

			ReportManager.correct(this, con, "All " + source + " xrefs assigned to correct object type");
		}

		return result;

	}

	// ----------------------------------------------------------------------

}
