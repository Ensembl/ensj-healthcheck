/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks the metadata table to make sure it is OK. Only one meta table at a
 * time is done here; checks for the consistency of the meta table across
 * species are done in MetaCrossSpecies.
 */
public class Meta extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of CheckMetaDataTableTestCase
     */
    public Meta() {

        setDescription("Check that the meta table contains the right entries for the human and mouse");
        setTeamResponsible(Team.VARIATION);

    }

    /**
     * Check various aspects of the meta table.
     *
     * @param dbre The database to check.
     * @return True if the test passed.
     */
    public boolean run(final DatabaseRegistryEntry dbre) {
        boolean result = true;

        Connection con = dbre.getConnection();
        String metaKey;

        result &= checkSchemaVersionDBName(dbre);

        // check the Meta table in Human: should contain the entry for the
        // pairwise_ld
		if (dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)) {
            // find out if there is an entry for the default LD Population
            metaKey = "pairwise_ld.default_population";

            result &= checkKeysPresent(con, metaKey);

			result &= checkForOrphansWithConstraint(con, "meta", "meta_value", "population", "population_id",
					"meta_key = '" + metaKey + "'");

			// check sift and polyphen
			metaKey = "sift_version";
			result &= checkKeysPresent(con, metaKey);

			metaKey = "polyphen_version";
			result &= checkKeysPresent(con, metaKey);
		}
		if (dbre.getSpecies().equals(DatabaseRegistryEntry.CANIS_FAMILIARIS)) {
            // find out if the entries in the Meta point to the strain
            // information
            String[] metaKeys = {"sample.default_strain"};
            for (int i = 0; i < metaKeys.length; i++) {
                metaKey = metaKeys[i];

                result &= checkKeysPresent(con, metaKey);
                if (metaKey == "sample.default_strain") {
                    result &= checkForOrphansWithConstraint(con, "meta", "meta_value", "sample",
                            "name COLLATE latin1_general_cs", "meta_key = '" + metaKey + "'");
                }
            }
        }

        // Check that the required meta keys exist
        String[] metaKeys = new String[]{"schema_version", "schema_type", "species.production_name"};
        for (int i = 0; i < metaKeys.length; i++) {
            if (!checkKeysPresent(con, metaKeys[i])) {
                result = false;
				ReportManager.problem(this, con, "Missing required meta_key '" + metaKeys[i] + "'");
            }
        }

        // List the keys that affects the schema
        metaKeys = new String[]{"schema_version", "schema_type", "patch"};
        // Check that the species_id column is NULL for meta entries that
        // concerns the schema
        for (int i = 0; i < metaKeys.length; i++) {
            String sql = "SELECT meta_id FROM meta WHERE meta_key = '" + metaKeys[i] + "' AND species_id IS NOT NULL";
            String[] violations = DBUtils.getColumnValues(con, sql);
            for (int j = 0; j < violations.length; j++) {
                result = false;
				ReportManager.problem(this, con, "Meta entry for meta_key '" + metaKeys[i] + "' with meta_id = "
						+ violations[j] + " has species_id not set to NULL");
            }
        }

		// Check that species specific meta keys (e.g. sift_version,
		// sift_protein_db_version, HGVS_version) are set to 1
		String sql = "SELECT meta_id, meta_key FROM meta WHERE meta_key NOT IN ('schema_version', 'schema_type', 'patch') AND (species_id != 1 OR species_id IS NULL)";
		List<String[]> data = DBUtils.getRowValuesList(con, sql);
		for (String[] line : data) {
			result = false;
			ReportManager.problem(this, con, "Meta entry for species specific meta_key " + line[1] + " (meta_id="
					+ line[0] + ") is not set to 1");
		}

        if (result) {
            // if there were no problems, just inform for the interface to pick
            // the HC
            ReportManager.correct(this, con, "Meta test passed without any problem");
        }
        return result;
    } // run

    // --------------------------------------------------------------

    private boolean checkKeysPresent(Connection con, String metaKey) {

        boolean result = true;

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key='" + metaKey + "'");
        if (rows == 0) {
            result = false;
			ReportManager.problem(this, con, "No entry in meta table for " + metaKey);
        } else {
            ReportManager.correct(this, con, metaKey + " entry present");
        }

        return result;
    }

    // ---------------------------------------------------------------------

    /**
	 * Check that the schema_version in the meta table is present and matches the
	 * database name.
     */
    private boolean checkSchemaVersionDBName(DatabaseRegistryEntry dbre) {

        boolean result = true;

        // get version from database name
        String dbNameVersion = dbre.getSchemaVersion();
        dbNameVersion = dbNameVersion.replaceAll("_[0-9]+", "");
        logger.finest("Schema version from database name: " + dbNameVersion);

        // get version from meta table
        Connection con = dbre.getConnection();

        if (dbNameVersion == null) {
			ReportManager.warning(this, con, "Can't deduce schema version from database name.");
            return false;
        }

        String schemaVersion = DBUtils.getRowColumnValue(con,
                "SELECT meta_value FROM meta WHERE meta_key='schema_version'");
        logger.finest("schema_version from meta table: " + schemaVersion);

        int dbNameVersionInt;
        int schemaVersionInt;
        try {
            dbNameVersionInt = Integer.parseInt(dbNameVersion);
            schemaVersionInt = Integer.parseInt(schemaVersion);
        } catch (NumberFormatException e) {
            dbNameVersionInt = 0;
            schemaVersionInt = 0;
        }
        int schemaDiff = schemaVersionInt - dbNameVersionInt;
        boolean diffOk = (schemaVersionInt != 0 && dbNameVersionInt != 0) && (schemaDiff == 0 || schemaDiff == 53);

        if (schemaVersion == null || schemaVersion.length() == 0) {

			ReportManager.problem(this, con, "No schema_version entry in meta table");
            return false;

        } else if (!schemaVersion.matches("[0-9]+")) {

			ReportManager.problem(this, con, "Meta schema_version " + schemaVersion + " is not numeric");
            return false;

        } else if (! diffOk ) {

			ReportManager.problem(this, con, "Meta schema_version " + schemaVersion
					+ " does not match version inferred from database name (" + dbNameVersion + ")");
            return false;

        } else {

			ReportManager.correct(this, con,
					"schema_version " + schemaVersion + " matches database name version " + dbNameVersion);

        }
        return result;

    }
    // ---------------------------------------------------------
}
