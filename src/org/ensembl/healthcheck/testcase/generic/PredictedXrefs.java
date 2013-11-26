/*
 * Copyright [1999-2013] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import java.util.HashMap;
import java.util.Map.Entry;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for any xrefs that are listed as "KNOWN" in the external_db table but
 * are actually predictions. Currently only RefSeq XP/XM xrefs.
 */

public class PredictedXrefs extends SingleDatabaseTestCase {

	/**
	 * Create a new PredictedXrefs testcase.
	 */
	public PredictedXrefs() {

		addToGroup("post_genebuild");
		addToGroup("core_xrefs");
		addToGroup("post-compara-handover");

		setDescription("Check for predicted xrefs erroneously classed as KNOWN.");
		setTeamResponsible(Team.CORE);
		setSecondTeamResponsible(Team.GENEBUILD);
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

		// hash of external_db_name / xref accession regexps to look for.
		// Note patterns are MySQL patterns, not regexps
		// if any of these match it's an error
		HashMap<String,String> nameToAccessionPattern = new HashMap<String,String>();
		nameToAccessionPattern.put("RefSeq_mRNA", "XM%");
		nameToAccessionPattern.put("RefSeq_ncRNA", "XR%");
		nameToAccessionPattern.put("RefSeq_peptide", "XP%");

		Connection con = dbre.getConnection();

		for(Entry<String,String> entry: nameToAccessionPattern.entrySet()) {		  
			String externalDBName = entry.getKey();
			String pattern = entry.getValue();

			logger.fine("Checking for " + externalDBName + " xrefs matching "
					+ pattern);

			int rows = DBUtils
					.getRowCount(
							con,
							"SELECT COUNT(*) FROM xref x, external_db e WHERE x.external_db_id=e.external_db_id AND e.db_name='"
									+ externalDBName
									+ "' AND x.dbprimary_acc LIKE '"
									+ pattern
									+ "'");

			if (rows > 0) {

				ReportManager
						.problem(
								this,
								con,
								rows
										+ " "
										+ externalDBName
										+ " xrefs seem to be predictions (match "
										+ pattern
										+ ")\nUSEFUL SQL:SELECT COUNT(*) FROM xref x, external_db e WHERE x.external_db_id=e.external_db_id AND e.db_name='RefSeq_peptide'  AND x.dbprimary_acc LIKE 'XP%';");
				result = false;

			} else {

				ReportManager.correct(this, con, "No " + externalDBName
						+ " xrefs match " + pattern);

			}

		}

		return result;

	} // run

	// ----------------------------------------------------------------------

} // PredictedXrefs
