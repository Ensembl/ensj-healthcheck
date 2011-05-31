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

package org.ensembl.healthcheck.testcase.variation;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that if the peptide_allele_string of transcript_variation is not >1. It should out >1, unless it filled with numbers
 */
public class VariationSynonym extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Check Variation Synonym
	 */
	public VariationSynonym() {
		addToGroup("variation");
		addToGroup("variation-release");
		setDescription("Check that Venter and Watson should have 3 million variations");
		setTeamResponsible(Team.VARIATION);

	}

	/**
	 * Run the test
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		
		if (dbre.getSpecies() != Species.HOMO_SAPIENS) {
			ReportManager.info(this, con, "This test is only for human at moment");
			return true;
		}
		
		boolean overallResult = true;
		
		try {
		
			String[] individualNames = { "Venter", "Watson" };
			String individualName;
			
			// Check that Venter and Watson should have 3 million variations
	
			for (int i = 0; i < individualNames.length; i++) {
				
				boolean result = true; 
				individualName = individualNames[i];
				int rows = getRowCount(con, "SELECT COUNT(*) FROM variation v, source s WHERE v.source_id=s.source_id and s.name like '%" + individualName + "'");
				int rows1 = getRowCount(con, "SELECT COUNT(*) FROM variation_synonym vs, source s WHERE vs.source_id=s.source_id and s.name like '%" + individualName + "'");
				int tot_rows = rows + rows1;
				if (tot_rows < 3000000) {
					result = false;
					ReportManager.problem(this, con, tot_rows + " with variations for " + individualName);
				} else {
					ReportManager.correct(this, con, "Venter/Watson has more then 3 million variations");
				}
				overallResult &= result;
			}
			
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an error: " + e.getMessage());
			overallResult = false;
		}
				
		return overallResult;

	} // run

} // VariationSynonym
