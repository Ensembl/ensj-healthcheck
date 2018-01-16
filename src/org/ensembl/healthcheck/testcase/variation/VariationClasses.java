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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Sanity check variation classes
 */
public class VariationClasses extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of VariationClasses
	 */
	public VariationClasses() {


		setDescription("Sanity check variation classes");
		setTeamResponsible(Team.VARIATION);

	}

	// ---------------------------------------------------------------------

	/**
	 * Sanity check the variation classes.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Species species = dbre.getSpecies();

		Connection con = dbre.getConnection();

        	// check HGMD for human - no allele available, so check input type not over-written
                // check COSMIC & ClinVar as added outside full rebuild
        	if (species == Species.HOMO_SAPIENS) {
           		String [] sources = {"HGMD", "COSMIC", "ClinVar"};
                        int len = sources.length;
    
			for (int i =0; i< len; i++){

         		boolean source_ok = checkCount(con, sources[i]);
            			if(source_ok == false){
                			 result = false;
             			}
			}
         	}

         	// check dbSNP for all species - did VariationClass pipeline fail?
         	String source = "dbSNP";
         	boolean dbsnp_ok = checkCount(con, source);
         	if(dbsnp_ok == false){
            		result = false;
         	}

       		return result;
    }


    public boolean checkCount( Connection con, String source) {

      boolean result = true;

      try {

                // check that multiple variation classes have been assigned
                String query =  "SELECT COUNT(distinct v.class_attrib_id) from variation v, source s "+
                                "WHERE s.name = '"+ source + "' AND s.source_id = v.source_id ";

                 int rows = DBUtils.getRowCount(con, query);
                 if (rows == 1) {
		     result = false;
		     ReportManager.problem(this, con, "Only one variation class attrib type available for source " + source);
                 }

         }
         catch (Exception e) {
	    ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
	    result = false;
	 }

	if (result) {
		ReportManager.correct(this, con, "Variation classes look sane for source " + source );
	}

	return result;

     }


	// -----------------------------------------------------------------

	/**
	 * This only applies to variation databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.CORE);
		removeAppliesToType(DatabaseType.VEGA);

	}

} // VariationClasses
