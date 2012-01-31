package org.ensembl.healthcheck.testcase.funcgen;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Test;

public class CheckResultSetDBFileLink extends SingleDatabaseTestCase {

	public CheckResultSetDBFileLink() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		setTeamResponsible(Team.FUNCGEN);

		setDescription("Checks if the binary signal (col) files exist for relevant ResultSets\n" +
				"Also checks dbfile_data_root subdirs to see if there are still DISPLAYABLE or if they support a regualtory build\n" +
				"NOTE: RegulatorySets does something similar, but from the DataSet perspective\n " +
				"\tHence, consider those HC results first, before fixing these!");
		
		setPriority(Priority.AMBER);
		setEffect("Signal tracks will not display in the browser.");
		setFix("Re-create files or check file names manually.");		
		
	}

	private String getSupportedRegulatoryFeatureSet(Connection con, String subdirName){
		
		String regFsetSQL = "SELECT fs.name from result_set rs, supporting_set ss, data_set ds, " + 
		"supporting_set ss1, data_set ds1, feature_set fs WHERE " +
		"rs.result_set_id=ss.supporting_set_id and ss.type='result' and ss.data_set_id=ds.data_set_id " +
		"AND ds.feature_set_id=ss1.supporting_set_id and ss1.type='feature' and " + 
		"ss1.data_set_id=ds1.data_set_id and ds1.feature_set_id=fs.feature_set_id and " +
		"fs.type='regulatory' and fs.name not rlike '.*_v[0-9]+$' and rs.name='" + subdirName + "'";
		String regFset = null;
		
		try{
			Statement stmt = con.createStatement();
			ResultSet supportedRegFset = stmt.executeQuery(regFsetSQL);
		
			if((supportedRegFset != null) && supportedRegFset.next()){
				regFset = supportedRegFset.getString(1);	
				//doesn't matter if we get duplicate entries here based on
				//redundant rset names. catch reundant set before here
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
			
		return regFset;
	}
	
	public boolean run(DatabaseRegistryEntry dbre) {
		
		boolean result=true;
		Connection con = dbre.getConnection();
	
		try {
			Statement stmt = con.createStatement();
			
			HashMap<String, String> rSetDBLinks = new HashMap<String, String>();
			HashMap<String, String> rSetStates  = new HashMap<String, String>();
			HashMap<String, String> rSetRFSets  = new HashMap<String, String>();
			ArrayList removeableRsets           = new ArrayList();

			
			String rsetInfoSQL = "SELECT rs.name, dbf.path, s1.name from result_set rs left join dbfile_registry dbf "+ 
								"ON rs.result_set_id=dbf.table_id and dbf.table_name='result_set' left join " + 
								"(select s.table_id, sn.name from status s, status_name sn where " + 
								"s.status_name_id=sn.status_name_id and s.table_name='result_set' and sn.name='DISPLAYABLE') s1 " +
								"ON rs.result_set_id=s1.table_id";
		
			ResultSet rsetInfo = stmt.executeQuery(rsetInfoSQL);
			String rsetStatus, rsetPath, rsetName, regFset;
		
			while ((rsetInfo != null) && rsetInfo.next()) {
				rsetName   = rsetInfo.getString(1); 
				rsetPath   = rsetInfo.getString(2);
				rsetStatus = rsetInfo.getString(3);
				regFset = this.getSupportedRegulatoryFeatureSet(con, rsetName);
			
				//TEST IF WE HAVE SEEN A REDUNDANTLY NAMED RESULT_SET
				if(rSetDBLinks.containsKey(rsetName)){
					//bail out here or continue?
					//or could mark for deletion as we could have >2 
					ReportManager.problem(this, con, "Found redundant result_set naming:\t" + rsetName +
							"\nEither rectify in DB or updated HC to account for result_set unique key");
					return false; //bail out as results maybe unsafe
				}
				
				if( (rsetPath != null) || 
					(rsetStatus != null) ||
					(regFset != null) ){
								
					rSetDBLinks.put(rsetName, rsetPath);
					rSetStates.put(rsetName, rsetStatus);
					rSetRFSets.put(rsetName, regFset);
				}
				else{
					removeableRsets.add(rsetName);				
					ReportManager.info(this, con, 
							"Found 'removeable' result_set (i.e. not DISPLAYABLE, in build or has dbfile_registry):\t" + rsetName);	
				}
				
			}

			int numRsets = rSetDBLinks.size();
			
			//Get Base Folder
			ResultSet rsetDBDataRoot = stmt.executeQuery("SELECT meta_value from meta where meta_key='dbfile.data_root'");
			
			if((rsetDBDataRoot != null) && rsetDBDataRoot.next()){
				String root_dir  = rsetDBDataRoot.getString(1);
						
				//TEST EXISTING DIRECTORIES ARE RESULT SETS
				File result_feature_dir_f = new File(root_dir + "/result_feature");
				
				if(result_feature_dir_f.exists()){
					
					String[] subDirs = result_feature_dir_f.list();
					String rsetSQL;
					
					for(int i=0; i<subDirs.length; i++){
						rsetSQL = "SELECT result_set_id from result_set where name='" + subDirs[i] + "'";
						ResultSet subdirRsetIDs = stmt.executeQuery(rsetSQL);
						
						if((rsetDBDataRoot != null) && subdirRsetIDs.next()){
							String rsetID         = subdirRsetIDs.getString(1);
							logger.fine("Found result_feature subdir:\t" + subDirs[i] + " with rset id\t" + rsetID);

							if(subdirRsetIDs.next()){
								ReportManager.problem(this, con, "Cannot find unique result_set for subdir:\t" + subDirs[i] +
										".\nCheck manually or update HC");
								result = false;
							}
							
							//CATCH SUBDIRS WHICH FOR RESULT_SETS WITHOUT DBFILE_REGISTRY/DISPLAYABLE ENTRY OR IN BUILD
							if(removeableRsets.contains(subDirs[i])){
								ReportManager.info(this, con, "Found result_feature subdir for 'removeable' result_set " +
										"(i.e. not DISPLAYABLE, in build or has dbfile_registry):\t" + subDirs[i]);	
							}
						}
						else{
							ReportManager.problem(this, con, "Cannot find result_set entry for:\t" +
									root_dir + "/result_feature/"+ subDirs[i]);
							result = false;
						}
					}
				}
				else{
					ReportManager.problem(this, con, "Cannot test if result_set dirs are valid as parent directory does not exist:\t" + 
							root_dir + "/result_feature");
					result = false;
					//Don't return here as rsetPaths in DB may now be pointing to as different path
				}
					
									
				if(numRsets == 0){
					ReportManager.problem(this, con, "dbfile_root is defined in the meta table but found no result_sets can be found");
					result = false;					//Could return here?
				
				}
				else{ // NOW CHECK EXISTING RESULT SETS
					File root_dir_f = new File(root_dir);
														
					if(root_dir_f.exists()){
						ArrayList problemLinks  = new ArrayList();
						ArrayList problemStates = new ArrayList();
						ArrayList problemRFsets = new ArrayList();
						Iterator<String> dbLinkIt = rSetDBLinks.keySet().iterator();
						Object tmpObject;
						
						while(dbLinkIt.hasNext()){
							rsetName   = dbLinkIt.next().toString();
							//toString on null was failing silently here!
							//rsetPath   = ( (tmpObject = rSetDBLinks.get(rsetName)) == null) ? null : tmpObject.toString();
							//rsetStatus = ( (tmpObject = rSetStates.get(rsetName)) == null) ? null : tmpObject.toString();
							//regFset    = ( (tmpObject = rSetRFSets.get(rsetName)) == null) ? null : tmpObject.toString();
							//quicker but more verbose to integrate below
							//usage of tmpObject assignment in test is a little obfuscated
							
							// CHECK DISPLAYABLE AND DBFILE_REGISTRY ENTRIES
							if( (tmpObject = rSetDBLinks.get(rsetName)) == null ){
								regFset = null;
								problemRFsets.add("Found result_set which does not appear to " +
										"support the RegulatoryBuild:\t" + rsetName);
							}else{
								regFset = tmpObject.toString();
							}
											
							if( (tmpObject = rSetStates.get(rsetName)) == null ){
								rsetStatus = null;
							
								problemStates.add(rsetName + " is not DISPLAYABLE");								
							}else{
								rsetStatus =tmpObject.toString();
							}
					
							if( (tmpObject = rSetDBLinks.get(rsetName)) == null ){
								rsetPath = null;
								problemLinks.add("result_set " + rsetName + " does not have a dbfile_registry entry");
							}
							else{// NOW TEST COL FILES
								String rSetFinalPath = root_dir + tmpObject.toString();
								File rsetFolder = new File(rSetFinalPath);
							
								if(rsetFolder.exists()){
									String[] windows = {"30","65","130","260","450","648","950","1296"}; 
								
									for(int i=0;i<windows.length;i++){
										String rsetWindowFileName = rSetFinalPath + "/result_features." + rsetName + "." + windows[i] + ".col";
										File rsetWindowFile = new File(rsetWindowFileName);
									
										if(rsetWindowFile.exists()){
											if(rsetWindowFile.length() == 0){
												problemLinks.add(rsetWindowFileName + " seems empty for set " + rsetName);
											}
										} else {
											problemLinks.add(rsetWindowFileName + " does not seem to exist for set " + rsetName);
										}
									}
																
								} else {
									problemLinks.add(rSetFinalPath + " does not seem to be valid for set " + rsetName);
								}
							}
						}

						int MAX_REPORT=5; //Only out 5 problems by default
						
						//Handle all error types separately
						int numProbs = problemLinks.size();
															
						if(numProbs != 0){
							ReportManager.problem(this, con, "Found " + numProbs + " result_set file/path omissions");
							result = false;
												
							for(int i=0; i<numProbs; i++){
																	
								if(i >= MAX_REPORT){
									//Both these seem to report even with when restricting to -output problem?
									ReportManager.info(this, con, problemLinks.get(i).toString());
									//logger.info(problemLinks.get(problemIt.next()));
								}
								else{
									ReportManager.problem(this, con, problemLinks.get(i).toString());
								}	
							}
						}
						
						numProbs = problemStates.size();
						
						if(numProbs != 0){
							ReportManager.problem(this, con, numProbs + " result sets do not seem to be DISPLAYABLE");
							result = false;
						
							for(int i=0; i<numProbs; i++){
																		
								if(i >= MAX_REPORT){
									//Both these seem to report even with when restricting to -output problem?
									ReportManager.info(this, con, problemStates.get(i).toString());
									//logger.info(problemLinks.get(problemIt.next()));
								}
								else{
									ReportManager.problem(this, con, problemStates.get(i).toString());
								}	
							}	
						}
						
						numProbs = problemRFsets.size();
						
						if(numProbs != 0){
							ReportManager.problem(this, con, numProbs + " result sets do not appear to support a regulatory build");
							result = false;
						
							for(int i=0; i<numProbs; i++){
																	
								if(i >= MAX_REPORT){
									//Both these seem to report even with when restricting to -output problem?
									ReportManager.info(this, con, problemRFsets.get(i).toString());
									//logger.info(problemLinks.get(problemIt.next()));
								}
								else{
									ReportManager.problem(this, con, problemRFsets.get(i).toString());
								}	
							}	
						}
					} 
					else {
						ReportManager.problem(this, con, "Found " + numRsets + " result_sets but " + 
								"dbfile.data_root does not seem to be valid:\t" + root_dir);
						result = false; //could return here?
					}
				}
			} else {
				
				if(numRsets == 0){
					//could sanity check we don't have a build here?
					ReportManager.info(this, con, "Found no result_sets or dbfile.data_root");
				}
				else{				
					ReportManager.problem(this, con, "Found " + numRsets + 
							"result_sets but no dbfile.data_root meta key");
					result = false; 	//could return here?
				}
			}				
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return result;
	}
}
