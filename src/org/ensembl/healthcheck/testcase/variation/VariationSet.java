/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the tables handling variation sets are valid and won't cause problems
 */
public class VariationSet extends SingleDatabaseTestCase {

	// The maximum variation_set_id value that we can store in the set construct in the variation_feature table
	private static final int MAX_VARIATION_SET_ID = 64;

	/**
	 * Creates a new instance of VariationSetTestCase
	 */
	public VariationSet() {


		setDescription("Checks that the variation_set tables are valid");

		setTeamResponsible(Team.VARIATION);

	}

	// ---------------------------------------------------------------------

	/**
	 * Store the SQL queries in a Properties object.
	 */
	private Properties getSQLQueries() {

		// Store all the needed SQL statements in a Properties object
		Properties sqlQueries = new Properties();
		String query;

		// Query checking that no variation set has more than one parent
		query = 
				"SELECT DISTINCT vss1.variation_set_sub FROM variation_set_structure vss1 JOIN variation_set_structure vss2 ON (vss2.variation_set_sub = vss1.variation_set_sub AND vss2.variation_set_super != vss1.variation_set_super)";
		sqlQueries.setProperty("multiParent", query);

		// Query getting all parent variation sets
		query = "SELECT DISTINCT vss.variation_set_super FROM variation_set_structure vss";
		sqlQueries.setProperty("parentSets", query);

		// Query getting the name for a variation set id
		query = "SELECT vs.name FROM variation_set vs WHERE vs.variation_set_id = ? LIMIT 1";
		sqlQueries.setProperty("setName", query);

		// Query checking the name for a variation set id is defined
		query = "SELECT count(*) FROM variation_set vs WHERE vs.name is NULL OR vs.name='NULL' ";
		sqlQueries.setProperty("setCheckName", query);

		// Query checking the description for a variation set id is defined
		query = "SELECT count(*) FROM variation_set vs WHERE vs.description is NULL OR vs.description='NULL' ";
		sqlQueries.setProperty("setCheckDescription", query);

		// Query checking the short name for a variation set id exists and is defined in the attrib table
		query = "SELECT count(*) FROM variation_set vs LEFT JOIN attrib a ON (vs.short_name_attrib_id = a.attrib_id) WHERE vs.variation_set_id IS NOT NULL AND a.value IS NULL";
		sqlQueries.setProperty("setCheckShortName", query);

		// Query getting the subsets for a parent variation set id
		query = "SELECT vss.variation_set_sub FROM variation_set_structure vss WHERE vss.variation_set_super = ?";
		sqlQueries.setProperty("subSet", query);

		// Query getting all combinations of variation sets that contain at least one common variation. These can be checked that none
		// of them have a (grand)parent-child relationship
		query = 
				"SELECT DISTINCT vsv1.variation_set_id, vsv2.variation_set_id FROM variation_set_variation vsv1 JOIN variation_set_variation vsv2 ON (vsv2.variation_id = vsv1.variation_id AND vsv2.variation_set_id > vsv1.variation_set_id)";
		sqlQueries.setProperty("setCombos", query);

		// Query for checking that the primary keys of all variation sets will fit into the variation_set_id set column in
		// variation_feature
		query = "SELECT COUNT(*) FROM variation_set vs WHERE vs.variation_set_id > ?";
		sqlQueries.setProperty("variationSetIds", query);

		return sqlQueries;
	}

	/**
	 * Check that the variation set data makes sense and has a valid tree structure.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;
		String msg = "";
		Connection con = dbre.getConnection();

		try {

			// Store all the needed SQL statements in a Properties object
			Properties sqlQueries = getSQLQueries();
			Statement stmt = con.createStatement();
			ResultSet rs;
			boolean fetch;

			// Prepare a statement for getting the variation set name from an id
			PreparedStatement setName = con.prepareStatement(sqlQueries.getProperty("setName"));
			// Prepare a statement for getting the variation subsets for an id
			PreparedStatement subSet = con.prepareStatement(sqlQueries.getProperty("subSet"));
			
			// Check that no variation set is missing the short name
			int countShortNames = DBUtils.getRowCount(con,sqlQueries.getProperty("setCheckShortName"));
			if (countShortNames > 0) {
				result = false;
				ReportManager.problem(this, con, "There are " + String.valueOf(countShortNames)
						+ " variation set(s) whose short name 'short_name_attrib_id' is missing OR is not defined in 'attrib' table");
			}
			// Check that no variation set is missing the name
			int countNames = DBUtils.getRowCount(con,sqlQueries.getProperty("setCheckName"));
			if (countNames > 0) {
				result = false;
				ReportManager.problem(this, con, "There are " + String.valueOf(countNames)
						+ " variation set(s) whose 'name' is missing");
			}
			// Check that no variation set is missing the description
			int countDesc = DBUtils.getRowCount(con,sqlQueries.getProperty("setCheckDescription"));
			if (countDesc > 0) {
				result = false;
				ReportManager.problem(this, con, "There are " + String.valueOf(countDesc)
						+ " variation set(s) whose 'description' is missing");
			}

			// Check that no subset has more than one parent
			if ((rs = stmt.executeQuery(sqlQueries.getProperty("multiParent"))) != null && (fetch = rs.next())) {
				String sets = "";
				while (fetch) {
					sets += "[" + this.getVariationSetName(rs.getInt(1), setName) + "], ";
					fetch = rs.next();
				}
				ReportManager.problem(this, con, "Variation sets " + sets.substring(0, sets.length() - 2) + " have more than one parent set");
				result = false;
			} else {
				msg += "No variation sets have more than one super set\n";
			}

			// Check that no variation set is a subset of itself
			boolean no_reticulation = true;
			if ((rs = stmt.executeQuery(sqlQueries.getProperty("parentSets"))) != null && (fetch = rs.next())) {
				while (fetch && no_reticulation) {
					int parent_id = rs.getInt(1);
					ArrayList path = this.getAllSubsets(parent_id, new ArrayList(), subSet);
					// If the tree structure is invalid, the path will contain "(!)"
					if (path.contains("(!)")) {
						// Translate the dbIDs of the nodes to the corresponding names stored in the variation_set table
						String nodes = "";
						for (int i = 0; i < (path.size() - 1); i++) {
							nodes += "[" + this.getVariationSetName(((Integer) path.get(i)).intValue(), setName) + "]->";
						}
						nodes = nodes.substring(0, nodes.length() - 2) + " (!)";
						ReportManager.problem(this, con, "There is a variation set that is a subset of itself, " + nodes);
						result = false;
						no_reticulation = false;
					}
					fetch = rs.next();
				}
			}
			if (no_reticulation) {
				msg += "No variation set is a subset of itself\n";
			}

			// Check that variations that are in a subset are not also present in the superset(s)
			// If there are problems with the set tree structure, we must skip this test
			if (no_reticulation) {
				rs = stmt.executeQuery(sqlQueries.getProperty("setCombos"));
				boolean redundant = false;
				while (rs != null && rs.next()) {
					Integer set1 = new Integer(rs.getInt(1));
					Integer set2 = new Integer(rs.getInt(2));
					Hashtable tree = this.getSetHierarchy(set1, subSet);
					boolean related = this.isSubSet(set2, tree);
					if (!related) {
						tree = this.getSetHierarchy(set2, subSet);
						related = this.isSubSet(set1, tree);
						Integer t = set2;
						set2 = set1;
						set1 = t;
					}
					if (related) {
						ReportManager.problem(this, con, "The variation set '" + this.getVariationSetName(set1.intValue(), setName) + "' contains variations that are also present in the subset '"
								+ this.getVariationSetName(set2.intValue(), setName) + "'. Preferrably, only the subset '" + this.getVariationSetName(set2.intValue(), setName) + "' should contain those variations");
						result = false;
					}
				}
				if (!redundant) {
					msg += "No nested sets contain overlapping variations\n";
				}
			} else {
				ReportManager.info(this, con, "Will not look for overlapping variations in nested subsets because of problems with the nested set structure");
			}
			
			// Prepare a statement for checking the variation_set_ids
			PreparedStatement vsIds = con.prepareStatement(sqlQueries.getProperty("variationSetIds"));
			vsIds.setInt(1, MAX_VARIATION_SET_ID);
			rs = vsIds.executeQuery();

			// Check the returned count, it should be 0
			rs.next();
			int count = rs.getInt(1);
			if (count > 0) {
				result = false;
				ReportManager.problem(this, con, "There are " + String.valueOf(count)
						+ " variation set(s) whose primary key 'variation_set_id' is too large to be stored in the variation_set_id column in the variation_feature table");
			}

		} catch (Exception e) {
			ReportManager.problem(this, con, "Exception occured during healthcheck: " + e.toString());
			result = false;
		}

		if (result) {
			ReportManager.correct(this, con, msg);
		}
		return result;

	} // run

	// -----------------------------------------------------------------

	private String getVariationSetName(int variationSetId, PreparedStatement pStmt) throws Exception {
		pStmt.setInt(1, variationSetId);
		ResultSet rs = pStmt.executeQuery();

		String name = "";
		if (rs.next()) {
			name = rs.getString(1);
		}

		return name;
	} // getVariationSetName

	// -----------------------------------------------------------------

	/**
	 * Recursively parses the subset tree of a variation set. If a condition is encountered where a variation set has already been
	 * observed higher up in the tree, the recursion will abort and the last element of the returned ArrayList will be a string "(!)".
	 * 
	 * @param parent
	 *          The database id of the variation set to get subsets for.
	 * @param path
	 *          An ArrayList containing the dbIDs of nodes already visited in the tree.
	 * @param con
	 *          A connection object to the database.
	 * @return ArrayList containing the dbIDs of visited nodes in the tree. If a reticulation in the tree has been encountered, the
	 *         last element will be a string "(!)".
	 */
	private ArrayList getAllSubsets(int parent, ArrayList path, PreparedStatement pStmt) throws Exception {

		boolean seen = path.contains(new Integer(parent));
		path.add(new Integer(parent));

		if (seen) {
			path.add("(!)");
			return path;
		}

		pStmt.setInt(1, parent);
		ResultSet rs;

		ArrayList subPath = new ArrayList(path);
		// As long as there are sub sets, get all subsets for each of them
		if (pStmt.execute()) {
			rs = pStmt.getResultSet();
			while (rs.next() && !seen) {
				int id = rs.getInt(1);
				subPath = this.getAllSubsets(id, new ArrayList(path), pStmt);
				seen = subPath.contains("(!)");
			}
		}

		if (seen) {
			return new ArrayList(subPath);
		}

		return new ArrayList(path);
	} // getAllSubsets

	// -----------------------------------------------------------------

	/**
	 * Get the Tree structure including and below the supplied set as a Hashtable. Each set is represented with its dbID as key and a
	 * Hashtable containing its subsets as value.
	 */
	private Hashtable getSetHierarchy(Integer current, PreparedStatement pStmt) throws Exception {

		// Create a hashtable to hold this node's subtree
		Hashtable subtree = new Hashtable();

		// Get all the children of the current node's trees
		pStmt.setInt(1, current.intValue());
		ResultSet rs;

		// As long as there are sub sets, get all subsets for each of them
		if (pStmt.execute()) {
			rs = pStmt.getResultSet();
			while (rs.next()) {
				// Add the child's subtree
				Integer nextId = new Integer(rs.getInt(1));
				subtree.putAll(this.getSetHierarchy(nextId, pStmt));
			}
		}

		// Finally, add the subtree as a value to the current node
		Hashtable tree = new Hashtable();
		tree.put(current, subtree);
		return tree;
	}

	/**
	 * Test whether a given set id is a subset in the specified hierarchy
	 */
	private boolean isSubSet(Integer query, Hashtable tree) throws Exception {

		// Loop over the values in the current tree until it's empty or we have encountered the query set
		boolean isSubSet = false;
		Enumeration keys = tree.keys();

		while (!isSubSet && keys.hasMoreElements()) {
			Integer key = (Integer) keys.nextElement();
			isSubSet = key.equals(query);
			if (!isSubSet) {
				isSubSet = this.isSubSet(query, (Hashtable) tree.get(key));
			}
		}

		return isSubSet;
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

} // EmptyVariationTablesTestCase
