/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;

/**
 * Check that unrpoejcted GO xrefs exist, and that there are no blank or null
 * linkage types.
 */

public class GOXrefs extends SingleDatabaseTestCase {

	/**
	 * Create a new GOXrefs testcase.
	 */
	public GOXrefs() {

		setDescription("Check that unrpoejcted GO xrefs exist, and that there are no blank or null linkage types.");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only really applies to core databases
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
	 * @return true if the test passed.
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
			String sql = "SELECT COUNT(*) FROM external_db edb, xref x WHERE edb.db_name like 'go%' AND edb.external_db_id = x.external_db_id AND (x.info_type IS NULL OR x.info_type != 'PROJECTION')";

			int xref_rows = DBUtils.getRowCount(con, sql);
			if (xref_rows == 0) {

				ReportManager.problem(this, con,
						"No unprojected GO xrefs found.");
				result = false;

			} else {

				ReportManager.correct(this, con, "Found " + xref_rows
						+ " unprojected GO xrefs");

				// if GO xrefs exist, check that the ontology_xref table is
				// populated
				int ontology_xref_rows = DBUtils.getRowCount(con,
						"SELECT COUNT(*) FROM ontology_xref");
				if (ontology_xref_rows == 0) {

					ReportManager
							.problem(
									this,
									con,
									"Found "
											+ xref_rows
											+ " GO xrefs in xref table but ontology_xref table is empty");
					result = false;

				} else {

					ReportManager.correct(this, con, "ontology_xref table has "
							+ ontology_xref_rows + " rows");

				}
			}

		}

		// check for blank or null linkage_type
		int blank = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(*) FROM ontology_xref WHERE linkage_type IS NULL OR linkage_type=''");
		if (blank > 0) {

			ReportManager
					.problem(
							this,
							con,
							blank
									+ " rows in ontology_xref have null or blank ('') linkage_type");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"No blank or null linkage_types in ontology_xref");
		}

		// check that linkage_type values are one of the allowable values
		String[] allowable_linkage_types = { "IC", "IBA", "IDA", "IEA", "IEP",
				"IGI", "IMP", "IPI", "ISS", "NAS", "ND", "TAS", "NR", "RCA",
				"EXP", "ISO", "ISA", "ISM", "IGC" };

		String[] linkage_types = DBUtils
				.getColumnValues(
						con,
						"SELECT DISTINCT(linkage_type) FROM ontology_xref WHERE linkage_type != '' AND linkage_type NOT IN ('"
								+ Utils.arrayToString(allowable_linkage_types,
										"','") + "')");
		if (linkage_types.length > 0) {

			ReportManager
					.problem(
							this,
							con,
							"Linkage type(s): "
									+ Utils.arrayToString(linkage_types, ", ")
									+ " incorrect. Allowable values are: "
									+ Utils.arrayToString(
											allowable_linkage_types, ", "));
			result = false;

		} else {

			ReportManager.correct(this, con,
					"Linkage type values in ontology_xref are correct");

		}

		// check that *only* GO xrefs have linkage types assigned
		String[] dbs = DBUtils
				.getColumnValues(
						con,
						"SELECT DISTINCT(e.db_name) FROM external_db e, xref x, object_xref ox, ontology_xref g WHERE e.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id AND ox.object_xref_id=g.object_xref_id AND e.db_name not like 'GO%' ");
		if (dbs.length > 0) {

			ReportManager
					.problem(
							this,
							con,
							"Some "
									+ Utils.arrayToString(dbs, ", ")
									+ " xrefs have entries in linkage_type - should only be GO xrefs");
			result = false;

		} else {

			ReportManager.correct(this, con,
					"No non-GO xrefs have linkage types assigned");

		}

		return result;

	} // run
} // GOXrefs
