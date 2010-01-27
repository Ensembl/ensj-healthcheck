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
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that the tables handling variation sets are valid and won't cause problems
 */
public class VariationSet extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of VariationSetTestCase
	 */
	public VariationSet() {

		addToGroup("variation");
		addToGroup("variation-release");

		setDescription("Checks that the variation_set tables are valid");

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the variation set data makes sense and has a valid tree structure.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

	    boolean result = true;
	    String msg = new String();
	    Connection con = dbre.getConnection();

	    try {
		Statement stmt = con.createStatement();
		ResultSet rs;
		boolean fetch;
		
		// Check that there are no variation mappings to orphan variation sets
		String query = new String(
		    "SELECT DISTINCT "+
		    "  vsv.variation_set_id "+
		    "FROM "+
		    " variation_set_variation vsv "+
		    "WHERE "+
		    "  NOT EXISTS("+
		    "    SELECT "+
		    "      * "+
		    "    FROM "+
		    "      variation_set vs "+
		    "    WHERE "+
		    "      vs.variation_set_id = vsv.variation_set_id"+
		    "  )"
		);
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    while (fetch) {
			ReportManager.problem(this,con,"There are variations mapped to a variation set with variation_set_id '" + String.valueOf(rs.getInt(1)) + "' in variation_set_variation without a corresponding entry in variation_set table");
			result = false;
			fetch = rs.next();
		    }
		}
		else {
		    msg += new String("No orphan variation sets in variation_set_variation\n");
		}

		// Check that no variation sets in the variation_set_structure table without an entry in variation_set
		query = new String(
		    "SELECT DISTINCT "+
		    "  vss.variation_set_super "+
		    "FROM "+
		    " variation_set_structure vss "+
		    "WHERE "+
		    "  NOT EXISTS("+
		    "    SELECT "+
		    "      * "+
		    "    FROM "+
		    "      variation_set vs "+
		    "    WHERE "+
		    "      vs.variation_set_id = vss.variation_set_super"+
		    "  )"
		);
		String ids = new String();
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    while (fetch) {
			ids += String.valueOf(rs.getInt(1)) + ", ";
			fetch = rs.next();
		    }
		}
		query = new String(
		    "SELECT DISTINCT "+
		    "  vss.variation_set_sub "+
		    "FROM "+
		    " variation_set_structure vss "+
		    "WHERE "+
		    "  NOT EXISTS("+
		    "    SELECT "+
		    "      * "+
		    "    FROM "+
		    "      variation_set vs "+
		    "    WHERE "+
		    "      vs.variation_set_id = vss.variation_set_sub"+
		    "  )"
		);
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    while (fetch) {
			ids += String.valueOf(rs.getInt(1)) + ", ";
			fetch = rs.next();
		    }
		}
		
		if (ids.length() > 0) {
		    ReportManager.problem(this,con,"There are variation sets in variation_set_structure with ids " + ids.substring(0,ids.length()-2) + " without corresponding entries in variation_set table");
		    result = false;
		}
		else {
		    msg += new String("No orphan variation sets in variation_set_struncture\n");
		}
		
		// Check that all variations included in sets have an entry in the variation table and the validation status is not 'failed'
		query = new String(
		    "SELECT "+
		    "  vsv.variation_set_id "+
		    "FROM "+
		    "  variation_set_variation vsv, "+
		    "  variation v "+
		    "WHERE "+
		    "  v.variation_id = vsv.variation_id AND "+
		    "  v.validation_status = 'failed' "+
		    "LIMIT 1"
		);
		if ((rs = stmt.executeQuery(query)) != null && rs.next()) {
		    ReportManager.problem(this,con,"There are variations with validation_status = 'failed' mapped to variation set " + this.getVariationSetName(rs.getInt(1),con));
		    result = false;
		}
		else {
		    msg += new String("No variations in variation sets have validation_status 'failed'\n");
		}
		
		query = new String(
		    "SELECT "+
		    "  vsv.variation_set_id "+
		    "FROM "+
		    "  variation_set_variation vsv "+
		    "WHERE "+
		    "  NOT EXISTS ("+
		    "    SELECT "+
		    "      * "+
		    "    FROM "+
		    "      variation v "+
		    "    WHERE "+
		    "      v.variation_id = vsv.variation_id"+
		    "  ) "+
		    "LIMIT 1"
		);
		if ((rs = stmt.executeQuery(query)) != null && rs.next()) {	
		    ReportManager.problem(this,con,"There are variations mapped to variation set " + this.getVariationSetName(rs.getInt(1),con) + " that don't exist in the variation table");
		    result = false;
		}
		else {
		    msg += new String("All variations in variation sets have entries in variation table\n");
		}
		
		// Check if variation sets that are not used (neither in variation_set_variation nor variation_set_structure) are present in variation_set table
		query = new String(
		  "SELECT DISTINCT "+
		  "  vs.variation_set_id "+
		  "FROM "+
		  "  variation_set vs "+
		  "WHERE "+
		  "  NOT EXISTS ("+
		  "    SELECT "+
		  "      * "+
		  "    FROM "+
		  "      variation_set_variation vsv "+
		  "    WHERE "+
		  "      vsv.variation_set_id = vs.variation_set_id"+
		  "  ) AND "+
		  "  NOT EXISTS ("+
		  "    SELECT "+
		  "      * "+
		  "    FROM "+
		  "      variation_set_structure vss "+
		  "    WHERE "+
		  "      vss.variation_set_super = vs.variation_set_id OR "+
		  "      vss.variation_set_sub = vs.variation_set_id"+
		  "  )"
		);
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    String sets = new String();
		    while (fetch) {
			sets += "[" + this.getVariationSetName(rs.getInt(1),con) + "], ";
			fetch = rs.next();
		    }
		    ReportManager.problem(this,con,"Variation sets " + sets.substring(0,sets.length()-2) + " appear not to be used");
		    result = false;
		}
		else {
		    msg += new String("No unused variation sets\n");
		}
		
		// Check that no subset has more than one parent
		query = new String(
		    "SELECT DISTINCT "+
		    "  vss.variation_set_sub "+
		    "FROM "+
		    "  variation_set_structure vss "+
		    "WHERE "+
		    "  EXISTS ("+
		    "    SELECT "+
		    "      * "+
		    "    FROM "+
		    "      variation_set_structure vss2 "+
		    "    WHERE "+
		    "      vss2.variation_set_sub = vss.variation_set_sub AND "+
		    "      vss2.variation_set_super != vss.variation_set_super"+
		    "  )"
		);
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    String sets = new String();
		    while (fetch) {
			sets += "[" + this.getVariationSetName(rs.getInt(1),con) + "], ";
			fetch = rs.next();
		    }
		    ReportManager.problem(this,con,"Variation sets " + sets.substring(0,sets.length()-2) + " have more than one super set");
		    result = false;
		}
		else {
		    msg += new String("No variation sets have more than one super set\n");
		}
		
		// Check that no variation set is a subset of itself
		query = new String(
		  "SELECT DISTINCT "+
		  "  vss.variation_set_super "+
		  "FROM "+
		  "  variation_set_structure vss "
		);
		
		boolean no_reticulation = true;
		if ((rs = stmt.executeQuery(query)) != null && (fetch = rs.next())) {		    
		    while (fetch && no_reticulation) {
			int parent_id = rs.getInt(1);
			ArrayList path = this.getAllSubsets(parent_id,new ArrayList(),con);
			// If the tree structure is invalid, the path will contain "(!)"
			if (path.contains(new String("(!)"))) {
			    // Translate the dbIDs of the nodes to the corresponding names stored in the variation_set table 
			    String nodes = new String();
			    for (int i=0; i<(path.size()-1); i++) {
				nodes += "[" + this.getVariationSetName(((Integer) path.get(i)).intValue(),con) + "]->";
			    }
			    nodes = nodes.substring(0,nodes.length()-2) + " (!)";
			    ReportManager.problem(this,con,"There is a variation set that is a subset of itself, " + nodes);
			    result = false;
			    no_reticulation = false;
			}
			fetch = rs.next();
		    }
		}
		if (no_reticulation) {
		    msg += new String("No variation set is a subset of itself\n");
		}
		
	    } catch (Exception e) {
		ReportManager.problem(this,con,"Exception occured during healthcheck: " + e.toString());
		result = false;
	    }
		
	    if (result) {
	        ReportManager.correct(this, con, msg);
	    }
	    System.out.println(msg);
	    return result;

	} // run

	// -----------------------------------------------------------------

	private String getVariationSetName(int variationSetId, Connection con) throws Exception {
	    Statement stmt = con.createStatement();
	    ResultSet rs;
	    
	    String query = new String(
		"SELECT "+
		"  vs.name "+
		"FROM "+
		"  variation_set vs "+
		"WHERE "+
		"  vs.variation_set_id = '" + String.valueOf(variationSetId) + "'"
	    );
	    
	    String name = new String();
	    
	    if ((rs = stmt.executeQuery(query)) != null && rs.next()) {
		name = rs.getString(1);
	    }
	    
	    return name;
	} // getVariationSetName

	// -----------------------------------------------------------------
	
	/**
	 * Recursively parses the subset tree of a variation set. If a condition is encountered where a variation set has already
	 * been observed higher up in the tree, the recursion will abort and the last element of the returned ArrayList will be a
	 * string "(!)".
	 * 
	 * @param parent
	 *          The database id of the variation set to get subsets for.
	 * @param path
	 *          An ArrayList containing the dbIDs of nodes already visited in the tree. 
	 * @param con
	 *          A connection object to the database. 
	 * @return ArrayList containing the dbIDs of visited nodes in the tree. If a reticulation in the tree has been encountered, the last element will be a string "(!)".
	 */
	private ArrayList getAllSubsets(int parent, ArrayList path, Connection con) throws Exception {
	    
	    Statement stmt = con.createStatement();
	    ResultSet rs;
	    
	    boolean seen = path.contains(new Integer(parent));
	    path.add(new Integer(parent));
	    
	    if (seen) {
		path.add(new String("(!)"));
		return path;
	    }
	    
	    String query = new String(
		"SELECT "+
		"  vss.variation_set_sub "+
		"FROM "+
		"  variation_set_structure vss "+
		"WHERE "+
		"  vss.variation_set_super = '" + String.valueOf(parent) + "'"
	    );
	    
	    ArrayList subPath = new ArrayList(path);
	    // As long as there are sub sets, get all subsets for each of them
	    if (stmt.execute(query)) {
		rs = stmt.getResultSet();
		while (rs.next() && !seen) {
		    int id = rs.getInt(1);
		    subPath = this.getAllSubsets(id,new ArrayList(path),con);
		    seen = subPath.contains(new String("(!)"));
		}
	    }
	    
	    if (seen) {
		return new ArrayList(subPath);
	    }
	    
	    return new ArrayList(path);
	} // getAllSubsets
	
	// -----------------------------------------------------------------

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
