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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.DatabaseType; 
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all transcripts of genes of type 'gene' translate.
 */

public class TranscriptsTranslate extends SingleDatabaseTestCase {

    /**
     * Create a new TranscriptsTranslate testcase.
     */
    public TranscriptsTranslate() {

        addToGroup("post_genebuild");
        addToGroup("release");
        setDescription("Check that all transcripts of genes of type \'gene\' translate");
	
    }

    /**
     * This only really applies to core databases
     */
    public void types() {

        removeAppliesToType(DatabaseType.EST);
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
	    
	String sql = "SELECT COUNT(*) FROM gene g, transcript tr LEFT JOIN translation t ON t.transcript_id=tr.transcript_id WHERE t.translation_id IS NULL AND g.gene_id=tr.gene_id and g.type=\'gene\'";
	    
	int rows = getRowCount(con, sql);
	if (rows != 0) {
	    
	    ReportManager.problem(this, con, rows + " transcript(s) in genes of type 'gene' do not have translations.");
	    result = false;
		
	} else {
	    
	    ReportManager.correct(this, con, "All transcripts have translations");
	    
	}
	    
	return result;

    } // run
    
} // TranscriptsTranslate
