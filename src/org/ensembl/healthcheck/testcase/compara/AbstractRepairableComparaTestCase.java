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


package org.ensembl.healthcheck.testcase.compara;

import java.util.Map;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/*
Abstract class that provides a "Repair" interface to any table
Subclasses must implement the abstract getters getTableName(), getAddQuery(),
getUpdateQuery() and getRemoveQuery(), and the runTest() method, which is
called by run() to do the tests
*/
public abstract class AbstractRepairableComparaTestCase extends AbstractComparaTestCase implements Repair {

	protected abstract String getTableName();

	protected HashMap<String,String> EntriesToAdd = new HashMap<String,String>();
	protected HashMap<String,String> EntriesToRemove = new HashMap<String,String>();
	protected HashMap<String,String> EntriesToUpdate = new HashMap<String,String>();

	protected abstract String getAddQuery(String key, String value);
	protected abstract String getUpdateQuery(String key, String value);
	protected abstract String getRemoveQuery(String key);

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

		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, getTableName())) {
			result = false;
			ReportManager.problem(this, con, getTableName() + " table not present");
			return result;
		}

		// These methods return false if there is any problem with the test
		result &= runTest(dbre);

		// I still have to check if some entries have to be removed/inserted/updated
		for (Map.Entry<String, String> key_value : EntriesToRemove.entrySet()) {
			ReportManager.problem(this, con, "Remove from " + getTableName() + ": " + key_value.getKey() + " -- " + key_value.getValue());
			result = false;
		}
		for (Map.Entry<String, String> key_value : EntriesToAdd.entrySet()) {
			ReportManager.problem(this, con, "Add in " + getTableName() + ": " + key_value.getKey() + " -- " + key_value.getValue());
			result = false;
		}
		for (Map.Entry<String, String> key_value : EntriesToUpdate.entrySet()) {
			ReportManager.problem(this, con, "Update in " + getTableName() + ": " + key_value.getKey() + " -- " + key_value.getValue());
			result = false;
		}

		return result;
	}

	abstract protected boolean runTest(DatabaseRegistryEntry dbre);

	// ------------------------------------------
	// Implementation of Repair interface.

	/**
	 * Update, insert and delete entries in the table in order to match the
	 * expected tags
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void repair(DatabaseRegistryEntry dbre) {

		if (EntriesToAdd.isEmpty() && EntriesToUpdate.isEmpty() && EntriesToRemove.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.print("Repairing the " + getTableName() + " table... ");
		Connection con = dbre.getConnection();
		try {
			Statement stmt = con.createStatement();

			// Start by removing entries as a duplicated entry will be both deleted and then inserted
			for (Map.Entry<String, String> key_value : EntriesToRemove.entrySet()) {
				String sql = getRemoveQuery(key_value.getKey());
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows DELETED for key \"" + key_value.getKey() + "\" while repairing " + getTableName());
				}
			}
			for (Map.Entry<String, String> key_value : EntriesToAdd.entrySet()) {
				String sql = getAddQuery(key_value.getKey(), key_value.getValue());
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows INSERTED for key \"" + key_value.getKey() + "\" while repairing " + getTableName());
				}
			}
			for (Map.Entry<String, String> key_value : EntriesToUpdate.entrySet()) {
				String sql = getUpdateQuery(key_value.getKey(), key_value.getValue());
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows UPDATED for key \"" + key_value.getKey() + "\" while repairing " + getTableName());
				}
			}
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}

		System.out.println(" ok.");

	}

	/**
	 * Show MySQL statements needed to repair the table
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void show(DatabaseRegistryEntry dbre) {

		if (EntriesToAdd.isEmpty() && EntriesToUpdate.isEmpty() && EntriesToRemove.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.println("MySQL statements needed to repair the " + getTableName() + " table:");

		for (Map.Entry<String, String> key_value : EntriesToRemove.entrySet()) {
			String sql = getRemoveQuery(key_value.getKey());
			System.out.println("  " + sql);
		}
		for (Map.Entry<String, String> key_value : EntriesToAdd.entrySet()) {
			String sql = getAddQuery(key_value.getKey(), key_value.getValue());
			System.out.println("  " + sql);
		}
		for (Map.Entry<String, String> key_value : EntriesToUpdate.entrySet()) {
			String sql = getUpdateQuery(key_value.getKey(), key_value.getValue());
			System.out.println("  " + sql);
		}

	}

	// -------------------------------------------------------------------------

}
