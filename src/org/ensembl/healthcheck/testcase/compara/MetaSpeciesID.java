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

public class MetaSpeciesID extends AbstractRepairableComparaTestCase {

	String[] speciesless_meta_keys = { "schema_version", "schema_type", "patch", "division" };

	protected String getTableName() {
		return "meta";
	}
	protected String getAddQuery(String key, String value) {
		return ""; // Cannot happen
	}
	protected String getUpdateQuery(String key, String value) {
		return "UPDATE meta SET species_id = " + value + " WHERE meta_key = \"" + key + "\";";
	}
	protected String getRemoveQuery(String key) {
		return ""; // Cannot happen
	}

	/**
	 * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
	 */
	public MetaSpeciesID() {
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
		result &= checkSpeciesId(dbre);
		return result;
	}

	/**
	 * Check that the species_id is 1 for everything except schema_version which should be NULL
	 */
	private boolean checkSpeciesId(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// get version from meta table
		Connection con = dbre.getConnection();

		String sql = "SELECT species_id, meta_key FROM meta";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (Utils.stringInArray(rs.getString(2), speciesless_meta_keys, true)) { // Is it one of meta_keys that expect species_id=NULL ?
					if (rs.getInt(1) != 0) {
						// set species_id of schema_version to NULL
						EntriesToUpdate.put(rs.getString(2), "NULL");
					}
				} else {    // the rest of meta_keys expect species_id=1 in Compara schema
					if (rs.getInt(1) != 1) {
						// set species_id of everything else to 1
						EntriesToUpdate.put(rs.getString(2), "1");
					}
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}
		return result;
	}

} // MetaSpeciesID
