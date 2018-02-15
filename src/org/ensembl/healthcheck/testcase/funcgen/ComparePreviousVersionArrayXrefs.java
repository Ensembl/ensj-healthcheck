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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;



/**
 * Compare the xrefs in the current database with those from the equivalent
 * database on the secondary server.
 */

public class ComparePreviousVersionArrayXrefs extends RegulationComparePreviousVersion {

	/**
	 * Create a new  testcase.
	 */

	public ComparePreviousVersionArrayXrefs() {
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		//setHintLongRunning(true);// ?Only take about 10 mins for mouse
		setTeamResponsible(Team.FUNCGEN);
		setDescription("Compare the Arrays xrefs in the current database with those from the equivalent database on the secondary server");
	}


	//We could add a run wrapper here to compare DISPLAYABLE arrays first?


	// ---------[-------------------------------------------------------------

	protected Map getCounts(DatabaseRegistryEntry dbre) {

		//Doing this in one query will most likely run the instance out of resources
		//Need to return an array of queries which can be performed bitwise

		Map arrayXrefCounts = new HashMap();
		//Get array_ids for all DISPLAYABLE
		//Also need to compare DISPLAYABLE arrays HC?

		String sql = "select a.array_id, a.vendor, a.name from array a " +
			" group by a.array_id";

		
		//Count object_xref records for each array

		try {
			Statement idStmt     = dbre.getConnection().createStatement();
			//Create separate Statment so we don't close the arrayIDs ResultSet
			Statement cntStmt    = dbre.getConnection().createStatement();
			ResultSet arrayIDs = idStmt.executeQuery(sql);
			//System.out.println(sql);

			while (arrayIDs != null && arrayIDs.next()) {
				//ResultSets are 'never null', so why test for this?
				
				//Set this to ProbeSet for affy, and Probe for all others
				//Would be nice to have this in the DB, but rely on vendor for now
				
				//Should probably 

				if (arrayIDs.getString(2).equals("AFFY")) { //ProbeSets
					//
					
					sql = "SELECT a.name AS array_name, COUNT(distinct ox.object_xref_id) AS count FROM external_db edb, xref x, " +
					 	"object_xref ox, probe p, array_chip ac, array a " +
						"WHERE edb.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id and ox.ensembl_id=p.probe_set_id and " +
						"p.array_chip_id=ac.array_chip_id and ac.array_id=a.array_id and ox.ensembl_object_type='ProbeSet' and " +
						"edb.db_display_name='EnsemblTranscript' and a.array_id=" + arrayIDs.getString(1) +
						" GROUP BY a.array_id";
				} 
				else { //Probes
					sql = "SELECT a.name AS array_name, COUNT(distinct ox.object_xref_id) AS count FROM external_db edb, xref x, " + 
						"object_xref ox, probe p, array_chip ac, array a " +
						"WHERE edb.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id and ox.ensembl_id=p.probe_id and " +
						"p.array_chip_id=ac.array_chip_id and ac.array_id=a.array_id and ox.ensembl_object_type='Probe' and " +
					 	"edb.db_display_name='EnsemblTranscript' and a.array_id=" + arrayIDs.getString(1) +
						" GROUP BY a.array_id";
				}


				//SELECT a.name AS array_name, COUNT(distinct ox.object_xref_id) AS count FROM external_db edb, xref x, object_xref ox, probe p, array_chip ac, array a WHERE edb.external_db_id=x.external_db_id AND x.xref_id=ox.xref_id and ox.ensembl_id=p.probe_id and p.array_chip_id=ac.array_chip_id and ac.array_id=a.array_id and ox.ensembl_object_type='Probe' and edb.db_display_name='EnsemblTranscript' and a.array_id='' GROUP BY a.array_id


				//Can rely on only having xrefs against one edb per array due to 
				//mandatory rollback in probe2transcript

				//Could do with warning if edb does not match schema build if new and old schema_build do not match.
				//i.e. we have a new assembly or gene build update


				//System.out.println(sql);
			
				//Recreate getCountsBySQL
			
				try {
					logger.finest("Counting " + entityDescription() + " for " + dbre.getName() + ':' + arrayIDs.getString(3)
								  + "\nUSING SQL:\n" + sql);
					ResultSet xrefCounts = cntStmt.executeQuery(sql);
								
					while (xrefCounts != null && xrefCounts.next()) {
						//recast count which mysql returns as string as Integer?
						arrayXrefCounts.put(xrefCounts.getString(1), new Integer(xrefCounts.getInt(2)));
					}
				} catch (SQLException e){
					e.printStackTrace();
				}
			}
		}catch (SQLException e){
			e.printStackTrace();
		}

		return arrayXrefCounts;

	} // ------------------------------------------------------------------------

	protected String entityDescription() {

		return "Array Xrefs";

	}

	// ------------------------------------------------------------------------

	protected double threshold() {

		//only allow 10% change before fail?
		return 0.95;

	}

        // ------------------------------------------------------------------------

        protected double minimum() {

                return 0;

        }

	// ----------------------------------------------------------------------

	protected boolean testUpperThreshold(){
		return true;
	}


	// ----------------------------------------------------------------------

} // ComparePreviousVersionArrayXrefs

