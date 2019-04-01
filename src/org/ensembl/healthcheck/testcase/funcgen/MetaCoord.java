/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.funcgen;

/** Can we not just modify the generic version to take a schema/dbtype type
	And store the variables in a hash? Set groups varaible dependant on dbtype?
	What is calling this?
*/

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.MissingMetaKeyException;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

import static org.apache.commons.lang.StringUtils.join;
import static org.ensembl.healthcheck.DatabaseType.CORE;

/**
 * Check that meta_coord table contains entries for all the coordinate systems
 * that all the features are stored in.
 */
public class MetaCoord extends AbstractCoreDatabaseUsingTestCase {

	private String[] featureTables = getFuncgenFeatureTables();

	/**
	 * Create a new instance of MetaCoord.
	 */
	public MetaCoord() {

		addToGroup("funcgen");
		addToGroup("funcgen-release");
		setDescription("Check that meta_coord table contains entries for all the coordinate systems that all the features are stored in");
                setTeamResponsible(Team.FUNCGEN);
	}

	/**
	 * Run the test.
	 * 
	 * @param funcgenDbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry funcgenDbre) {

		boolean result = true;
		
		DatabaseRegistryEntry coreDbre;
		
		try {
			
			coreDbre = getCoreDb(funcgenDbre);
			
		} catch (MissingMetaKeyException e) {

			ReportManager.problem(this, funcgenDbre.getConnection(), e.getMessage());
			return false;
			
		} catch (CoreDbNotFoundException e) {

			ReportManager.problem(this, funcgenDbre.getConnection(), e.getMessage());
			return false;
			
		}

		Connection funcgenCon = funcgenDbre.getConnection();
		Connection coreCon = coreDbre.getConnection();


		// coordSystems is a hash of lists of coordinate systems that each feature
		// table contains
		Map coordSystems = new HashMap(); 

		try {

			Statement funcgenStatement = funcgenCon.createStatement();
			Statement coreStatement = coreCon.createStatement();

			// build up a list of all the coordinate systems that are in the various
			// feature tables
			for (int tableIndex = 0; tableIndex < featureTables.length; tableIndex++) {

				String tableName = featureTables[tableIndex];
				String funcgenSql = "SELECT DISTINCT(seq_region_id) FROM " + tableName;

				logger.finest("Getting seq_region_ids for " + tableName);
				ResultSet funcgenRs = funcgenStatement.executeQuery(funcgenSql);

				ArrayList<String> seqRegionIDs=new ArrayList<>();

				if (! funcgenRs.isBeforeFirst()){
				    logger.warning("No features found for " + tableName);
				    continue;
                }

				if(funcgenRs.next())
                while (funcgenRs.next()){
					seqRegionIDs.add(funcgenRs.getString(1));
				}

				String seqRegionIDsString = seqRegionIDs.toString().replace("[","").replace("]","");
				String coreSql = "SELECT DISTINCT(coord_system_id) FROM seq_region WHERE seq_region_id IN (" + seqRegionIDsString + ")";
				logger.finest("Getting coord_system_ids for " + tableName);
				ResultSet coreRs = coreStatement.executeQuery(coreSql);

				while (coreRs.next()) {
					String coordSystemID = coreRs.getString(1);
					logger.finest("Added feature coordinate system for " + tableName + ": " + coordSystemID);
					// check that the meta_coord table has an entry corresponding to this
					int mc = DBUtils.getRowCount(funcgenCon, "SELECT COUNT(*) FROM meta_coord WHERE coord_system_id=" + coordSystemID + " AND table_name='"
							+ tableName + "'");
					if (mc == 0) {
						ReportManager.problem(this, funcgenCon, "No entry for coordinate system with ID " + coordSystemID + " for " + tableName
								+ " in meta_coord");
						result = false;
					} else if (mc > 1) {
						ReportManager.problem(this, funcgenCon, "Coordinate system with ID " + coordSystemID + " duplicated for " + tableName
								+ " in meta_coord");
						result = false;
					} else {
						ReportManager.correct(this, funcgenCon, "Coordinate system with ID " + coordSystemID + " for table " + tableName
								+ " has an entry in meta_coord");
					}

					// store in coordSystems map - create List if necessary
					List csList = (ArrayList) coordSystems.get(tableName);
					if (csList == null) {
						csList = new ArrayList();
					}
					csList.add(coordSystemID);
					coordSystems.put(tableName, csList);
				}

				funcgenRs.close();

			}

			// check that every meta_coord table entry refers to a coordinate system
			// that is used in a feature
			// if this isn't true it's not fatal but should be flagged
			String sql = "SELECT * FROM meta_coord";
			ResultSet rs = funcgenStatement.executeQuery(sql);
			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String csID = rs.getString("coord_system_id");
				logger.finest("Checking for coord_system_id " + csID + " in " + tableName);
				List featureCSs = (ArrayList) coordSystems.get(tableName);
				if (featureCSs != null && !featureCSs.contains(csID)) {
					ReportManager.problem(this, funcgenCon, "meta_coord has entry for coord_system ID " + csID + " in " + tableName
							+ " but this coordinate system is not actually used in " + tableName);
					result = false;
				}

			}

			rs.close();
			funcgenStatement.close();

			// check that there are no null max_length entries
			result &= checkNoNulls(funcgenCon, "meta_coord", "max_length");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	}

}
