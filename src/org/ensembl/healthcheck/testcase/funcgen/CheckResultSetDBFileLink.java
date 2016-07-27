/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016] EMBL-European Bioinformatics Institute
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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

public class CheckResultSetDBFileLink extends SingleDatabaseTestCase {
	
	String[] windows = {"30","65","130","260","450","648","950","1296"}; 
	
	protected String[] windowSizes() {	return windows; }
	
    public CheckResultSetDBFileLink() {
        addToGroup("post_regulatorybuild");
        addToGroup("funcgen");//do we need this group and the funcgen-release group?
        addToGroup("funcgen-release");
        setTeamResponsible(Team.FUNCGEN);

        setDescription("Checks if the binary signal (col) files exist for relevant ResultSets\n" +
                       "Also checks dbfile_data_root subdirs to see if there are still DISPLAYABLE or if they support a regualtory build\n");
		
		
        setPriority(Priority.AMBER);
        setEffect("Signal tracks will not display in the browser.\n" + 
                  "NOTE: RegulatorySets does something similar, but from the DataSet perspective\n " +
                  "\tHence, consider those HC results first, before fixing these!");
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
            Statement stmt             = con.createStatement();
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
            int MAX_REPORT=50; //Only out 50 problems by default
            HashMap<String, String> rSetDBLinks  = new HashMap<String, String>();
            HashMap<String, String> rSetFClasses = new HashMap<String, String>();
            HashMap<String, String> rSetStates   = new HashMap<String, String>();
            HashMap<String, String> rSetRFSets   = new HashMap<String, String>();
            ArrayList<String> removeableRsets    = new ArrayList<String>();

            String rsetInfoSQL = "SELECT rs.name, dbf.path, s1.name, rs.feature_class from result_set rs left join dbfile_registry dbf "+ 
                "ON rs.result_set_id=dbf.table_id and dbf.table_name='result_set' left join " + 
                "(select s.table_id, sn.name from status s, status_name sn where " + 
                "s.status_name_id=sn.status_name_id and s.table_name='result_set' and sn.name='DISPLAYABLE') s1 " +
                "ON rs.result_set_id=s1.table_id";
		
            ResultSet rsetInfo = stmt.executeQuery(rsetInfoSQL);
            String rsetStatus, rsetPath, rsetName, regFset, rsetFClass;
           //String infoString = "";
			
            while ((rsetInfo != null) && rsetInfo.next()) {
                rsetName   = rsetInfo.getString(1); 
                rsetPath   = rsetInfo.getString(2);
                rsetStatus = rsetInfo.getString(3);
                rsetFClass = rsetInfo.getString(4);
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
                    rSetFClasses.put(rsetName, rsetFClass);
                    rSetDBLinks.put(rsetName, rsetPath);
                    rSetStates.put(rsetName, rsetStatus);
                    rSetRFSets.put(rsetName, regFset);
                    
                }
                else{
                    removeableRsets.add(rsetName);	
                }
            }

            if(removeableRsets.size() > 0){
                //Should this be info instead?
                ReportManager.problem(this, con, "Found " + removeableRsets.size() + 
                                      " 'removeable' result_sets i.e. not DISPLAYABLE, not in build and has no dbfile_registry.path:\n\t" +
                                      StringUtils.join(removeableRsets, "\n\t") + "\n");
                result = false;
            }
			
            int numRsets = rSetDBLinks.size();
			
            //Get Base Folder
            ResultSet rsetDBDataRoot = stmt.executeQuery("SELECT meta_value from meta where meta_key='dbfile.data_root'");
            String    problemString; 	//For easier interpretation/reporting, build 1 problem string per result_set/subDir, 
			
            if((rsetDBDataRoot != null) && rsetDBDataRoot.next()){
                String root_dir  = rsetDBDataRoot.getString(1);
                //rsetDBDataRoot.close(); // don't need this anymore as reusing stmt will close this

                ReportManager.problem(this, con, "Found dbfile.data_root meta key. Need to remove this once all the other failures have been resolved");
                result = false;
                //This can be removed once we resolve the dbfile.data_root issue. /nfs/ensnfs-dev/staging/nNeed adding to config
                //and add species and assembly

                //Get distinct result_set feature_class values
                String    fclassSQL    = "SELECT distinct(feature_class) from result_set";  
                ResultSet rsetFclasses = stmt.executeQuery(fclassSQL);
         
                //rsetFclasses and subdirRsetIDs ResultSets interleved here from same stmt! 
                //This may cause issues, but seemingly not the issue we are experiencing
                //A ResultSet object is automatically closed when the Statement object that generated it is closed, re-executed, 
                //or used to retrieve the next result from a sequence of multiple results. << counts or ResultSets, this is very rare and can probably be ignored
                //This is only applicable to using execute, then a separate get method on Statment e.g. getResultSet

                while ((rsetFclasses != null) && rsetFclasses.next()) {
                    String featureClass   = rsetFclasses.getString(1); 
          
                    //TEST EXISTING DIRECTORIES ARE RESULT SETS
                    String resultSetPath  = root_dir + "/" +  featureClass + "_feature";
                    File   resultSetPathF = new File(resultSetPath);
			
                    if(resultSetPathF.exists() &&
                       resultSetPathF.isDirectory()){
                        String[] subDirs = resultSetPathF.list();
                        String rsetSQL;
                        ArrayList<String> subdirProblems = new ArrayList<String>();
                        Statement stmt1          = con.createStatement();
                        boolean seenREADME = false;

                        for(String subDir : subDirs){
                        	problemString = "";

                            //Check is not a soft link
                        	//as these are to support archives, and really need testing in another HC
                            boolean isLink = true;
                            String fullPath = resultSetPath + "/" + subDir;
                            try { isLink = isSymLink(fullPath); } catch(IOException i){ i.printStackTrace(); }
                      
                            if(subDir.equals("README") ){
                                seenREADME = true;
                                continue;
                            }
                            else if(isLink){
                            	continue;                            	
                            }

                            rsetSQL = "SELECT result_set_id from result_set where name='" + subDir + "'";
                            ResultSet subdirRsetIDs = stmt1.executeQuery(rsetSQL);
						
                            if((root_dir != null) && subdirRsetIDs.next()){
                                //String rsetID         = subdirRsetIDs.getString(1);
                                //logger.fine("Found result_feature subdir:\t" + subDirs[i] + " with rset id\t" + rsetID);

                                if(subdirRsetIDs.next()){
                                    problemString += "\tCannot find unique result_set. Check manually or update HC\n";
                                }
							
                                //CATCH SUBDIRS WHICH FOR RESULT_SETS WITHOUT DBFILE_REGISTRY/DISPLAYABLE ENTRY OR IN BUILD
                                if(removeableRsets.contains(subDir)){
                                    problemString += "\tAppears to be 'removeable' i.e. not DISPLAYABLE, not in build and has no dbfile_registry.path.\n";
                                }
                            }
                            else{
                                problemString += "\tCannot find result_set.\n";
                            }
						
                            if(! problemString.equals("")){
                                subdirProblems.add(subDir + " " + featureClass + "_feature subdir has problems:\n" + problemString);
                            }
                        }
					
                        if(seenREADME == false){
                            ReportManager.problem(this, con, "No README file present in:\t" + resultSetPath);
                            result = false;
                        }

					
                        int numProbs = subdirProblems.size();
					
                        if(numProbs != 0){
                            ReportManager.problem(this, con, "Found " + numProbs + " " + featureClass + "_feature subdirs with problems (use -output info for all).");
                            result = false;
											
                            for(int i=0; i<numProbs; i++){
																
                                if(i >= MAX_REPORT){
                                    //Both these seem to report even with when restricting to -output problem?
                                    ReportManager.info(this, con, subdirProblems.get(i).toString());
                                }
                                else{
                                    ReportManager.problem(this, con, subdirProblems.get(i).toString());
                                }	
                            }
                        }
                        else{
                            ReportManager.info(this, con, "Found 0 " + featureClass + "_feature subdirs with problems.");					
                        }
					
                    }
                    else{
                        ReportManager.problem(this, con, 
                                              "Cannot test if result_set dirs are valid as path does not exist or is not a directory:\t" + 
                                              resultSetPath);
                        result = false;
                        //Don't return here as rsetPaths in DB may now be pointing to as different path
                    }
				}

                if(numRsets == 0){
                    ReportManager.problem(this, con, "dbfile_root is defined in the meta table but found no result_sets can be found");
                    result = false;					//Could return here?
                    
                }
                else{ // NOW CHECK EXISTING RESULT SETS
                File root_dir_f = new File(root_dir);
										
                    if(root_dir_f.exists()){
                        ArrayList<String> rsetProblems = new ArrayList<String>();
                        Iterator<String> dbLinkIt = rSetDBLinks.keySet().iterator();
                        //Here we are iterating over all the rSetDBLinks twice
                        //once for each FeatureClass
                        //but we get the rsetFClass below
                        Object tmpObject;
						
                        while(dbLinkIt.hasNext()){
                            rsetName   = dbLinkIt.next().toString();

                            //Need to bring in the class here too

                            problemString = "";
                            //toString on null was failing silently here!
                            rsetPath   = ( (tmpObject = rSetDBLinks.get(rsetName)) == null) ? "NO DBFILE_REGISTRY PATH" : tmpObject.toString();
                            rsetStatus = ( (tmpObject = rSetStates.get(rsetName)) == null) ? "NOT DISPLAYABLE" : tmpObject.toString();
                            regFset    = ( (tmpObject = rSetRFSets.get(rsetName)) == null) ? "NOT IN BUILD" : tmpObject.toString();
                            rsetFClass = rSetFClasses.get(rsetName); //Will always be defined
													
                            //Report all these together for easier interpretation	
                            if( ( rsetPath.equals("NO DBFILE_REGISTRY PATH") ||
                                  rsetStatus.equals("NOT DISPLAYABLE") ||
                                  regFset.equals("NOT IN BUILD") ) &&
                                rsetFClass.equals("result") ){
								
                                problemString += "\tdbfile_registry.path:\t" + rsetPath + "\n\t" +
                                    "IS " + rsetStatus + "\n\t" + "Supports:\t" + regFset + "\n";
                            }
                            else if( (rsetPath.equals("NO DBFILE_REGISTRY PATH") ||
                                      rsetStatus.equals("NOT DISPLAYABLE") ) &&
                                     rsetFClass.equals("dna_methylation") ){
                                problemString += "\tdbfile_registry.path:\t" + rsetPath + "\n\t" +
                                    "IS " + rsetStatus + "\n";
                            }

                            if(! rsetPath.equals("NO DBFILE_REGISTRY PATH")){// NOW TEST COL FILES
                                String rSetFinalPath = root_dir + rsetPath;
                                File rsetFileFolder = new File(rSetFinalPath);
															
                                if(rsetFileFolder.exists()){

                                    if(rsetFClass.equals("result") ){
                                            
                                        //String[] windows = {"30","65","130","260","450","648","950","1296"}; 
                                    	//for(int i=0;i<windows.length;i++){
                                        
                                    	String[] windowSizes= windowSizes();
                                    	
                                    	for (String wSize : windowSizes) {
                                            String rsetWindowFileName = rSetFinalPath + "/result_features." + rsetName + "." + wSize + ".col";
                                            File rsetWindowFile = new File(rsetWindowFileName);
                                                
                                            if(rsetWindowFile.exists()){
                                                if(rsetWindowFile.length() == 0){
                                                    problemString += "\tEmpty file:\t" + rsetWindowFileName + "\n";
                                                }
                                            } else {
                                                problemString += "\tFile does not exist:\t" + rsetWindowFileName + "\n";
                                            }
                                        }
                                    }
																
                                } else {
                                    problemString += "\tdbfile_registry.path does not exist:\t" + rSetFinalPath + "\n";
                                }
                            }
													
                            if(! problemString.equals("")){
                                rsetProblems.add(rsetName + " ResultSet has problems:\n" + problemString);
                            }
                        }

						
                        int numProbs = rsetProblems.size();
                        
                        if(numProbs != 0){
                            ReportManager.problem(this, con, "Found " + numProbs + " ResultSets with problems.\n");
                            result = false;
												
                            for(int i=0; i<numProbs; i++){
																	
                                if(i >= MAX_REPORT){
                                    //Both these seem to report even with when restricting to -output problem?
                                    ReportManager.info(this, con, rsetProblems.get(i).toString());
                                }
                                else{
                                    ReportManager.problem(this, con, rsetProblems.get(i).toString());
                                }	
                            }
                        }
                        else{
                            ReportManager.info(this, con, "Found 0 ResultSets with problems.");					
                        }
                    } 
                    else {
                        ReportManager.problem(this, con, "Found " + numRsets + " result_sets but " + 
                                              "dbfile.data_root does not seem to be valid:\t" + root_dir);
                        result = false; //could return here?
                    }
                } // END OF EXISTING RESULT SET CHECK
            }
            else { //no rsetDBDataRoot
                
                if(numRsets == 0){
                    //could sanity check we don't have a build here?
                    ReportManager.info(this, con, "Found no result_sets or dbfile.data_root");
                }
                else{				
                    ReportManager.problem(this, con, "Found " + numRsets + 
                                          "result_sets but no dbfile.data_root meta key. Please add a  dbfile.data_root meta key to perform this HC");
                    result = false; 	//could return here?
                }
            }
        		
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return result;
    }
    
    
    // Need to push these to some core File utils class
    
    public static boolean isSymLink(File file) throws IOException {
    	
    	if (file == null) throw new NullPointerException("File argument cannot be null");
    	
    	File cfile;
    	  	
	  	if (file.getParent() == null) {
	  		cfile = file;
	  	} else {
	  		File canonDir = file.getParentFile().getCanonicalFile();
	  		cfile = new File(canonDir, file.getName());
	  	}

	  	return ! cfile.getCanonicalFile().equals( cfile.getAbsoluteFile() );
  	}
    
    public static boolean isSymLink(String path) throws IOException {
    	if (path == null) throw new NullPointerException("Path argument cannot be null");
    	File pathFile = new File(path);
    	return isSymLink(pathFile);
  	}
}
