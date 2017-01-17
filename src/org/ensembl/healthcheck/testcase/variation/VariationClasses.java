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

		addToGroup("variation-release");

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

        // at the moment we only check human
        
        if (species == Species.HOMO_SAPIENS) {
            
            try {
                
                // and we only check that no HGMD mutation is ever classed as 'sequence_alteration'

                String query =  "SELECT COUNT(*) "+
                                "FROM variation v, source s, attrib a, attrib_type t "+
                                "WHERE t.code = 'SO_term' "+
                                "AND a.attrib_type_id = t.attrib_type_id "+
                                "AND a.value = 'sequence_alteration' "+
                                "AND a.attrib_id = v.class_attrib_id "+
                                "AND s.name = 'HGMD-PUBLIC' "+
                                "AND s.source_id = v.source_id ";

			    result &= (DBUtils.getRowCount(con, query) == 0);

		    } 
            catch (Exception e) {
			    ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
		    	result = false;
		    }
        }

		if (result) {
			ReportManager.correct(this, con, "Variation classes look sane");
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

} // VariationClasses
