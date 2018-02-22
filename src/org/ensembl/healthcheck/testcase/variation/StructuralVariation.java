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
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that the structural variations do not contain anomalities
 */
public class StructuralVariation extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of StructuralVariation
	 */
	public StructuralVariation() {

		
		setDescription("Checks that the structural variation tables make sense");
		setTeamResponsible(Team.VARIATION);

	}

	// ---------------------------------------------------------------------

	/**
	 * Check that the structural variation tables make sense.
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
			String stmt = "SELECT COUNT(DISTINCT svf1.structural_variation_id) FROM structural_variation_feature svf1 JOIN structural_variation_feature svf2 ON (svf2.structural_variation_id = svf1.structural_variation_id AND svf2.structural_variation_feature_id > svf1.structural_variation_feature_id AND svf2.seq_region_id = svf1.seq_region_id AND svf2.seq_region_start = svf1.seq_region_start AND svf2.seq_region_end = svf1.seq_region_end AND svf2.seq_region_strand = svf1.seq_region_strand)";
			int rows = DBUtils.getRowCount(con,stmt);
			if (rows > 0) {
				result = false;
				ReportManager.problem(this, con, String.valueOf(rows) + " rows are duplicated in structural_variation_feature");
			}
	
			result &= checkCountIsZero(con, "structural_variation_feature", "(inner_start < seq_region_start OR outer_start > seq_region_start OR inner_end > seq_region_end OR outer_end < seq_region_end)");

			// At the moment, this is ok since that means an insertion relative to the reference. In the future, we should probably represent these Ensembl-style (start = end+1)
			// result &= checkCountIsZero(con, "structural_variation", "inner_start = inner_end");
	
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck caused an exception: " + e.getMessage());
			result = false;
		}
		return result;

	} // run

} // StructuralVariation
