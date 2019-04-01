/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that no HGNC xrefs have dbprimary_acc=display_label; this is usually
 * caused by withdrawn symbols which we don't want to include.
 */

public class HGNCNumeric extends SingleDatabaseTestCase {

	double threshold = 0.01; // Fraction of numeric identifiers below which no
								// warning will be issued

	/**
	 * Create a new HGNCNumeric testcase.
	 */
	public HGNCNumeric() {

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
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String allSQL = "SELECT COUNT(*) FROM external_db e, xref x, object_xref ox, gene g WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.ensembl_object_type='Gene' AND ox.ensembl_id=g.gene_id AND e.db_name LIKE 'HGNC%'";
		String numericSQL = allSQL + " AND x.dbprimary_acc=x.display_label";

		int rowsAll = DBUtils.getRowCount(con, allSQL);

		if (rowsAll == 0) {
			return true; // avoid division by 0 later
		}

		int rowsNumeric = DBUtils.getRowCount(con, numericSQL);

		double fraction = (double) rowsNumeric / (double) rowsAll;

		if (fraction > threshold) {

			ReportManager
					.problem(
							this,
							con,
							rowsNumeric
									+ " ("
									+ (fraction * 100)
									+ "%) HGNC xrefs with dbprimary_acc=display_label; this will cause genes to have numeric display names, or break hyperlinks");
			result = false;

		} else {

			ReportManager.correct(this, con, "All HGNC xrefs (or more than "
					+ (threshold * 100)
					+ "%) have different dbprimary_acc and display_label");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // HGNCNumeric

