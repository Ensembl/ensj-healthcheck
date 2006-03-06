/*
 Copyright (C) 2003 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.DatabaseType; 
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for any genes/exons that are suspiciously large; > 2Mb for genes, > 0.5Mb for exons.
 * Length assumed to be end-start+1, i.e. including introns.
 */

public class BigGeneExon extends SingleDatabaseTestCase {


    private static long GENE_WARN  = 1000000; // warn if length greater than this
    private static long GENE_ERROR = 2000000; // throw if length greater than this
    private static long EXON_ERROR = 500000;  // warn if length greater than this

    /**
     * Create a new BigGeneExon testcase.
     */
    public BigGeneExon() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check for suspiciously long genes & exons");
	
    }

    /**
     * This only really applies to core databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.OTHERFEATURES);
        removeAppliesToType(DatabaseType.ESTGENE);
        removeAppliesToType(DatabaseType.VEGA);

    }

    /**
     * Run the test.
     * 
     * @param dbre The database to use.
     * @return true if the test pased.
     *  
     */
    public boolean run(DatabaseRegistryEntry dbre) {

	boolean result = true;

	Connection con = dbre.getConnection();
	    
	// gene - warning
	String sql = "SELECT COUNT(*) FROM gene WHERE (seq_region_end-seq_region_start+1) >= " + GENE_WARN + " AND (seq_region_end-seq_region_start+1) < " + GENE_ERROR;
	    
	int rows = getRowCount(con, sql);
	if (rows > 0) {
	    
	    ReportManager.info(this, con, rows + " genes are longer than " + GENE_WARN + " bases but less than " + GENE_ERROR + " bases");
		
	} else {
	    
	    ReportManager.correct(this, con, "No genes longer than " + GENE_WARN + " bases but less than " + GENE_ERROR + " bases");
	    
	}


	// gene - error
	sql = "SELECT COUNT(*) FROM gene WHERE (seq_region_end-seq_region_start+1) >= " + GENE_ERROR ;
	    
	rows = getRowCount(con, sql);
	if (rows > 0) {
	    
	    ReportManager.problem(this, con, rows + " genes are longer than " + GENE_ERROR + " bases");
	    result = false;
		
	} else {
	    
	    ReportManager.correct(this, con, "No genes longer than " + GENE_ERROR + " bases");
	    
	}

	// exon - error
	sql = "SELECT COUNT(*) FROM exon WHERE (seq_region_end-seq_region_start+1) >= " + EXON_ERROR ;
	    
	rows = getRowCount(con, sql);
	if (rows > 0) {
	    
	    ReportManager.problem(this, con, rows + " exons are longer than " + EXON_ERROR + " bases");
	    result = false;
		
	} else {
	    
	    ReportManager.correct(this, con, "No exons longer than " + EXON_ERROR + " bases");
	    
	}

	return result;

    } // run
    
} // BigGeneExon
