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
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for duplicate synonym names which may have different import sources despite having the same original source
 */
public class VariationSynonym extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of Check Variation Synonym
	 */
	public VariationSynonym() {
		addToGroup("variation-release");
		setDescription("Check for duplicate variation synonyms");
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
		
		boolean result = true;
		
		try {
			
		    int rows = DBUtils.getRowCount(con, "select vs1.variation_synonym_id, vs1.variation_id, vs1.name from variation_synonym vs1, variation_synonym vs2 where  vs2.variation_id =  vs1.variation_id and vs2.name  = vs1.name and vs2.variation_synonym_id > vs1.variation_synonym_id");

		    if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " duplicate variation_synonyms detected");
		    } else {
			ReportManager.correct(this, con, "No duplicate variation_synonyms detected");
		    }
		
			
		} catch (Exception e) {
			ReportManager.problem(this, con, "HealthCheck generated an error: " + e.getMessage());
			result = false;
		}
				
		return result;

	} // run

} // VariationSynonym
