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

		setDescription("Checks if the Read Counts binary files for ResultFeatures exist and are not empty.");
		setPriority(Priority.AMBER);
		setEffect("Signal tracks will not display in the browser.");
		setFix("Re-create files or check file names manually.");		
		
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		
		boolean result=true;
		Connection con = dbre.getConnection();
		
		try {
			Statement stmt = con.createStatement();
			
			HashMap<String, String> rSetDBLinks = new HashMap<String, String>();
			
			ResultSet rsetRSetNames = stmt.executeQuery("SELECT rs.name, dbf.path from result_set rs, dbfile_registry dbf "+ 
			"WHERE rs.result_set_id=dbf.table_id and dbf.table_name='result_set'");
			while ((rsetRSetNames != null) && rsetRSetNames.next()) {
				//Assumes both fields are non-null... which should be true according to efg schema
				//Also assumes rset name is unique
				rSetDBLinks.put(rsetRSetNames.getString(1), rsetRSetNames.getString(2));
			}

			//If there are no DBLinks then there is nothing to test
			if(rSetDBLinks.isEmpty()) return true;
			
			//Get Base Folder
			ResultSet rsetDBDataRoot = stmt.executeQuery("SELECT meta_value from meta where meta_key='dbfile.data_root'");
			if((rsetDBDataRoot != null) && rsetDBDataRoot.next()){
				String root_dir  = rsetDBDataRoot.getString(1);
				//Check if this root dir exists first
				File root_dir_f = new File(root_dir);
				if(root_dir_f.exists()){
					//This HC does not check whether resultsets exist for a given dataset. 
					//It only checks if file links	are correct.	
					HashMap<String, String> problemLinks = new HashMap<String, String>();
					
					Iterator<String> dbLinkIt = rSetDBLinks.keySet().iterator();
					while(dbLinkIt.hasNext()){
						String rsetName  = dbLinkIt.next();
						String rsetPath  = rSetDBLinks.get(rsetName);
						String rSetFinalPath = root_dir+rsetPath;
						File rsetFolder = new File(rSetFinalPath);
						if(rsetFolder.exists()){
							String[] windows = {"30","65","130","260","450","648","950","1296"}; 
							for(int i=0;i<windows.length;i++){
								String rsetWindowFileName = root_dir+rsetPath+"/result_features."+rsetName+"."+windows[i]+".col";
								File rsetWindowFile = new File(rsetWindowFileName);
								if(rsetWindowFile.exists()){
									if(rsetWindowFile.length()==0){
										problemLinks.put(rsetName, rsetWindowFileName + " seems empty for set "+rsetName);
										result = false;
									}
								} else {
									problemLinks.put(rsetName, rsetWindowFileName + " does not seem to exist for set "+rsetName);
									result = false;
								}
							}							
						} else {
							problemLinks.put(rsetName, rSetFinalPath + " does not seem to be valid for set "+rsetName);
							result=false;
						}
					}

					int MAX_REPORT=5;
					//Check amount of problems and just output a few if there are too many...
					if(problemLinks.keySet().size()>MAX_REPORT){
						ReportManager.problem(this, con, problemLinks.keySet().size()+" result sets do not seem to have valid dbFile links");
						result = false;
					}
					
					int number=0;
					Iterator<String> problemIt = problemLinks.keySet().iterator();
					while(problemIt.hasNext()){
						ReportManager.problem(this, con, problemLinks.get(problemIt.next()));
						if(number>MAX_REPORT) return false;
					}
					
					
				} else {
					ReportManager.problem(this, con, root_dir + " defined in dbfile.data_root meta key for " +  
							DBUtils.getShortDatabaseName(con) + " does not seem to be valid");
					result = false;
				}
				
			} else {
				ReportManager.problem(this, con, DBUtils.getShortDatabaseName(con) + " does not have a dbfile.data_root meta key");
				result = false; 	
			}
				
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return result;
	}

}
