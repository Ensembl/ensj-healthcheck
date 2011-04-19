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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that no HGNC xrefs have dbprimary_acc=display_label; this is usually caused by withdrawn symbols which we don't want to
 * include.
 */

public class HGNCNumeric extends SingleDatabaseTestCase {

	double threshold = 0.01; // Fraction of numeric identifiers below which no warning will be issued

	/**
	 * Create a new HGNCNumeric testcase.
	 */
	public HGNCNumeric() {

		addToGroup("release");
		addToGroup("core_xrefs");
		setDescription("Check that no HGNC xrefs have dbprimary_acc=display_label");
		setPriority(Priority.AMBER);
		setFix("Remove HGNC xrefs and object_xrefs where dbprimary_acc=display_label. Set display_xref_ids of genes that were pointing to these to null.");
		setEffect("Causes genes to be displayed with numeric HGNC symbols, and some dbprimary_acc=display_label for HGNC when they're not supposed to be, which confuses Mart.");
		setTeamResponsible(Team.CORE);
	}

	/**
	 * Only run on core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
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

		String allSQL = "SELECT COUNT(*) FROM external_db e, xref x, object_xref ox, gene_stable_id gsi WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.ensembl_object_type='Gene' AND ox.ensembl_id=gsi.gene_id AND e.db_name LIKE 'HGNC%'";
		String numericSQL = allSQL + " AND x.dbprimary_acc=x.display_label";

		int rowsAll = getRowCount(con, allSQL);

		if (rowsAll == 0) {
			return true; // avoid division by 0 later
		}

		int rowsNumeric = getRowCount(con, numericSQL);

		double fraction = (double) rowsNumeric / (double) rowsAll;

		if (fraction > threshold) {

			ReportManager.problem(this, con, rowsNumeric + " (" + (fraction * 100)
					+ "%) HGNC xrefs with dbprimary_acc=display_label; this will cause genes to have numeric display names, or break hyperlinks");
			result = false;

		} else {

			ReportManager.correct(this, con, "All HGNC xrefs (or more than " + (threshold * 100) + "%) have different dbprimary_acc and display_label");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // HGNCNumeric

