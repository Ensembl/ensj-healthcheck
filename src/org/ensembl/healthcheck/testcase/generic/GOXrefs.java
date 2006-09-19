/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that unrpoejcted GO xrefs exist, and that there are no blank or null linkage types.
 */

public class GOXrefs extends SingleDatabaseTestCase {

	/**
	 * Create a new GOXrefs testcase.
	 */
	public GOXrefs() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that unrpoejcted GO xrefs exist, and that there are no blank or null linkage types.");

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// only check for GO xrefs for human, mouse, rat & drosophila
		// if (dbre.getSpecies().equals(Species.HOMO_SAPIENS) ||
		// dbre.getSpecies().equals(Species.MUS_MUSCULUS) ||
		// dbre.getSpecies().equals(Species.RATTUS_NORVEGICUS) ||
		// dbre.getSpecies().equals(Species.DROSOPHILA_MELANOGASTER)) {

		Connection con = dbre.getConnection();

		if (true) {

			// check that they exist in the xref table
			String sql = "SELECT COUNT(*) FROM external_db edb, xref x WHERE edb.db_name= 'go' AND edb.external_db_id = x.external_db_id AND (x.info_type IS NULL OR x.info_type != 'PROJECTION')";

			int xref_rows = getRowCount(con, sql);
			if (xref_rows == 0) {

				ReportManager.problem(this, con, "No unprojected GO xrefs found.");
				result = false;

			} else {

				ReportManager.correct(this, con, "Found " + xref_rows + " unprojected GO xrefs");

				// if GO xrefs exist, check that the go_xref table is populated
				int go_xref_rows = getRowCount(con, "SELECT COUNT(*) FROM go_xref");
				if (go_xref_rows == 0) {

					ReportManager.problem(this, con, "Found " + xref_rows + " GO xrefs in xref table but go_xref table is empty");
					result = false;

				} else {

					ReportManager.correct(this, con, "go_xref table has " + go_xref_rows + " rows");

				}
			}

		} else {

			logger.info("Not checking for GO xrefs in " + dbre.getSpecies());
			return true;

		}

		// check for blank or null linkage_type
		int blank = getRowCount(con, "SELECT COUNT(*) FROM go_xref WHERE linkage_type IS NULL OR linkage_type=''");
		if (blank > 0) {

			ReportManager.problem(this, con, blank + " rows in go_xref have null or blank ('') linkage_type");
			result = false;

		} else {

			ReportManager.correct(this, con, "No blank or null linkage_types in go_xref");
		}

		// check that *only* GO xrefs have linkage types assigned
		String[] dbs = getColumnValues(con, "SELECT DISTINCT(e.db_name) FROM external_db e, xref x, object_xref ox, go_xref g WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.object_xref_id=g.object_xref_id AND e.db_name != 'GO' ");
		if (dbs.length > 0) {

			ReportManager.problem(this, con, "Some " + Utils.arrayToString(dbs, ", ") + " xrefs have entries in linkage_type - should only be GO xrefs");
			result = false;

		} else {

			ReportManager.correct(this, con, "No non-GO xrefs have linkage types assigned");
			
		}
		
		return result;

	} // run
} // GOXrefs
