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
//import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that allele frequencies add up to 1
 */
public class AlleleFrequencies extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of Check Allele Frequencies
     */
    public AlleleFrequencies() {
        addToGroup("variation");
	addToGroup("variation-release");
        setDescription("Check that the allele frequencies add up to 1");
    }

    /**
     * Check that all allele/genotype frequencies add up to 1 for the same variation/subsnp and sample.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

        Connection con = dbre.getConnection();
	String[] tables = new String[] {
	    "population_genotype",
	    "allele"
	};
	// Get variations with allele/genotype frequencies that don't add up to 1 for the same variation_id, subsnp_id and sample_id
	for (int i=0; i<tables.length; i++) {
	    String stmt = "SELECT q.problem FROM (SELECT CONCAT('variation_id = ',a.variation_id,', subsnp_id = ',a.subsnp_id,', sample_id = ',a.sample_id,', sum is: ',ROUND(SUM(a.frequency),2)) AS problem, ROUND(SUM(a.frequency),2) AS sum FROM " + tables[i] + " a WHERE a.frequency IS NOT NULL GROUP BY a.variation_id, a.subsnp_id, a.sample_id) AS q WHERE q.sum != 1 LIMIT 1";
	    String vfId = getRowColumnValue(con,stmt);
	    if (vfId != null && vfId.length() > 0) {
		ReportManager.problem(this, con, "There are variations in " + tables[i] + " where the frequencies don't add up to 1 (e.g. " + vfId + ")");
		result = false;
	    }
	}
	if ( result ){
	    ReportManager.correct(this,con,"Allele/Genotype frequency healthcheck passed without any problem");
	}
        return result;

    } // run

} // AlleleFrequencies
