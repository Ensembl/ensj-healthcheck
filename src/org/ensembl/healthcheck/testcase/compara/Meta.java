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

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key relationships.
 */

public class Meta extends SingleDatabaseTestCase implements Repair {

	private HashMap MetaEntriesToAdd = new HashMap();

	private HashMap MetaEntriesToRemove = new HashMap();

	private HashMap MetaEntriesToUpdate = new HashMap();

	private HashMap SpeciesIdToUpdate = new HashMap();

	/**
	 * Create an ForeignKeyMethodLinkId that applies to a specific set of databases.
	 */
	public Meta() {

		addToGroup("compara_genomic");
		addToGroup("compara_homology");
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
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		if (!DBUtils.checkTableExists(con, "meta")) {
			result = false;
			ReportManager.problem(this, con, "Meta table not present");
			return result;
		}

		// These methods return false if there is any problem with the test
		result &= checkSchemaVersionDBName(dbre);

		result &= checkSpeciesId(dbre);

		// I still have to check if some entries have to be removed/inserted/updated
		Iterator it = MetaEntriesToRemove.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Remove from meta: " + next + " -- " + MetaEntriesToRemove.get(next));
			result = false;
		}
		it = MetaEntriesToAdd.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Add in meta: " + next + " -- " + MetaEntriesToAdd.get(next));
			result = false;
		}
		it = MetaEntriesToUpdate.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Update in meta: " + next + " -- " + MetaEntriesToUpdate.get(next));
			result = false;
		}

		it = SpeciesIdToUpdate.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			ReportManager.problem(this, con, "Update in meta: " + next + " -- " + SpeciesIdToUpdate.get(next));
			result = false;
		}

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
				if (rs.getString(2).equals("schema_version")) {
					if (rs.getInt(1) != 0) {
						// set species_id of schema_version to NULL
						SpeciesIdToUpdate.put(rs.getString(2), "NULL");
					}
				} else {
					if (rs.getInt(1) != 1) {
						// set species_id of everything else to 1
						SpeciesIdToUpdate.put(rs.getString(2), new Integer(1));
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
					MetaEntriesToUpdate.put("schema_version", new Integer(dbNameVersion));
				}
			} else {
				MetaEntriesToAdd.put("schema_version", new Integer(dbNameVersion));
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		return result;

	} // ---------------------------------------------------------------------


	// ------------------------------------------
	// Implementation of Repair interface.

	/**
	 * Update, insert and delete entries in the meta table in order to match max. alignment lengths found in the genomic_align table.
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void repair(DatabaseRegistryEntry dbre) {

		if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() && MetaEntriesToRemove.isEmpty() && SpeciesIdToUpdate.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.print("Repairing <meta> table... ");
		Connection con = dbre.getConnection();
		try {
			Statement stmt = con.createStatement();

			// Start by removing entries as a duplicated entry will be both deleted and then inserted
			Iterator it = MetaEntriesToRemove.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				String sql = "DELETE FROM meta WHERE meta_key = \"" + next + "\";";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows DELETED for meta_key \"" + next + "\" while repairing meta");
				}
			}
			it = MetaEntriesToAdd.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				// String sql = "INSERT INTO meta VALUES (NULL, \"" + next + "\", "
				// + MetaEntriesToAdd.get(next) + ", 1);");
				String sql = "INSERT INTO meta VALUES (NULL, 1, \"" + next + "\", " + MetaEntriesToAdd.get(next) + ");";

				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows INSERTED for meta_key \"" + next + "\" while repairing meta");
				}
			}
			it = MetaEntriesToUpdate.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();
				String sql = "UPDATE meta SET meta_value = " + MetaEntriesToUpdate.get(next) + " WHERE meta_key = \"" + next + "\";";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows UPDATED for meta_key \"" + next + "\" while repairing meta");
				}
			}
			it = SpeciesIdToUpdate.keySet().iterator();
			while (it.hasNext()) {
				Object next = it.next();

				String sql = "UPDATE meta SET species_id = " + SpeciesIdToUpdate.get(next) + " WHERE meta_key = \"" + next + "\";";
				int numRows = stmt.executeUpdate(sql);
				if (numRows != 1) {
					ReportManager.problem(this, con, "WARNING: " + numRows + " rows UPDATED for meta_key \"" + next + "\" while repairing meta");
				}
			}
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}

		System.out.println(" ok.");

	}

	/**
	 * Show MySQL statements needed to repair meta table
	 * 
	 * @param dbre
	 *          The database to use.
	 */
	public void show(DatabaseRegistryEntry dbre) {

		if (MetaEntriesToAdd.isEmpty() && MetaEntriesToUpdate.isEmpty() && MetaEntriesToRemove.isEmpty() && SpeciesIdToUpdate.isEmpty()) {
			System.out.println("No repair needed.");
			return;
		}

		System.out.println("MySQL statements needed to repair meta table:");

		Iterator it = MetaEntriesToRemove.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  DELETE FROM meta WHERE meta_key = \"" + next + "\";");
		}
		it = MetaEntriesToAdd.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  INSERT INTO meta VALUES (NULL, 1, \"" + next + "\", " + MetaEntriesToAdd.get(next) + ");");
		}
		it = MetaEntriesToUpdate.keySet().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			System.out.println("  UPDATE meta SET meta_value = " + MetaEntriesToUpdate.get(next) + " WHERE meta_key = \"" + next + "\";");
		}

	}

	// -------------------------------------------------------------------------

} // ForeignKeyMethodLinkId
