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
		setDescription("Tests that proper max_alignment_length have been defined.");
		setDescription("Check meta table for the right schema version and max alignment lengths");
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
		result &= checkMaxAlignmentLength(con);

		result &= checkSchemaVersionDBName(dbre);

		result &= checkConservationScoreLink(dbre);

//		result &= checkSpeciesTreesArePresent(dbre);

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
	 * Check that the each conservation score MethodLinkSpeciesSet obejct has a link to a multiple alignment MethodLinkSpeciesSet in
	 * the meta table.
	 */
	private boolean checkConservationScoreLink(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// get version from meta table
		Connection con = dbre.getConnection();

		// get all the links between conservation scores and multiple genomic alignments
		String sql = "SELECT mlss1.method_link_species_set_id," + " mlss2.method_link_species_set_id, ml1.type, ml2.type, count(*)"
				+ " FROM method_link ml1, method_link_species_set mlss1, method_link ml2," + " method_link_species_set mlss2 WHERE mlss1.method_link_id = ml1.method_link_id "
				+ " AND ml1.class = \"ConservationScore.conservation_score\" " + " AND mlss1.species_set_id = mlss2.species_set_id AND mlss2.method_link_id = ml2.method_link_id"
				+ " AND (ml2.class = \"GenomicAlignBlock.multiple_alignment\" OR ml2.class LIKE \"GenomicAlignTree.%\") GROUP BY mlss1.method_link_species_set_id";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(5) > 1) {
					ReportManager.problem(this, con, "MethodLinkSpeciesSet " + rs.getString(1) + " links to several multiple alignments!");
					result = false;
				} else if (rs.getString(3).equals("GERP_CONSERVATION_SCORE")) {
					MetaEntriesToAdd.put("gerp_" + rs.getString(1), new Integer(rs.getInt(2)).toString());
				} else {
					ReportManager.problem(this, con, "Using " + rs.getString(3) + " method_link_type is not supported by this healthcheck");
					result = false;
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// get all the values currently stored in the DB
		sql = "SELECT meta_key, meta_value, count(*) FROM meta WHERE meta_key LIKE \"gerp_%\" GROUP BY meta_key";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(3) != 1) {
					// Delete all current entries. The right entry will be added
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				} else if (MetaEntriesToAdd.containsKey(rs.getString(1))) {
					// Entry matches one of the required entries. Update if needed.
					if (!MetaEntriesToAdd.get(rs.getString(1)).equals(rs.getString(2))) {
						MetaEntriesToUpdate.put(rs.getString(1), MetaEntriesToAdd.get(rs.getString(1)));
					}
					// Remove this entry from the set of entries to be added (as it already exits!)
					MetaEntriesToAdd.remove(rs.getString(1));
				} else {
					// Entry is out-to-date
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		return result;

	} // ---------------------------------------------------------------------

// 	/**
// 	 * Check that the each multi-species analysis that uses a species tree has the species tree stored in the meta table.
// 	 */
// 	private boolean checkSpeciesTreesArePresent(DatabaseRegistryEntry dbre) {

// 		boolean result = true;

// 		// get version from meta table
// 		Connection con = dbre.getConnection();

// 		// get all the links between conservation scores and multiple genomic alignments
// 		String sql = "SELECT method_link_species_set_id, IFNULL(meta_key, 'NULL'), IFNULL(meta_value, 'NULL'),"
// 				+ " method_link_species_set.name, count(distinct genome_db_id)"
// 				+ " FROM method_link_species_set"
// 				+ " JOIN method_link USING (method_link_id)"
// 				+ " LEFT JOIN meta ON (meta_key = CONCAT('tree_', method_link_species_set_id))"
// 				+ " JOIN species_set USING (species_set_id)"
// 				+ " WHERE (class LIKE 'GenomicAlignTree%' OR class LIKE '%multiple_alignment' OR class LIKE '%tree_node')"
// 				+ " GROUP BY method_link_species_set_id";

// 		try {
// 			Statement stmt = con.createStatement();
// 			ResultSet rs = stmt.executeQuery(sql);
// 			while (rs.next()) {
// 				Integer methodLinkSpeciesSetIdInt = rs.getInt(1);
// 				String metaKeyStr = rs.getString(2);
// 				String treeStr = rs.getString(3);
// 				String methodLinkSpeciesSetNameStr = rs.getString(4);
// 				Integer numSpecies = rs.getInt(5);
// 				if (metaKeyStr.equals("NULL")) {
// 					ReportManager.problem(this, con, "MethodLinkSpeciesSet " + methodLinkSpeciesSetIdInt  +
// 							" (" + methodLinkSpeciesSetNameStr  + ") does not have its tree in the meta table!");
// 					result = false;
// 				} else if (StringUtils.countMatches(treeStr, "(") != StringUtils.countMatches(treeStr, ")")) {
// 					ReportManager.problem(this, con, "The tree for MethodLinkSpeciesSet " + methodLinkSpeciesSetIdInt  +
// 							" (" + methodLinkSpeciesSetNameStr  + ") does not have the same number of opening and closing brackets!");
// 					result = false;
// 				} else if (StringUtils.countMatches(treeStr, ",") + 1 != numSpecies) {
// 					ReportManager.problem(this, con, "The tree for MethodLinkSpeciesSet " + methodLinkSpeciesSetIdInt  +
// 							" (" + methodLinkSpeciesSetNameStr  + ") does not have the right number of leaves!");
// 					result = false;
// 				} else {
// 					MetaEntriesToAdd.put("tree_" + rs.getString(1), rs.getString(3));
// 				}
// 			}
// 			rs.close();
// 			stmt.close();
// 		} catch (SQLException se) {
// 			se.printStackTrace();
// 			result = false;
// 		}

// 		// get all the values currently stored in the DB
// 		sql = "SELECT meta_key, meta_value, count(*) FROM meta WHERE meta_key LIKE \"tree_%\" GROUP BY meta_key";

// 		try {
// 			Statement stmt = con.createStatement();
// 			ResultSet rs = stmt.executeQuery(sql);
// 			while (rs.next()) {
// 				if (rs.getInt(3) != 1) {
// 					// Delete all current entries. The right entry will be added
// 					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
// 				} else if (MetaEntriesToAdd.containsKey(rs.getString(1))) {
// 					// Entry matches one of the required entries. Update if needed.
// 					if (!MetaEntriesToAdd.get(rs.getString(1)).equals(rs.getString(2))) {
// 						MetaEntriesToUpdate.put(rs.getString(1), MetaEntriesToAdd.get(rs.getString(1)));
// 					}
// 					// Remove this entry from the set of entries to be added (as it already exits!)
// 					MetaEntriesToAdd.remove(rs.getString(1));
// 				} else {
// 					// Entry is out-to-date
// 					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
// 				}
// 			}
// 			rs.close();
// 			stmt.close();
// 		} catch (SQLException se) {
// 			se.printStackTrace();
// 			result = false;
// 		}

// 		return result;

// 	} // ---------------------------------------------------------------------

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

	/**
	 * Check the max_align_* and max_alignment_length in the meta table
	 */
	private boolean checkMaxAlignmentLength(Connection con) {

		boolean result = true;

		int globalMaxAlignmentLength = 0;

		// Check whether tables are empty or not
		if (!tableHasRows(con, "genomic_align")) {
			ReportManager.correct(this, con, "NO ENTRIES in genomic_align table");
			return true;
		}

		// Calculate current max_alignment_length by method_link_species_set
		String sql = "SELECT CONCAT('max_align_', method_link_species_set_id)," + " MAX(dnafrag_end - dnafrag_start) FROM genomic_align" + " GROUP BY method_link_species_set_id";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			// Add 2 to the dnafrag_end - dnafrag_start in order to get length + 1.
			// Adding this at this point is probably faster than asking MySQL to add 2
			// to every single row...
			while (rs.next()) {
				MetaEntriesToAdd.put(rs.getString(1), new Integer(rs.getInt(2) + 2).toString());
				if (rs.getInt(2) > globalMaxAlignmentLength) {
					globalMaxAlignmentLength = rs.getInt(2) + 2;
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// Calculate current max_align_ by method_link_species_set for constrained elements
		String sql_ce = "SELECT CONCAT('max_align_', method_link_species_set_id)," + " MAX(dnafrag_end - dnafrag_start) FROM constrained_element" + " GROUP BY method_link_species_set_id";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql_ce);
			// Add 2 to the dnafrag_end - dnafrag_start in order to get length + 1.
			// Adding this at this point is probably faster than asking MySQL to add 2
			// to every single row...
			while (rs.next()) {
				MetaEntriesToAdd.put(rs.getString(1), new Integer(rs.getInt(2) + 2).toString());
				if (rs.getInt(2) > globalMaxAlignmentLength) {
					globalMaxAlignmentLength = rs.getInt(2) + 2;
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// Get values currently stored in the meta table
		sql = "SELECT meta_key, meta_value, count(*)" + " FROM meta WHERE meta_key LIKE \"max\\_align\\_%\" GROUP BY meta_key";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				if (rs.getInt(3) != 1) {
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				} else if (MetaEntriesToAdd.containsKey(rs.getString(1))) {
					if (!MetaEntriesToAdd.get(rs.getString(1)).equals(rs.getString(2))) {
						MetaEntriesToUpdate.put(rs.getString(1), MetaEntriesToAdd.get(rs.getString(1)));
					}
					MetaEntriesToAdd.remove(rs.getString(1));
				} else {
					MetaEntriesToRemove.put(rs.getString(1), rs.getString(2));
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		// Get current global value from the meta table (used for backwards compatibility)
		sql = "SELECT meta_key, meta_value" + " FROM meta WHERE meta_key = \"max_alignment_length\"";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.first()) {
				if (rs.getInt(2) != globalMaxAlignmentLength) {
					MetaEntriesToUpdate.put("max_alignment_length", new Integer(globalMaxAlignmentLength).toString());
				}
			} else {
				MetaEntriesToAdd.put("max_alignment_length", new Integer(globalMaxAlignmentLength).toString());
			}
			rs.close();
			stmt.close();
		} catch (SQLException se) {
			se.printStackTrace();
			result = false;
		}

		return result;

	}

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
