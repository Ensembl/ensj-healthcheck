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

package org.ensembl.healthcheck.testcase.funcgen;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.ensembl.CoreDbNotFoundException;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.MissingMetaKeyException;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.util.DBUtils;

public class FeaturePosition extends AbstractCoreDatabaseUsingTestCase {
	
	public FeaturePosition() {	
		setTeamResponsible(Team.FUNCGEN);
		setDescription("Checks if features lie within bounds of seq_region i.e. start >=0 and end <= seq_region length.");
		setPriority(Priority.AMBER);
	}
	
	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		setAppliesToType(DatabaseType.FUNCGEN);
	}
	
	
	/**
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

        if (Pattern.matches("master_schema_funcgen_\\d+", dbre.getName())) {
            logger.fine("Skipping " + dbre.getName());
            return true;
        }

		boolean               result   = true;
		Connection            dbConnection   = dbre.getConnection();
		DatabaseRegistryEntry coreDbre;
		try {
			coreDbre = getCoreDb(dbre);
		} catch (MissingMetaKeyException e) {

			ReportManager.problem(this, dbre.getConnection(), e.getMessage());
			return false;
			
		} catch (CoreDbNotFoundException e) {

			ReportManager.problem(this, dbre.getConnection(), e.getMessage());
			return false;
			
		}
		
		logger.info("Using core database " + coreDbre.getName() + " " + coreDbre.getDatabaseServer().getDatabaseURL());
						
        String sql = "select seq_region_id, seq_region.name, length from seq_region join seq_region_attrib using (seq_region_id) join attrib_type using (attrib_type_id) where code=\"toplevel\"";
		HashMap<String, String> coreSeqRegionIDName = new HashMap<String, String>();
		HashMap<String, String> seqRegionIdToLength = new HashMap<String, String>();
 
		try {
			ResultSet rs = coreDbre.getConnection().createStatement().executeQuery(sql);

			while (rs.next()) {
				coreSeqRegionIDName.put( rs.getString(1), rs.getString(2) );
				seqRegionIdToLength.put( rs.getString(1), rs.getString(3) );
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
				  
	    String [] featureTables = {"peak", "regulatory_feature", "motif_feature",
	                               "external_feature",  "segmentation_feature", "mirna_target_feature"};
    
	    for(String featureTable : featureTables) {
	    	
	    	logger.info("Checking " + featureTable);
	    	
	        String problemString = "";
	        String usefulSQL     = "";
	        String updateSQL     = "";
	        int    totalNumberOfFailedFeatures = 0;
	        
	        Iterator<String> seqRegionIdIterator = seqRegionIdToLength.keySet().iterator();
	
	        while(seqRegionIdIterator.hasNext()) {
	        	
	            String seqRegionId     = seqRegionIdIterator.next();
	            String seqRegionName   = coreSeqRegionIDName.get(seqRegionId);
	            String seqRegionLength = seqRegionIdToLength.get(seqRegionId);
	     
	            if(featureTable.equals("regulatory_feature")) {
	                sql = "select count(" + featureTable + "_id) from " + featureTable + " WHERE seq_region_id=" + 
	                		seqRegionId + " AND  ((seq_region_start - bound_start_length) <= 0 " +
	                    "OR (seq_region_end + bound_end_length) > " + seqRegionLength + ")";
	            } else {
	                sql = "select count(" + featureTable + "_id) from " + featureTable + " WHERE seq_region_id=" + 
	                		seqRegionId + " AND  (seq_region_start <= 0 OR seq_region_end > " + seqRegionLength + ")";  
	            }
	
	            Integer numberOfFailedFeatures  = DBUtils.getRowCount(dbConnection, sql); 	
	            totalNumberOfFailedFeatures    += numberOfFailedFeatures;
	
	            //This is already being 'caught' higher in the stack, but no exit
	            //but still shows as 'PASSED' as result is true by default!
	            //featCount is -1 not null if sql failed
	
	            if(numberOfFailedFeatures == -1) {
	                ReportManager.problem(this, dbConnection, "SQL Failed:\t" + sql);
	                return false;
	            }
				
	            if(numberOfFailedFeatures > 0) {
	                updateSQL += "UPDATE " + featureTable + " set seq_region_end =" + seqRegionLength +  " WHERE seq_region_id=" + 
	                		seqRegionId + " AND seq_region_end  > " + seqRegionLength + ";\n";
	
	                updateSQL += "UPDATE " + featureTable + " set seq_region_start=1 WHERE seq_region_id=" + 
	                		seqRegionId + " AND seq_region_start = 0;\n";
	            
	                usefulSQL += sql + ";\n";			
	                problemString = problemString + " " + seqRegionName + "(" + numberOfFailedFeatures + ")";
	                result =  false;
	            }
	        }
	
	        if(! problemString.isEmpty() ) {
	            ReportManager.problem(this, dbConnection, 
	                 "Found " + totalNumberOfFailedFeatures + " " + featureTable + "s exceeding seq_region bounds:\t" + problemString +
	                 "\nUSEFUL SQL:\n" + usefulSQL + "\nUPDATE SQL:\n" + updateSQL);
	            result = false;  
	        }
	    }
		return result;
	}
}
