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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for HGNCs that have been assigned as display labels more than one gene.
 */

public class HGNCMultipleGenes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of HGNCMultipleGenes.
	 */
	public HGNCMultipleGenes() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");

		setDescription("Check for HGNCs that have been assigned as display labels more than one gene.");
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
	 *            The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// this has to be done the slow way, don't think there's a way to do
		// this all at once
		String sql = "SELECT DISTINCT(x.display_label), COUNT(*) AS count FROM gene g, xref x, external_db e WHERE e.external_db_id=x.external_db_id AND e.db_name LIKE 'HGNC%' AND x.xref_id=g.display_xref_id ";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega do
															// not consider
															// duplicates for
															// the haplotypes
			sql += "and g.seq_region_id NOT in(select seq_region_id from seq_region_attrib sa join attrib_type at on sa.attrib_type_id=at.attrib_type_id where code ='vega_ref_chrom') and (g.source='havana' or g.source='WU') ";
		}
		sql += " GROUP BY x.display_label";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega only
															// count the ones
															// for which the
															// source is the
															// same
			sql += ", g.source ";
		}
		sql += " HAVING COUNT > 1";

		int rows = DBUtils.getRowCount(con, sql);

		if (rows > 0) {

			ReportManager.problem(this, con, rows
					+ " HGNC symbols have been assigned to more than one gene");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"All HGNC symbols only assigned to one gene");
		}

		return result;

	}

	// ----------------------------------------------------------------------

}
