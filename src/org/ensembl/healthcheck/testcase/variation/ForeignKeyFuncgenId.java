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


package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.MultiDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships between funcgen and variation database.
 */

public class ForeignKeyFuncgenId extends MultiDatabaseTestCase {

	/**
	 * Create a ForeignKeyFuncgenId that applies to a specific set of databases.
	 */
	public ForeignKeyFuncgenId() {
		setDescription("Check for broken foreign-key relationships between variation and funcgen databases.");
		setHintLongRunning(false);
		setTeamResponsible(Team.VARIATION);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *            The databases to check, in order funcgen->variation
	 * @return true if regulatory_feature stable ids in funcgen and variation are the same.
	 * 
	 */
	public boolean run(DatabaseRegistry dbr) {

		boolean overallResult = true;

		DatabaseRegistryEntry[] variationDBs = dbr
				.getAll(DatabaseType.VARIATION);

		// the database registry parameter dbr only contains the databases
		// matching the regular expression passed on the command line
		// so create a database registry containing all the regulation databases and
		// find the one we want
		List<String> funcgenRegexps = new ArrayList<String>();
		funcgenRegexps.add(".*_funcgen_.*");

		DatabaseRegistry allDBR = new DatabaseRegistry(funcgenRegexps, null, null, false);

		for (int i = 0; i < variationDBs.length; i++) {
			boolean result = true;
			DatabaseRegistryEntry dbvar = variationDBs[i];
			Connection con = dbvar.getConnection();
			String variationName = dbvar.getName();
			if(! variationName.matches("master.*")){
			try {
        // Only for human and mouse
        if (dbvar.getSpecies() == Species.HOMO_SAPIENS || dbvar.getSpecies() == Species.MUS_MUSCULUS) { 
          String funcgenName = variationName.replaceAll("variation", "funcgen");

          DatabaseRegistryEntry dbrfuncgen = allDBR.getByExactName(funcgenName);
          if (dbrfuncgen == null) {
            logger.severe("Incorrect funcgen database " + funcgenName + " for " + variationName);
            throw new Exception("Incorrect funcgen database " + funcgenName + " for " + variationName);
          }

          ReportManager.info(this, con, "Using " + dbrfuncgen.getName()
              + " as regulation database and " + dbvar.getName()
              + " as variation database");

          result &= checkForOrphans(con, dbvar.getName()
              + ".motif_feature_variation", "feature_stable_id",
              dbrfuncgen.getName() + ".regulatory_feature", "stable_id");

          result &= checkForOrphans(con, dbvar.getName()
              + ".regulatory_feature_variation", "feature_stable_id",
              dbrfuncgen.getName() + ".regulatory_feature", "stable_id");
          if (result) {
            // if there were no problems, just inform for the interface
            // to pick the HC
            ReportManager.correct(this, con, "ForeignKeyFuncgenId test passed without any problem");
          }
        } else {
          ReportManager.correct(this, con, "ForeignKeyFuncgenId test not run for this species.");
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

	/**
	 * This only applies to variation databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VEGA);

	}

}
