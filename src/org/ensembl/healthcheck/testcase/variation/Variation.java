/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the variation table does not contain anomalities
 */
public class Variation extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Variation
	 */
	public Variation() {

		addToGroup("variation-release");
		
		setDescription("Checks that the variation table");
		setTeamResponsible(Team.VARIATION);

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the variation table does not have blank evidence attribs
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		boolean result = true;
		
		try {				
			// Check for empty but non-null evidence statuses 
			if (!checkCountIsZero(con,"variation","evidence_attribs = '' ")) {
				ReportManager.problem(this, con, "Variations with blank evidence_attribs statuses");
				result = false;
			}
			
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
			result = false;
		}



               try {
                        // Check for MAF > 0.5 
                        if (!checkCountIsZero(con,"variation","minor_allele_freq >0.5 ")) {
                                ReportManager.problem(this, con, "Variations with minor alleles > 0.5");
                                result = false;
                        }

                } catch (Exception e) {
                        ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
                        result = false;
                }

    try {
      // Check that there are no variants in the failed variation set, with display = 1 and no citation record
      String size_stmt =  "SELECT count(distinct v.variation_id) "
                        + "FROM variation_set vs "
                        + "JOIN variation_set_variation vsv ON (vs.variation_set_id = vsv.variation_set_id) "
                        + "JOIN variation v ON (vsv.variation_id = v.variation_id) "
                        + "LEFT JOIN variation_citation vc ON (v.variation_id = vc.variation_id) "
                        + "WHERE vs.name = 'All failed variations' "
                        + "AND v.display = 1 "
                        + "AND vc.variation_id IS NULL;";

      int size_rows = DBUtils.getRowCount(con,size_stmt);
      if (size_rows > 0) {
        result = false;
        ReportManager.problem(this, con, String.valueOf(size_rows) + " failed variants with display = 1 and no citation records");
      }
    } catch (Exception e) {
      ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
      result = false;
    }
		
		return result;

	} // run

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

} // Variation
