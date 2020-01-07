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
package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Repair;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * @author mnuhn
 *
 */
public class DuplicateProbeFeatures extends SingleDatabaseTestCase  implements Repair {

	final private int maxUsefulSqlStatements = 10;

	protected Connection con;
	
	public DuplicateProbeFeatures() {
		setTeamResponsible(Team.FUNCGEN);
		setDescription("Tests for duplicate probe features and can remove them.");
	}

	public void types() {
		appliesToType(DatabaseType.FUNCGEN);
	}
	
	protected List<String> usefulSql() throws SQLException {
		
		ResultSet rs = getDuplicates();
		List<String> sqlStatements = new ArrayList<String>(); 
		while (rs.next() && sqlStatements.size()<maxUsefulSqlStatements) {
			String commaSeparatedIdList = rs.getString("ids");
			String[] ids = commaSeparatedIdList.split(",");
			StringBuffer idsToDelete = new StringBuffer();
			int idsInCurrentStatement = 0;
			// Don't skip the first in the list, we want to show all.
			for(int i=0; i<ids.length; i++) {
				
				idsToDelete.append(ids[i]);
				idsInCurrentStatement++;
				
				if (idsInCurrentStatement>=10) {
					
					sqlStatements.add("select * from probe_feature where probe_feature_id in (" + idsToDelete + ");");
					idsToDelete = new StringBuffer();
					idsInCurrentStatement = 0;
					
				} else {
					// No comma after the last one.
					if (i+1<ids.length) {
						idsToDelete.append(", ");
					}
				}
			}
			if (idsToDelete.length()>0) {
				sqlStatements.add("select * from probe_feature where probe_feature_id in (" + idsToDelete + ");");
			}
		}
		return sqlStatements;
	}
	
	protected List<String> repairSql() throws SQLException {
		
		final int maxIdsInOneStatement = 10;
		
		ResultSet rs = getDuplicates();
		List<String> sqlStatements = new ArrayList<String>(); 
		while (rs.next()) {
			String commaSeparatedIdList = rs.getString("ids");
			String[] ids = commaSeparatedIdList.split(",");
			StringBuffer idsToDelete = new StringBuffer();
			int idsInCurrentStatement = 0;
			// Skip the first in the list, the others get deleted.
			for(int i=1; i<ids.length; i++) {
				
				idsToDelete.append(ids[i]);
				idsInCurrentStatement++;
				
				if (idsInCurrentStatement>=maxIdsInOneStatement) {
					
					sqlStatements.add("delete from probe_feature where probe_feature_id in (" + idsToDelete + ");");
					idsToDelete = new StringBuffer();
					idsInCurrentStatement = 0;
					
				} else {
					// No comma after the last one.
					if (i+1<ids.length) {
						idsToDelete.append(", ");
					}
				}
			}
			if (idsToDelete.length()>0) {
				sqlStatements.add("delete from probe_feature where probe_feature_id in (" + idsToDelete + ");");
			}
		}
		return sqlStatements;
	}
	
	protected ResultSet getDuplicates() throws SQLException {
		
		Statement stmt = con.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize((Integer.MIN_VALUE));
		
		// By default group_concat gets cut off after 1000 characters, we
		// don't want to loose any, so setting this to a higher value.
		//
		stmt.executeQuery("SET SESSION group_concat_max_len = 1000000");
		ResultSet rs = stmt.executeQuery(getDuplicateSql());
		return rs;
	}
	
	protected int getNumDuplicates() throws SQLException {
		
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(getNumDuplicatesSql());
		rs.next();
		int numDuplicates = rs.getInt("numDuplicates");
		return numDuplicates;
	}
	
	protected String getDuplicateSql() {
		return "SELECT count(probe_feature_id) num_occurrences, group_concat(cast(probe_feature_id as char)) ids, seq_region_id, seq_region_start, seq_region_end, probe_id, analysis_id, cigar_line "
				+ "FROM probe_feature "
				+ "group by seq_region_id, seq_region_start, seq_region_end, probe_id, analysis_id, cigar_line "
				+ "having num_occurrences > 1 order by num_occurrences desc";
	}
	
	protected String getNumDuplicatesSql() {
		return "select count(*) numDuplicates from (" + getDuplicateSql() + ") duplicate_list";
	}
	
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean testPassed;
		
		String sql = "SELECT count(*) as numberOfRows FROM probe_feature;";
		con = dbre.getConnection();
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			int numberOfRows = rs.getInt("numberOfRows");
			logger.info("There are " + numberOfRows + " rows in the probe_feature table.");
			
			int numDuplicates = getNumDuplicates();
	
			if (numDuplicates > 0) {
				
				numDuplicates = getNumDuplicates();
				
				String msg = "Has " + numDuplicates + " duplicated probe features.";
				logger.severe(msg);
				ReportManager.problem(this, con, msg);
				
				List<String >sqlStatements = usefulSql();
				
				logger.info("Useful sql:");
				Iterator<String> i = sqlStatements.iterator();
				while(i.hasNext()) {
					logger.info(i.next());
				}
				
				testPassed = false;
			} else {
				String msg = "No duplicate probe features found";
				logger.info(msg);
				ReportManager.correct(this, con, msg);
				testPassed = true;
			}
		} catch (SQLException e) {
			testPassed = false;
			e.printStackTrace();
			return testPassed;
		}
		return testPassed;
	}

	@Override
	public void repair(DatabaseRegistryEntry dbre) {
		showOrRepair(dbre, false);
	}

	@Override
	public void show(DatabaseRegistryEntry dbre) {
		showOrRepair(dbre, true);
	}
	
	public void showOrRepair(DatabaseRegistryEntry dbre, boolean onlyShow) {
		
		this.con = dbre.getConnection();
		List<String> repairSqlStatements;
		
		try {
			Statement stmt = con.createStatement();
			repairSqlStatements = repairSql();
			Iterator<String> iterator = repairSqlStatements.listIterator();
			while (iterator.hasNext()) {
				String sql = iterator.next();
				
				if (!onlyShow) {
					logger.info("Executing: " + sql);
					stmt.executeUpdate(sql);
				} else {
					logger.info("Showing only: " + sql);
				}
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
}
