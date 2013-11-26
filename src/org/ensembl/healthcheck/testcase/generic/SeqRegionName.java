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

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils;


/**
 * Check that the seq_region names are in the right format. Only checks human and mouse.
 */

public class SeqRegionName extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionName testcase.
	 */
	public SeqRegionName() {

		addToGroup("post_genebuild");
		addToGroup("compara-ancestral");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that seq_region names for human and mouse are in the right format.");
		setTeamResponsible(Team.GENEBUILD);
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

		Species s = dbre.getSpecies();
                Connection con = dbre.getConnection();
                String AssemblyAccession = DBUtils.getMetaValue(con, "assembly.accession");

                if (AssemblyAccession.contains("GCA")) {

			result &= seqRegionNameCheck(con, "clone", "^[a-zA-Z]+[0-9]+\\.[0-9]+$");
			result &= seqRegionNameCheck(con, "contig", "^[a-zA-Z]*[0-9]*(\\\\.[0-9]+)+(\\.[0-9+])*$");
                        result &= seqRegionNameCheck(con, "scaffold", "^[a-zA-Z]*[0-9]*(\\.[0-9]+)+(\\.[0-9]+)*$");

                }

		if (s.equals(Species.ANCESTRAL_SEQUENCES)) {

			result &= seqRegionNameCheck(con, "ancestralsegment", "Ancestor_[0-9]+_[0-9]+$");

		}

		return result;

	} // run

	// ----------------------------------------------------------------------
	/**
	 * Check that seq regions of a particular coordinate system are named appropriately.
	 * 
	 * @return True if all seq_region names match the regexp.
	 */

	private boolean seqRegionNameCheck(Connection con, String coordinateSystem, String regexp) {

		boolean result = true;

		int rows = DBUtils.getRowCount(con, String.format(
				"SELECT COUNT(*) FROM seq_region sr, coord_system cs WHERE sr.coord_system_id=cs.coord_system_id AND cs.name='%s' AND sr.name NOT LIKE 'LRG%%' AND sr.name NOT LIKE 'MT' AND sr.name NOT REGEXP '%s' ", coordinateSystem,
				regexp));

		if (rows > 0) {

			ReportManager.problem(this, con, String.format("%d seq_regions in coordinate system %s have names that are not of the correct format", rows, coordinateSystem));
			result = false;

		} else {

			ReportManager.correct(this, con, String.format("All seq_regions in coordinate system %s have names in the correct format", coordinateSystem));

		}

		return result;

	}

	// ----------------------------------------------------------------------

} // SeqRegionName
