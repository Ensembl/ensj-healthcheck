/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class FeaturePosition extends SingleDatabaseTestCase {

	
	/**
	 * Create a new instance
	 */
	public FeaturePosition() {
		addToGroup("pre_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		
		setTeamResponsible(Team.FUNCGEN);
		setDescription("Checks if features lie within bounds of seq_region i.e. start !=0 and end <= seq_region length.");
		setPriority(Priority.AMBER);
		setEffect("Low quality features will be included which maybe the result of reads mapping to repeat regions at end of seq_regions.");
		setFix("Fix:  See DELETE SQL. These should have been filtered within the pipeline!");
	}
	
	
	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
		
		//Do we really need these removes?
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);

	}
	
	
	/**
	 * Run the test.
	 * We will check if all the motif features in a regulatory feature contain all
	 *  the motif features associated to the annotated features associated to the regulatory feature
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {
		boolean     result = true;
		Connection  efgCon = dbre.getConnection();
		String schemaBuild = dbre.getSchemaVersion() + "_" + dbre.getGeneBuildVersion();
		String  coreDBName = dbre.getSpecies() + "_core_" + schemaBuild;
		DatabaseRegistryEntry coreDbre = getDatabaseRegistryEntryByPattern(coreDBName);
		
		if (coreDbre == null){
			ReportManager.problem(this, efgCon, "Could not access default core DB:\t" + coreDBName);
			return false;	
		}		
				
		//Get existing distinct seq_region_id and their limits from the core DB
		//This needs to be restricted to the schemaBuild otherwise we may be using the wrong seq_region
		//No need for schemaBuild restriction here due to reg_feat join
		//Could still have problems if we have archived sets on older assembly. Unlikely to happen
		//Need to get name here so we can safely do sr join in feature count
		String sql = "select sr.core_seq_region_id, sr.name, sr.seq_region_id from seq_region sr, regulatory_feature rf where rf.seq_region_id=sr.seq_region_id group by sr.name";
		HashMap<String, String> coreSeqRegionIDName    = new HashMap<String, String>(); 
		HashMap<String, String> nameFuncgenSeqRegionID = new HashMap<String, String>(); 
		
 
		try {
			ResultSet rs = efgCon.createStatement().executeQuery(sql);

			while (rs.next()){
				coreSeqRegionIDName.put(rs.getString(1), rs.getString(2));
				nameFuncgenSeqRegionID.put(rs.getString(2), rs.getString(3));
			}
		}
		catch (SQLException se) {	
			se.printStackTrace();
			return false;
		}
		
					Connection coreCon = coreDbre.getConnection();
		HashMap<String, String> seqRegionLen      = new HashMap<String, String>(); 
			
		for (Iterator<String> iter = coreSeqRegionIDName.keySet().iterator(); iter.hasNext();) {
			String coreSrID = (String) iter.next();
			seqRegionLen.put(coreSrID, DBUtils.getRowColumnValue(coreCon, "select length from seq_region where seq_region_id=" + coreSrID) );
		}
		  
    //Shouldn't these be defined somewhere more generic?
    String [] featureTables = {"annotated_feature", "regulatory_feature", 
                               "external_feature",  "segmentation_feature"};
    
    for(String fTable : featureTables){
        String problemString = "";
        String usefulSQL     = "";
        String deleteSQL     = "";
        int    totalFeatures = 0;
        Iterator<String> it = seqRegionLen.keySet().iterator();

        while(it.hasNext()){
            String coreRegionId    = it.next();
            String srName          = coreSeqRegionIDName.get(coreRegionId);
            String funcgenRegionID = nameFuncgenSeqRegionID.get(srName);
            String srLength        = seqRegionLen.get(coreRegionId);
            //Using efg sr_id removes need for use of schema_build and sr join
     
            //start = 0 as is unsigned i.e. never <0
            //only need the bound calc here, as it will be more extreme or equal to seq_region loci

            if(fTable.equals("regulatory_feature")){
                sql = "select count(" + fTable + "_id) from " + fTable + " WHERE seq_region_id=" + 
                    funcgenRegionID + " AND  ((seq_region_start - bound_start_length) = 0 " +
                    "OR (seq_region_end + bound_end_length) > " + srLength + ")";
            }
            else{
                sql = "select count(" + fTable + "_id) from " + fTable + " WHERE seq_region_id=" + 
                    funcgenRegionID + " AND  (seq_region_start = 0 OR seq_region_end > " + srLength + ")";  
            }



            Integer featCount = DBUtils.getRowCount(efgCon, sql); 	
            totalFeatures    += featCount;

            //This is already being 'caught' higher in the stack, but no exit
            //but still shows as 'PASSED' as result is true by default!
            //featCount is -1 not null if sql failed

            if(featCount == -1){
                ReportManager.problem(this, efgCon, "SQL Failed:\t" + sql);
                return false;
            }
			
            if(featCount > 0){
                //Delete as we never trust peaks over ends of sequencable regions, as they are likely
                //the start of long ranging repeats where alignments stack up erroneously
          
                if(fTable.equals("regulatory_feature")){
                    deleteSQL += "DELETE ra, rf from regulatory_feature rf join " + 
                        "regulatory_attribute ra using (regulatory_feature_id) WHERE seq_region_id=" + 
                        funcgenRegionID + " AND  ((seq_region_start - bound_start_length)  = 0 " +
                        "OR  (seq_region_end + bound_end_length)  > " + srLength + ");\n" ;
                }
                else{
                    deleteSQL += "DELETE from " + fTable + " WHERE seq_region_id=" + funcgenRegionID + 
                        " AND  (seq_region_start = 0 OR seq_region_end  > " + srLength + ");\n";
                }
            
                usefulSQL += sql + ";\n";			
                problemString = problemString + " " + srName + "(" + featCount + ")";
                result =  false;
            }
        }

        if(! problemString.isEmpty() ){
        
            ReportManager.problem
                (
                 this, efgCon, 
                 "Found " + totalFeatures + " " + fTable + "s exceeding seq_region bounds:\t" + problemString +
                 "\nUSEFUL SQL:\n" + usefulSQL + "\nDELETE SQL:\n" + deleteSQL
                 );  
        }
    }
		return result;
	}

}
