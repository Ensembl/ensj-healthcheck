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
import java.util.Map;
import java.util.Iterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.ArrayList;
import java.sql.Array;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class RegulatorySets extends SingleDatabaseTestCase {


	/**
	 * Create a new instance
	 */
	public RegulatorySets() {
		addToGroup("post_regulatorybuild");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");

		setTeamResponsible(Team.FUNCGEN);

		setDescription("Checks if sets have appropriate associations and statues entries");
		setPriority(Priority.AMBER);
		setEffect("Displays may fail or omit some data");
		setFix("Run update_DB_for_release (in fix mode) or run suggested USEFUL SQL");
	}


	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		//Do we really need these removes?
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);
	}


	/**
	 * Run the test.
	 * We will check for all sets associated with regulatory build have appropriate meta_keys, supporting_sets
	 * and status entries
	 *
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 *
	 */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;
        Connection efgCon = dbre.getConnection();
        //Test for other meta keys here: reg build version?

        HashMap fsetInfo    = new HashMap<String, HashMap>();
        String[] metaKeys  = {"feature_set_ids", "focus_feature_set_ids", "feature_type_ids"};

        try {
            //This account for archived sets
            int regSetCount = DBUtils.getRowCount(efgCon, "SELECT distinct(cell_type_id) from feature_set where type='regulatory'");


            String[] cellTypes = new String[regSetCount];
            int count = 0;

            Statement stmt = efgCon.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT feature_set_id, name from feature_set where type='regulatory'");

            while (rs.next()) {
                HashMap fsInfo    = new HashMap<String, HashMap>();
                String fsetName = rs.getString("name");

                // TEST FOR ARCHIVED SETS
                //Need to do this for data_set table too?
                if(fsetName.matches(".*_v[0-9]+$")) {
                    ReportManager.problem(this, efgCon, "Found archived regulatory FeatureSet:\n" +
                                          fsetName + "\nUse rollback_experiment.pl to remove these");
                    result = false;
                    continue;
                }

                fsInfo.put("name", fsetName);
                String cellType = fsetName.replaceAll("RegulatoryFeatures:", "");
                fsInfo.put("cell_type", cellType);
                cellTypes[count] = cellType;

                // GET META_KEYS
                for (int i=0; i < metaKeys.length; i++) {
                    String fullKey = "regbuild." + cellType + "." + metaKeys[i];
                    Statement stmtMeta = efgCon.createStatement();
                    ResultSet rsMeta = stmtMeta.executeQuery("SELECT string from regbuild_string where name='" +
                                                             fullKey + "'");

                    if(! rsMeta.next()){
                        ReportManager.problem(this, efgCon, "Found absent regbuild_string:\t" +
                                              fullKey);
                        result = false;
                    }else{
                        fsInfo.put(metaKeys[i], rsMeta.getString("string"));
                    }
                }

                fsetInfo.put(rs.getString("feature_set_id"), fsInfo);
                ++count;
            }


            // TEST FOR OLD/ANOMALOUS META_KEYS
            stmt = efgCon.createStatement();
            rs = stmt.executeQuery("SELECT name from regbuild_string where name like 'regbuild%ids%'");

            while (rs.next()) {
                String metaKey = rs.getString("name");

                if(metaKey.matches(".*_v[0-9]+$") ){
                    ReportManager.problem(this, efgCon, "Found archived regbuild_string:\t" +
                                          metaKey);
                    result = false;
                    continue;
                }

                String metaTmp   = metaKey.replaceAll("regbuild.", "");
                String cellType  = metaTmp.replaceAll("\\..*_ids", ""); //will this work without compiling a pattern
                String keyType   = metaTmp.replaceAll(cellType + ".*\\.", ""); //Will this escape properly?

                if(! Arrays.asList(cellTypes).contains(cellType)){
                    ReportManager.problem(this, efgCon, "Found cell type regbuild_string which is not represented as a " +
                                          "FeatureSet:\t" + metaKey);
                    result = false;
                }
            }


            //DEAL WITH EACH BUILD SEPARATELY
            //We already do most of this in HealthChecker.pm!
            Iterator rfsetIt = fsetInfo.keySet().iterator();

            while(rfsetIt.hasNext()){ //Regulatory FeatureSets
                String fsetID = (String) rfsetIt.next();
                HashMap fsInfo = (HashMap) fsetInfo.get(fsetID);

                // Check RegFeat data_set
                stmt = efgCon.createStatement();
                rs = stmt.executeQuery("SELECT name, data_set_id from data_set where feature_set_id=" + fsetID);

                if(! rs.next()){
                    ReportManager.problem(this, efgCon, "Found absent data_set:\t" + fsInfo.get("name"));
                    result = false;
                    continue; //while(fsetIt.hasNext())
                }

                // Set names matches?
                if(! fsInfo.get("name").equals(rs.getString("name"))){
                    ReportManager.problem(this, efgCon, "Found name mismatch between FeatureSet " +
                                          fsInfo.get("name") + " and linked DataSet " + rs.getString("name"));
                    result = false;
                }

                String dsetID = rs.getString("data_set_id");


                //GET ALL SUPPORTING SET INFO
                stmt         = efgCon.createStatement();
                ResultSet rsDsetSupport = stmt.executeQuery("SELECT ss.supporting_set_id as 'ss_feature_set_id', " +
                                                            "fs.feature_set_id as 'fs_feature_set_id', ds.data_set_id as 'ds_data_set_id', ss1.supporting_set_id as 'ss_result_set_id'" +
                                                            "from supporting_set ss left join (feature_set fs left join " +
                                                            "(data_set ds left join supporting_set ss1 on ds.data_set_id=ss1.data_set_id and ss1.type='result') " +
                                                            "on fs.feature_set_id=ds.feature_set_id) on fs.feature_set_id=ss.supporting_set_id " +
                                                            "where ss.type='feature' and ss.data_set_id=" + dsetID);

                count = 0;
                String[] metaFsetIDs       = ((String) fsInfo.get("feature_set_ids")).split(",\\s*");
                String   metaFtypeIDString = (String) fsInfo.get("feature_type_ids");
                String[] metaFtypeIDs      = metaFtypeIDString.split(",\\s*");

                //String[] ssFsetIDs    = (String[])((Array) rsDsetSupport.getArray("ss_feature_set_id")).getArray();
                //String[] fsFsetIDs    = (String[])((Array) rsDsetSupport.getArray("fs_feature_set_id")).getArray();
                //String[] dsDsetIDs    = (String[])((Array) rsDsetSupport.getArray("ds_data_set_id")).getArray();
                //String[] ssRsetIDs    = (String[])((Array) rsDsetSupport.getArray("ss_result_set_id")).getArray();


                ArrayList<String> ssFsetIDs = new ArrayList<String>();
                ArrayList<String> fsFsetIDs = new ArrayList<String>();
                ArrayList<String> dsDsetIDs = new ArrayList<String>();
                ArrayList<String> ssRsetIDs = new ArrayList<String>();

                while(rsDsetSupport.next()){
                    ssFsetIDs.add(rsDsetSupport.getString("ss_feature_set_id"));
                    fsFsetIDs.add(rsDsetSupport.getString("fs_feature_set_id"));
                    dsDsetIDs.add(rsDsetSupport.getString("ds_data_set_id"));
                    ssRsetIDs.add(rsDsetSupport.getString("ss_result_set_id"));
                }


                //CHECK META KEY feature_set_ids
                boolean sqlSafe = true;

                for(int i=0; i < metaFsetIDs.length; i++){

                    if(! ssFsetIDs.contains(metaFsetIDs[i])){
                        ReportManager.problem(this, efgCon, "Found feature_set_id in regbuild_string:\t" +
                                              "regbuild." + fsInfo.get("cell_type") + ".feature_set_ids which is not " +
                                              "present as a supporting_set_id for DataSet " + fsInfo.get("name"));
                        result  = false;
                        sqlSafe = false;
                        continue;
                    }

                    ++count;

                    //Check feature_type is correct
                    stmt              = efgCon.createStatement();
                    ResultSet ssFtype = stmt.executeQuery("SELECT feature_type_id from feature_set " +
                                                          "where feature_set_id=" + metaFsetIDs[i]);


                    if(! ssFtype.next()){
                        //Need to test this as we have only data from meta and supporting_set so far!
                        ReportManager.problem(this, efgCon, "Found absent supporting FeatureSet from regbuild." +
                                              fsInfo.get("cell_type") + ".feature_set_ids:\t" + metaFsetIDs[i]);
                        //This will also be reported in the CHECK SETS/STATES loop below
                        continue;
                    }

                    if(! ssFtype.getString("feature_type_id").equals(metaFtypeIDs[i])){
                        ReportManager.problem(this, efgCon, "Found mismatch between meta feature_set_id(" +
                                              ssFtype.getString("feature_type_id") + ") and meta feature_type_id(" +
                                              metaFtypeIDs[i] + ") for " + fsInfo.get("cell_type"));
                        result = false;
                        sqlSafe = false;
                    }
                }

                //CHECK META SIZE
                if(ssFsetIDs.size() != count){
                    ReportManager.problem(this, efgCon, "");
                    result = false;
                    sqlSafe = false;
                }

                //CHECK META KEY focus_feature_set_ids
                String[] metaFFsetIDs  = ((String) fsInfo.get("focus_feature_set_ids")).split(",\\s*");


                for(int i=0; i < metaFFsetIDs.length; i++){

                    if(metaFFsetIDs[i].equals("")){
                        //Now test whether any of the feature_type_ids are of core class
                        //ResultSet.getFetchSize() does not work for MySQL :/ Just returns 0
                        //If fetching ftype name would have to process the whole ResultSet before we can get thr true size
                        //Not that useful to list them, so just print some useful SQL and count instead

                        ResultSet ftypesRset = stmt.executeQuery("SELECT count(feature_type_id) as num_ftypes from feature_type where feature_type_id in("
                                                                 + metaFtypeIDString +
                                                                 ") and class in ('Open Chromatin', 'Transcription Factor' ,'Transcription Factor Complex')");

                        ftypesRset.next();
                        int numCoreFsets = ftypesRset.getInt("num_ftypes");//This just returns 0 anyway!!

                        if(numCoreFsets != 0){
                            //Useful SQL here?
                            //To list the offending feature sets or
                            //just to correct the regbuild strings?
                            //these should have been corrected by update_DB_for_release?

                            ReportManager.problem(this, efgCon, "Found empty regbuild." + fsInfo.get("cell_type")
                                                  + ".focus_feature_set_ids regbuild_string. Is this really a projection build?");
                            result = false;
                            continue;
                        }
                    }
                    else{

                        if(! Arrays.asList(metaFsetIDs).contains(metaFFsetIDs[i])){
                            ReportManager.problem
                                (
                                 this, efgCon, "Found feature_set_id(" + metaFFsetIDs[i] +
                                 ") in regbuild_string:\t" + "regbuild." + fsInfo.get("cell_type") +
                                 ".focus_feature_set_ids which is not present in regbuild."
                                 + fsInfo.get("cell_type") + ".feature_set_ids");
                            result = false;
                            //sqlSafe = false;// Is it just the focus key that is wrong?
                            //Will have already set this otherwise
                            continue;
                        }
                    }
                }

                //Could check length matches for MultiCell set?
                //Could actually remove this meta_key for MultiCell as it should be the same feature_set_ids?

                //CHECK SUPPORTING FEATURE/DATA/RESULT SETS, STATES AND DBFILE_REGISTRY
                String usefulSQL = "";
                String[] dsetStates =  {"DISPLAYABLE"};
                //my @rset_states = (@dset_states, 'DAS_DISPLAYABLE', $imp_cs_status);
                //No method on basic [] array to add other arrays elements in assigment
                //The declaration code expects a String and will not interpolate an String[]
                //as separate Strings
                //String[] rsetStates = {Arrays.asList(dsetStates).,  };
                //Need to get current CS name here for IMPORTED_'CS_NAME' status

			  
                String[] rsetStates  = {"DISPLAYABLE"}; //Conditional test for RESULT_FEATURE_SET is below
                String[] fsetStates  = {"DISPLAYABLE", "MART_DISPLAYABLE"};
                String rsetStatesString = Arrays.toString(rsetStates).replaceAll("[\\[\\]]", "'");
                String fsetStatesString = Arrays.toString(fsetStates).replaceAll("[\\[\\]]", "'");
                String dsetStatesString = Arrays.toString(dsetStates).replaceAll("[\\[\\]]", "'");
                rsetStatesString = rsetStatesString.replaceAll(", ", "', '");
                fsetStatesString = fsetStatesString.replaceAll(", ", "', '");
                dsetStatesString = dsetStatesString.replaceAll(", ", "', '");

                //String[] windowSizes = {};//leave file test this to Collection test?
                ArrayList<String> absentStates = new ArrayList<String>();




                //Could these usefulSql status commands be using IDs which are not valid or null?
                //Yes these are unsafe until the the meta_keys/supporting_sets are corrected!
                //Change INSERT IGNORE into just select to encourage the HC checker to look at the output first?

                //Set up ArrayLists so we can report once for each set after the main loop
                ArrayList<String> problemSupportingFsets = new ArrayList<String>();
                ArrayList<String> problemSupportingDsets = new ArrayList<String>();
                ArrayList<String> problemSupportingRsets = new ArrayList<String>();

                for(int i=0; i < fsFsetIDs.size(); i++){

                    if(fsFsetIDs.get(i) == null){ //fset check
                        ReportManager.problem(this, efgCon, "RegulatoryFeatures:" + fsInfo.get("cell_type") +
                                              " has absent supporting FeatureSet\t" + ssFsetIDs.get(i));
                        result = false;
                        continue;
                    }
                    else{ //fset status checks here
                        absentStates = getAbsentStates(efgCon, fsetStates, "feature_set", fsFsetIDs.get(i).toString());

                        if(absentStates.size() != 0){
                            problemSupportingFsets.add(fsFsetIDs.get(i));
                        }


                        if(dsDsetIDs.get(i) == null){ //dset check
                            ReportManager.problem(this, efgCon, "RegulatoryFeatures:" + fsInfo.get("cell_type") +
                                                  " has absent DataSet for supporting FeatureSet\t" + fsFsetIDs.get(i));
                            result = false;
                            continue;
                        }
                        else{ //dset status checks here
                            absentStates = getAbsentStates(efgCon, dsetStates, "data_set", dsDsetIDs.get(i).toString());

                            if(absentStates.size() != 0){
                                problemSupportingDsets.add(dsDsetIDs.get(i));
                            }



                            if(ssRsetIDs.get(i) == null){
                                ReportManager.problem(this, efgCon, "RegulatoryFeatures:" + fsInfo.get("cell_type")
                                                      + " has absent supporting_set ResultSet for supporting DataSet\t" + dsDsetIDs.get(i));
                                result = false;
                                continue;
                            }
                            else{ //rset tests and status checks here

                                stmt = efgCon.createStatement();
                                rs   = stmt.executeQuery("SELECT rs.name as rs_name, sn.name as sn_name, dbr.path from result_set rs " +
                                                         "LEFT JOIN (status s join status_name sn ON " +
                                                         "s.status_name_id=sn.status_name_id AND sn.name='RESULT_FEATURE_SET') " +
                                                         "ON rs.result_set_id=s.table_id AND s.table_name='result_set' " +
                                                         "LEFT JOIN dbfile_registry dbr ON rs.result_set_id=dbr.table_id AND dbr.table_name='result_set' " +
                                                         "WHERE rs.result_set_id=" + ssRsetIDs.get(i));



                                if(! rs.next()){
                                    ReportManager.problem(this, efgCon, "RegulatoryFeatures:" + fsInfo.get("cell_type") +
                                                          " supporting DataSet has absent supporting ResultSet:\t" + ssRsetIDs.get(i));
                                    result = false;
                                }
                                else{

                                    if(rs.getString("sn_name") == null){ //Should be a Collection
                                        //Test dbfile_registry entries match rset name
                                        //Leave file tests to separate HC which only deals with the dbfile_registry table and files
                                        String dbfPath = rs.getString("path");
					//result_feature/Monocytes-CD14+_H3K27me3_ENCODE_Broad_bwa_samse

                                        if(dbfPath == null){
                                            //test result_set_input type too!

                                            ReportManager.problem(this, efgCon,
                                                                  "Could not find dbfile_registry entry for ResultSet which is " +
                                                                  "not a RESULT_FEATURE_SET:\t" + ssRsetIDs.get(i));
                                            result = false;
                                        }
                                        else if(! dbfPath.matches(".*" + Pattern.quote(rs.getString("rs_name")) + ".*")){//rset_name matches path?
                                            // This now allows fuzzy matching on path to allow for 'versioned' data.
                                            ReportManager.problem(this, efgCon,
                                                                  "Found mismatch between ResultSet name and dbfile_registry.path:\t" +
                                                                  rs.getString("rs_name") + " vs " + dbfPath);
                                            result = false;
                                        }
                                    }


                                    absentStates = getAbsentStates(efgCon, rsetStates, "result_set", ssRsetIDs.get(i).toString());

                                    if(absentStates.size() != 0){
                                        //do this for whole set of supporting rsets?
                                        problemSupportingRsets.add(ssRsetIDs.get(i));
                                    }
                                }
                            }//end of if(ssRsetIDs.get(i) == null){ else
                        }
                    }// end of if(fsFsetIDs.get(i) == null){ else
                }//end of for loop


                if(problemSupportingFsets.size() > 0){

                    if(sqlSafe){
                        usefulSQL = "\nUSEFUL SQL:\tINSERT IGNORE INTO status SELECT fs.feature_set_id, 'feature_set', sn.status_name_id from feature_set fs, status_name sn " +
                            "WHERE sn.name in (" + fsetStatesString + ") AND fs.feature_set_id IN (" +
                            problemSupportingFsets.toString().replaceAll("[\\[\\]]", "") + ");";
                    }

                    ReportManager.problem(this, efgCon, "Found absent states (from " + Arrays.toString(fsetStates) +
                                          ") for supporting FeatureSets:\t" + problemSupportingFsets.toString() + usefulSQL);
                    result = false;
                }



                if(problemSupportingDsets.size() > 0){

                    if(sqlSafe){
                        usefulSQL = "\nUSEFUL SQL:\tINSERT IGNORE INTO status SELECT ds.data_set_id, 'data_set', sn.status_name_id from data_set ds, status_name sn " +
                            "WHERE sn.name in (" + dsetStatesString + ") " +
                            "AND ds.data_set_id IN (" +	problemSupportingDsets.toString().replaceAll("[\\[\\]]", "") + ");";
                    }

                    ReportManager.problem
                        (this, efgCon, "Found some absent states (from " + dsetStatesString
                         + ") for supporting DataSet:\t" + problemSupportingDsets.toString() + usefulSQL);
                    result = false;
                }



                if(problemSupportingRsets.size() > 0){

                    if(sqlSafe){
                        usefulSQL = "\nUSEFUL SQL:\tINSERT IGNORE INTO status SELECT rs.result_set_id, 'result_set', sn.status_name_id from result_set rs, status_name sn " +
                            //Really need a java 'join' here
                            "WHERE sn.name in (" + rsetStatesString +
                            ") AND rs.result_set_id IN (" + problemSupportingRsets.toString().replaceAll("[\\[\\]]", "") + ");";

                    }

                    ReportManager.problem
                        (
                         this, efgCon, "RegulatoryFeatures:" + fsInfo.get("cell_type") +
                         " supporting DataSets have supporting ResultSet with some absent states (from "
                         + rsetStatesString + "):\t" + problemSupportingRsets.toString() + usefulSQL);
                    result = false;
                }
            }


            //This doesn't need to be inside the try, apart from access to regSetCount

            if(regSetCount > 0){

                //Add bit to compare previous version and dates
                //if there version has changed then we should expect at least
                //one of the dates to change?
                //Unless it is a patch version
                //e.g. remove just a few problem features
                //dates remain same but version goes from 12 to 12.1?
                Connection secCon = getEquivalentFromSecondaryServer(dbre).getConnection();
                
                if (secCon == null) {
                    logger.warning("Can't get equivalent database for " + dbre.getName());
                    return true;
                }


                //These are empty strings not nullsif not present
                String sqlQuery      = "select meta_value from meta where meta_key='regbuild.version'";
                String oldRelVersion = DBUtils.getRowColumnValue(secCon, sqlQuery);
                String newRelVersion = DBUtils.getRowColumnValue(efgCon, sqlQuery);
                sqlQuery      = "select meta_value from meta where meta_key=" +
                    "'regbuild.initial_release_date'";
                String oldRelDate    = DBUtils.getRowColumnValue(secCon, sqlQuery);
                String newRelDate    = DBUtils.getRowColumnValue(efgCon, sqlQuery);
                sqlQuery      = "select meta_value from meta where meta_key=" +
                    "'regbuild.last_annotation_update'";
                String oldAnnoDate   = DBUtils.getRowColumnValue(secCon, sqlQuery);
                String newAnnoDate   = DBUtils.getRowColumnValue(efgCon, sqlQuery);


                //Deal with incomplete data first
                if( newRelVersion.isEmpty() &&
                    newRelDate.isEmpty()    &&
                    newAnnoDate.isEmpty()   ){
                    ReportManager.warning
                        (this, efgCon,
                         "Found no current release version and/or dates, unable to complete test");
                    result = false;
                }
                else{

                    if( oldRelVersion.isEmpty() &&
                        oldRelDate.isEmpty()    &&
                        oldAnnoDate.isEmpty()   ){
                        ReportManager.warning
                            (this, efgCon,
                             "Found no previous release version or dates, assuming this is a virgin build");

                        //We have already done a test for all new values
                        //assume if there are defined then they are valid as we have nothing to compare them to.
                    }
                    else{

                        if( oldRelVersion.isEmpty() ||
                            oldRelDate.isEmpty()    ||
                            oldAnnoDate.isEmpty()   ){
                            ReportManager.problem
                                (this, efgCon,
                                 "Previous release version and/or dates meta entries are incomplete, " +
                                 "cannot complete regbuild dates test");
                            result = false;
                        }
                        else{ //WE have complete set of old an new values

                            if(! oldRelVersion.equals(newRelVersion) ){ // last_annotation_update should differ

                                if(! newAnnoDate.equals(oldAnnoDate) ){
                                    ReportManager.problem
                                        (this, efgCon,
                                         "The regbuild.release version has changed, but the " +
                                         "rebguild.last_annotation_update has not"
                                         );
                                    result = false;
                                }

                                if(! newRelDate.equals(oldRelDate) ){
                                    ReportManager.warning
                                        (this, efgCon,
                                         "The regbuild.release version has changed, but the " +
                                         "rebguild.initial_release_date has not"
                                         );
                                }
                            }
                            else{ // rel version match so should dates!

                                if(! newAnnoDate.equals(oldAnnoDate) ){
                                    ReportManager.warning
                                        (this, efgCon,
                                         "The last_annotation_update has changed but the release_version has not"
                                         );
                                    result = false;
                                }

                                if(! newRelDate.equals(oldRelDate) ){
                                    ReportManager.warning
                                        (this, efgCon,
                                         "The initial_release_date has changed but the release_version has not"
                                         );
                                    result = false;
                                }
                            }
                        }
                    }
                }
            }//end of regbuild version data test
        }
        catch (SQLException se) {
            //Does this exit and return false?
            se.printStackTrace();
            //This currently still returns PASSED and does print the 'problem'!
            ReportManager.problem(this, efgCon, "Caught SQLException");
            result = false;
        }

        return result;
    }

    //Move this to SingleFuncgenTestCase?
    public ArrayList getAbsentStates(Connection efgCon, String[] statusNames, String tableName, String tableID){
        ArrayList<String> absentStates = new ArrayList<String>();

        try{
            Statement stmt = efgCon.createStatement();
            String sqlCmd = "";

            for (int i = 0; i< statusNames.length; i++){

                ResultSet rs = stmt.executeQuery("SELECT s.table_id from status s join status_name sn " +
                                                 "ON s.status_name_id=sn.status_name_id WHERE sn.name='" + statusNames[i] +
                                                 "' AND s.table_name='" + tableName + "' AND s.table_id=" + tableID);

                if(! rs.next()){
                    absentStates.add(statusNames[i]);
                }
            }



        }catch (SQLException se) {
            //Does this exit and return false?
            se.printStackTrace();
            //			ReportManager.problem(this, efgCon, "Caught SQLException");

        }

        return absentStates;
    }

}
