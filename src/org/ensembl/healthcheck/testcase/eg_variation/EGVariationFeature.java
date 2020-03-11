/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.eg_variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the variation_feature table do not contain anomalities
 */
public class EGVariationFeature extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of VariationFeature
	 */
	public EGVariationFeature() {

		addToGroup("EGVariation");
		
		setDescription("Checks that the EG variation_feature table makes sense");
		setTeamResponsible(Team.ENSEMBL_GENOMES);

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the variation_feature tables make sense.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		boolean result = true;
		
		try {
	
			// Look for duplicates
			String stmt = "SELECT COUNT(DISTINCT vf1.variation_id) FROM variation_feature vf1 JOIN variation_feature vf2 ON (vf2.variation_id = vf1.variation_id AND vf2.variation_feature_id > vf1.variation_feature_id AND vf2.seq_region_id = vf1.seq_region_id AND vf2.seq_region_start = vf1.seq_region_start AND vf2.allele_string = vf1.allele_string AND vf2.seq_region_end = vf1.seq_region_end AND vf2.seq_region_strand = vf1.seq_region_strand)";
			int rows = DBUtils.getRowCount(con,stmt);
			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, String.valueOf(rows) + " rows are duplicated in variation_feature");
			}
			
			// Check that seq_region_start is not 1 (why not..?)
			if (!checkCountIsZero(con,"variation_feature","seq_region_start = 1 AND seq_region_end > 1")) {
				ReportManager.problem(this, con, "Variation Features with coordinates = 1");
				result = false;
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

} // VariationFeature
