/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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
