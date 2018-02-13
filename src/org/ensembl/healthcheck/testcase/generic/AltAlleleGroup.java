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

/**
 * AltAlleleGroup
 * 
 * @author 
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;


import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 *  
 * Test to ensure that there are no alt allele groups that contain more than 1 gene on the primary assesmbly
 * 
 * 
 * @author
 * 
 */

public class AltAlleleGroup extends SingleDatabaseTestCase {

    private final static String ALLELE_GROUP_SQL =
    	  "SELECT count(alt_allele_group_id) AS cnt,alt_allele_group_id FROM "
    	+ "(SELECT aa.*,ae.exc_type FROM alt_allele aa "
    	+ "LEFT JOIN gene g ON g.gene_id=aa.gene_id "
    	+ "LEFT JOIN assembly_exception ae ON ae.seq_region_id=g.seq_region_id "
    	+ "WHERE exc_type IS NULL) AS aaexc "
    	+ "GROUP By alt_allele_group_id HAVING cnt>1;";
    
    public AltAlleleGroup() {

	appliesToType(DatabaseType.CORE);
	setDescription("Test to ensure that there are no alt allele groups"
		+ "that contain more than 1 gene on the primary assesmbly");

	setTeamResponsible(Team.GENEBUILD);
	
    }

    /**
     * This test only applies to the core database.
     */
    public void types() {

	removeAppliesToType(DatabaseType.OTHERFEATURES);
	removeAppliesToType(DatabaseType.CDNA);
	removeAppliesToType(DatabaseType.SANGER_VEGA);
	removeAppliesToType(DatabaseType.RNASEQ);
	removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Check that there are no alt allele groups that contain more than 1 gene on the primary assesmbly
     * 
     * @param dbre
     *          The database to check.
     * @return True if the test passes.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

	boolean result = true;

	Connection con = dbre.getConnection();
	try {
	    int alt_group_count = DBUtils.getRowCount( con, ALLELE_GROUP_SQL);
	    
	    if(alt_group_count > 0){
	    	
       	    ReportManager.problem(this, con,"Alt allele group contains more "
       	    	+ "than 1 gene on the primary assembly " + alt_group_count + ".");
	    	result = false;
	    	
	    }else if(alt_group_count < 0){
	    	
       	    ReportManager.problem(this, con,"Query did not execute for some reason. "
       	    	+ "Got row count in negative =>  " + alt_group_count + ".");
	    	result = false;
	    	
	    }else if(alt_group_count == 0){
	    	 ReportManager.correct(this, con, "No alt allele groups contain more"
	    	 + " than 1 gene on the primary assembly ");
	    	
	    }


	} catch (Exception e) {
	    result = false;
	    e.printStackTrace();
	}

	
	return result;

    }

} //AltAlleleGroup

