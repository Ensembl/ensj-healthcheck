/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships between core and variation database.
 */

public class ForeignKeyCoreId extends MultiDatabaseTestCase {

	/**
	 * Create an ForeignKeyCoreId that applies to a specific set of databases.
	 */
	public ForeignKeyCoreId() {

		addToGroup("variation-release");
		setDescription("Check for broken foreign-key relationships between variation and core databases.");
		setHintLongRunning(true);
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *            The databases to check, in order core->variation
	 * @return true if same transcripts and seq_regions in core and variation are
	 *         the same.
	 * 
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean overallResult = true;

		DatabaseRegistryEntry[] variationDBs = dbr.getAll(DatabaseType.VARIATION);

		// the database registry parameter dbr only contains the databases
		// matching the regular expression passed on the command line
		// so create a database registry containing all the core databases and
		// find the one we want
		List<String> coreRegexps = new ArrayList<String>();
		coreRegexps.add(".*_core_.*");

		DatabaseRegistry allDBR = new DatabaseRegistry(coreRegexps, null, null, false);

		for (int i = 0; i < variationDBs.length; i++) {

			boolean result = true;
			DatabaseRegistryEntry dbrvar = variationDBs[i];
			Connection con = dbrvar.getConnection();

			String variationName = dbrvar.getName();
			if (!variationName.matches("master.*")) {

				try {

					String coreName = variationName.replaceAll("variation", "core");

					DatabaseRegistryEntry dbrcore = allDBR.getByExactName(coreName);
					if (dbrcore == null) {
						logger.severe("Incorrect core database " + coreName + " for " + variationName);
						throw new Exception("Incorrect core database " + coreName + " for " + variationName);
					}

					ReportManager.info(this, con, "Using " + dbrcore.getName() + " as core database and "
							+ dbrvar.getName() + " as variation database");

					result &= checkForOrphans(con, dbrvar.getName() + ".transcript_variation", "feature_stable_id",
							dbrcore.getName() + ".transcript", "stable_id");

					result &= checkForOrphansWithConstraint(con, dbrvar.getName() + ".variation_feature",
							"seq_region_id", dbrcore.getName() + ".seq_region", "seq_region_id",
							"seq_region_id IS NOT NULL");

					result &= checkForOrphansWithConstraint(con, dbrvar.getName() + ".structural_variation_feature",
							"seq_region_id", dbrcore.getName() + ".seq_region", "seq_region_id",
							"seq_region_id IS NOT NULL");

					int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM " + dbrvar.getName()
							+ ".seq_region srv join " + dbrvar.getName()
							+ ".coord_system csv on (srv.coord_system_id=csv.coord_system_id) join " + dbrcore.getName()
							+ ".seq_region src on (src.name=srv.name) join " + dbrcore.getName()
							+ ".coord_system cs on (cs.coord_system_id=src.coord_system_id) "
							+ "WHERE csv.attrib = 'default_version' AND cs.attrib='default_version' AND src.seq_region_id != srv.seq_region_id");

					if (rows > 0) {
						ReportManager.problem(this, con, rows
								+ " rows seq_region in core has same name, but different seq_region_id comparing with seq_region in variation database");
						result = false;
					}

					if (result) {
						// if there were no problems, just inform for the interface
						// to pick the HC
						ReportManager.correct(this, con, "ForeignKeyCoreId test passed without any problem");
					}

				} catch (Exception e) {
					ReportManager.problem(this, con, "HealthCheck generated an exception: " + e.getMessage());
					result = false;
				}

				overallResult &= result;
			}
		}
		return overallResult;

	}

} // ForeignKeyCoreId
