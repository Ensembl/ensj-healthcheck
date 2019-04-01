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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks the FeatureTypes for the RegulatoryFeatures have been updated
 */

public class RegulatoryFeatureTypes extends SingleDatabaseTestCase {

	/**
	 * Create a new instance of RegulatoryFeatureTypes.
	 */
	public RegulatoryFeatureTypes() {
		addToGroup("post_regulatorybuild");
		addToGroup("release");
		addToGroup("funcgen");//do we need this group and the funcgen-release group?
		addToGroup("funcgen-release");
		setTeamResponsible(Team.FUNCGEN);

		setDescription("Checks regulatory_feature feature_types have been updated.");
		setPriority(Priority.RED);
		//These are misleading as they are printed even if the HC is passed
		//setEffect("RegulatoryFeatures will not have valid stable IDs.");
		//setFix("Re-run stable ID mapping or fix manually.");
	}

	/**
	 * This only applies to funcgen databases.
	 */
	public void types() {
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VARIATION);
		removeAppliesToType(DatabaseType.COMPARA);
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;
		Connection con = dbre.getConnection();
		
		
		try {
			Statement stmt = con.createStatement();
			// System.out.println("Executing " + sql);
			ResultSet rfsetFTypeNames = stmt.executeQuery("SELECT fs.name, ft.name, ft.class from feature_set fs, feature_type ft, regulatory_feature rf "+ 
					"WHERE fs.type='regulatory' and fs.feature_set_id=rf.feature_set_id and rf.feature_type_id=ft.feature_type_id group by fs.name, ft.name");
			
			while (rfsetFTypeNames != null && rfsetFTypeNames.next()) {
				String fsetName   = rfsetFTypeNames.getString(1);
				String ftypeName  = rfsetFTypeNames.getString(2);
				String ftypeClass = rfsetFTypeNames.getString(3);
				
				if (! ftypeClass.equals("Regulatory Feature")){
					ReportManager.problem(this, con, DBUtils.getShortDatabaseName(con) + " has regulatory FeatureSet " + fsetName + " which contains RegulatoryFeatures " +
										  "with invalid FeatureType class:\t" + ftypeName + " " + ftypeClass);
					result = false; 
				}
				else if(ftypeName.equals("RegulatoryFeature")){
					ReportManager.problem(this, con, DBUtils.getShortDatabaseName(con) + " has regulatory FeatureSet " + fsetName + 
										  " which still contains basic " +
										  "'RegulatoryFeature' type. RegulatoryFeature feature types need updating for this set");
					result = false; 	
				}
			}
				
		}catch (SQLException e){
			e.printStackTrace();
		}
	
		
		return result;
	}

	

}
