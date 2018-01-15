/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check for certain combinations of logic name and transcript name.
 */

public class TranscriptNames extends SingleDatabaseTestCase {

	/**
	 * Constructor
	 */
	public TranscriptNames() {

		setDescription("Check for certain combinations of logic name and transcript name.");
		setPriority(Priority.AMBER);
		setEffect("Transcript names do not match the logic names.");
		setTeamResponsible(Team.CORE);
                setSecondTeamResponsible(Team.GENEBUILD);
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

		// only valid in human, mouse and zebrafish
                Species species = dbre.getSpecies();
                boolean is_merged = isMerged(species);
		if (!is_merged) {
			return true;
		}

		// Hash of logic_names to the patterns that transcript names should match
		Map<String, String> logicNameRegexp = new HashMap<String, String>();
		logicNameRegexp.put("ensembl", "-2[0-9][0-9]$");
		logicNameRegexp.put("ensembl_havana_transcript", "-0[0-9][0-9]$");
		logicNameRegexp.put("havana", "-0[0-9][0-9]$");
		//logicNameRegexp.put("ncrna", "-2[0-9][0-9]$");
		logicNameRegexp.put("ensembl_ig_gene", "-2[0-9][0-9]$");
		logicNameRegexp.put("havana_ig_gene", "-0[0-9][0-9]$");
		logicNameRegexp.put("ncrna_pseudogene", "-2[0-9][0-9]$");
		logicNameRegexp.put("mt_genbank_import", "-2[0-9][0-9]$");
		logicNameRegexp.put("lrg_import", "LRG_[0-9]+t[0-9]+$");
		logicNameRegexp.put("ensembl_lincrna", "-2[0-9][0-9]$");

		Connection con = dbre.getConnection();

		try {

			PreparedStatement stmt = con
					.prepareStatement("SELECT COUNT(*) FROM analysis a, transcript t, xref x WHERE a.analysis_id=t.analysis_id AND t.display_xref_id=x.xref_id AND a.logic_name=? AND x.display_label NOT REGEXP ?");

			for (String logicName : logicNameRegexp.keySet()) {

				String regexp = logicNameRegexp.get(logicName);
				stmt.setString(1, logicName);
				stmt.setString(2, regexp);

				ResultSet rs = stmt.executeQuery();

				rs.first();
				int rows = rs.getInt(1);

				if (rows > 0) {

					result = false;
					ReportManager.problem(this, con, String.format("%d transcripts with logic name %s have names which don't match the required pattern (%s)", rows, logicName, regexp));

				} else {

					ReportManager.correct(this, con, String.format("All transcripts with logic name %s have correct names", logicName));

				}

				rs.close();

			}

		} catch (SQLException se) {
			se.printStackTrace();
		}

		return result;

	}

} // TranscriptNames
