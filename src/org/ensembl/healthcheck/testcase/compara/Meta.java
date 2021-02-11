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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.Utils; // needed for stringInArray

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class Meta extends AbstractRepairableComparaTestCase {


	protected String getTableName() {
		return "meta";
	}
	protected String getAddQuery(String key, String value) {
		return "INSERT INTO meta VALUES (NULL, 1, \"" + key + "\", " + value + ");";
	}
	protected String getUpdateQuery(String key, String value) {
		return "UPDATE meta SET meta_value = " + value + " WHERE meta_key = \"" + key + "\";";
	}
	protected String getRemoveQuery(String key) {
		return "DELETE FROM meta WHERE meta_key = \"" + key + "\";";
	}

	/**
	 * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
	 */
	public Meta() {
		setDescription("Check meta table for the right schema version and species_id");
		setTeamResponsible(Team.COMPARA);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean runTest(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "meta")) {
			result = false;
			ReportManager.problem(this, con, "Meta table not present");
			return result;
		}

		// These methods return false if there is any problem with the test
		if (!isMasterDB(con)) {
			result &= checkSchemaVersionDBName(dbre);
		}

		return result;
	}


	/**
	 * Check that the schema_version in the meta table is present and matches the database name.
	 */
	private boolean checkSchemaVersionDBName(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// get version from database name
		String dbNameVersion = dbre.getSchemaVersion();

		logger.finest("Schema version from database name: " + dbNameVersion);

		// get version from meta table
		Connection con = dbre.getConnection();

		// Get current global value from the meta table (used for backwards compatibility)
		String sql = "SELECT meta_key, meta_value" + " FROM meta WHERE meta_key = \"schema_version\"";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.first()) {
				if (rs.getInt(2) != new Integer(dbNameVersion).intValue()) {
					EntriesToUpdate.put("schema_version", dbNameVersion);
				}
			} else {
				EntriesToAdd.put("schema_version", dbNameVersion);
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		return result;

	} // ---------------------------------------------------------------------

} // Meta
