/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/*
Abstract class that provides a "Repair" interface to the method_link_species_set_tag table.
Subclasses must fill the "tagToCheck" attribute and imnplement the "doCheck" method.
*/
public abstract class MethodLinkSpeciesSetTag extends SingleDatabaseTestCase implements Repair {

	protected HashMap MetaEntriesToAdd = new HashMap();

	protected HashMap MetaEntriesToRemove = new HashMap();

	protected HashMap MetaEntriesToUpdate = new HashMap();

	protected String tagToCheck;


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

		if (!DBUtils.checkTableExists(con, "method_link_species_set_tag")) {
			result = false;
			ReportManager.problem(this, con, "method_link_species_set_tag table not present");
			return result;
		}

		// These methods return false if there is any problem with the test
		result &= doCheck(con);

		// I still have to check if some entries have to be removed/inserted/updated
		Iterator it = MetaEntriesToRemove.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Remove from method_link_species_set_tag: " + next + " -- " + MetaEntriesToRemove.get(next));
			result = false;
		}
		it = MetaEntriesToAdd.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Add in method_link_species_set_tag: " + next + " -- " + MetaEntriesToAdd.get(next));
			result = false;
		}
		it = MetaEntriesToUpdate.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Update in method_link_species_set_tag: " + next + " -- " + MetaEntriesToUpdate.get(next));
			result = false;
		}

		return result;
	}

	/**
	 * Check that the each conservation score MethodLinkSpeciesSet obejct has a link to a multiple alignment MethodLinkSpeciesSet in
	 * the method_link_species_set_tag table.
	 */
	abstract boolean doCheck(Connection con);

	// ------------------------------------------
	// Implementation of Repair interface.

	/**
	 * Update, insert and delete entries in the method_link_species_set_tag table in order to match max. alignment lengths found in the genomic_align table.
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void repair(DatabaseRegistryEntry dbre) {

		if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() && MetaEntriesToRemove.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.print("Repairing <method_link_species_set_tag> table... ");
		Connection con = dbre.getConnection();
		try {
			Statement stmt = con.createStatement();

			// Start by removing entries as a duplicated entry will be both deleted and then inserted
			Iterator it = MetaEntriesToRemove.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				String sql = "DELETE FROM method_link_species_set_tag WHERE method_link_species_set_id = \"" + next + "\" AND tag = \"" + tagToCheck + "\";";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows DELETED for mlss_id \"" + next + "\" while repairing mlss_tag");
				}
			}
			it = MetaEntriesToAdd.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				String sql = "INSERT INTO method_link_species_set_tag VALUES (\"" + next + "\", \"" + tagToCheck + "\", " + MetaEntriesToAdd.get(next) + ");";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows INSERTED for mlss_id \"" + next + "\" while repairing mlss_tag");
				}
			}
			it = MetaEntriesToUpdate.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				String sql = "UPDATE method_link_species_set_tag SET value = " + MetaEntriesToUpdate.get(next) + " WHERE tag = \"" + tagToCheck + "\" AND method_link_species_set_id = \"" + next + "\";";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows UPDATED for mlss_id \"" + next + "\" while repairing mlss_tag");
				}
			}
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}

		System.out.println(" ok.");

	}

	/**
	 * Show MySQL statements needed to repair method_link_species_set_tag table
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void show(DatabaseRegistryEntry dbre) {

		if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() && MetaEntriesToRemove.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.println("MySQL statements needed to repair method_link_species_set_tag table:");

		Iterator it = MetaEntriesToRemove.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  DELETE FROM method_link_species_set_tag WHERE method_link_species_set_id = \"" + next + "\" AND tag = \"" + tagToCheck + "\";");
		}
		it = MetaEntriesToAdd.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  INSERT INTO method_link_species_set_tag VALUES (\"" + next + "\", \"" + tagToCheck + "\", " + MetaEntriesToAdd.get(next) + ");");
		}
		it = MetaEntriesToUpdate.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  UPDATE method_link_species_set_tag SET value = " + MetaEntriesToUpdate.get(next) + " WHERE tag = \"" + tagToCheck + "\" AND method_link_species_set_id = \"" + next + "\";");
		}

	}

	// -------------------------------------------------------------------------

}
