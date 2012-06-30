/*
 Copyright (C) 2004 EBI, GRL
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
