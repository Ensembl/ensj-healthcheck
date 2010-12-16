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
 * Check that if the peptide_allele_string of transcript_variation is not >1. 
 * It should out >1, unless it filled with numbers
 */
public class TranscriptVariation extends SingleDatabaseTestCase {

    /**
     * Creates a new instance of Check Transcript Variation
     */
    public TranscriptVariation() {
        addToGroup("variation");
	addToGroup("variation-release");
        setDescription("Check that if the peptide_allele_string of transcript_variation is not >1. It should out >1, unless it filled with numbers");
    }

    /**
     * Find any matching databases that have peptide_allele_string column.
     * @param dbre
     *          The database to use.
     * @return Result.
     */
    public boolean run(DatabaseRegistryEntry dbre) {

        boolean result = true;

	// check peptide_allele_string not filled with numbers

        Connection con = dbre.getConnection();
        int rows = getRowCount(con, "SELECT COUNT(*) FROM transcript_variation WHERE peptide_allele_string >1");
        if (rows >=1) {
            result = false;
            ReportManager.problem(this, con, rows + " with peptide_allele_string >1");
        } else {
      //      ReportManager.info(this, con, "No transcript_variation have peptide_allele_string >1);
        }

	int rows1 = getRowCount(con, "SELECT COUNT(*) FROM transcript_variation WHERE consequence_type=''");
        if (rows1 >=1) {
            result = false;
            ReportManager.problem(this, con, rows1 + " with consequence_type a empty string");
        } else {
      //      ReportManager.info(this, con, "No transcript_variation have consequence_type a empty string");
        }
	
	int rows2 = getRowCount(con, "SELECT COUNT(*) FROM variation_feature vf WHERE NOT EXISTS (SELECT * FROM transcript_variation tv WHERE tv.variation_feature_id = vf.variation_feature_id) AND vf.consequence_type != 'INTERGENIC' AND vf.consequence_type != 'HGMD_MUTATION'");
        if (rows2 >=1) {
	    result = false;
	    ReportManager.problem(this, con, rows2 + " with consequence_type != 'INTERGENIC' and there is no corresponding transcript exists in transcript_variation table");
	}
	if (result){
        	ReportManager.correct(this,con,"transcript_variation have peptide_allele_string >1, indicating column shift, peptide_allele_string become a number or consequence_type a empty string or consequence_type is not a INTERGENIC where there is no transcript exists");
        }

        return result;

    } // run

} // TranscriptVariation
