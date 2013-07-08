/*
 * Copyright (C) 2003 EBI, GRL
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
package org.ensembl.healthcheck.testcase.funcgen;

import java.util.Map;
import java.sql.ResultSet;
import java.util.HashMap;
import java.sql.Statement;
import java.sql.SQLException;
import org.ensembl.healthcheck.testcase.generic.ComparePreviousVersionBase;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;

/**
 * Compare the xrefs in the current database with those from the equivalent
 * database on the secondary server.
 */

public class ComparePreviousVersionRegulatoryFeatures extends ComparePreviousVersionBase {


	/**
	 * Create a new  testcase.
	 */

	public ComparePreviousVersionRegulatoryFeatures() {
		addToGroup("release");
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		//setHintLongRunning(true);// ?Only take about 10 mins for mouse
		setTeamResponsible(Team.FUNCGEN);
		setDescription("Compare the numbers of Regulatory Features (and the underlying support features) in the current database with those from the equivalent database on the secondary server");
	}

	// ----------------------------------------------------------------------

	protected Map<String, Integer> getCounts(DatabaseRegistryEntry dbre) {

		//Doing this in one query will most likely run the instance out of resources
		//Need to return an array of queries which can be performed bitwise
		
		Map<String, Integer> fCounts = new HashMap<String, Integer>();
		
		//Get ids for all DISPLAYABLE feature sets

		//Expand this to all feature classes?

		String sql = "select fs.name, fs.type from feature_set fs, status s, status_name sn"+
		" where sn.name='DISPLAYABLE' and sn.status_name_id=s.status_name_id and s.table_name='feature_set' and "+
		" s.table_id=fs.feature_set_id and fs.type='regulatory' group by fs.feature_set_id";

		try {

			Statement idStmt     = dbre.getConnection().createStatement();
			//Create separate Statement so we don't close the fIDs ResultSet
			Statement cntStmt    = dbre.getConnection().createStatement();
			ResultSet fIDs = idStmt.executeQuery(sql);

			while (fIDs != null && fIDs.next()) {
				//ResultSets are 'never null', so why test for this?			
				
				//sql = "SELECT fs.name, COUNT(distinct f."+fIDs.getString(3)+"_feature_id) "+
				sql = "SELECT fs.name, COUNT(*) "+
				" FROM " + fIDs.getString(2) + "_feature f, feature_set fs " +
				" WHERE f.feature_set_id=fs.feature_set_id and fs.name='" + fIDs.getString(1) +
				"' GROUP BY f.feature_set_id";
				
				try {
					//logger.finest("Counting " + entityDescription() + " for " + dbre.getName() + ':' + fIDs.getString(2));
					ResultSet rsCounts = cntStmt.executeQuery(sql);
								
					while (rsCounts != null && rsCounts.next()) {
						//recast count which mysql returns as string as Integer?
						fCounts.put(rsCounts.getString(1), new Integer(rsCounts.getInt(2)));
					}
				} catch (SQLException e){
					e.printStackTrace();
				}
			}
		}catch (SQLException e){
			e.printStackTrace();
		}

		
		return fCounts;

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "Regulatory Features";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		//only allow 10% change before fail?
		return 0.95;

	}

        protected double minimum() {

                return 0;

        }

        // ------------------------------------------------------------------------

	
	protected boolean testUpperThreshold(){
		return true;
	}
	
	
} // ComparePreviousVersionArrayXrefs

