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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check for genes with more than one transcript where all the transcripts have the same display_xref_id.
 */

public class TranscriptsSameName extends SingleDatabaseTestCase {

	/**
	 * Create a new TranscriptsSameName testcase.
	 */
	public TranscriptsSameName() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription(" Check for genes with more than one transcript where all the transcripts have the same display_xref_id.");
		
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test pased.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// Find all genes with more than one transcript
		String[] geneIDs = getColumnValues(con, 	"SELECT g.gene_id, COUNT(*) AS transcript_count FROM gene g, transcript t WHERE t.gene_id=g.gene_id GROUP BY g.gene_id HAVING transcript_count > 1");
		
		int sameNameCount = 0;
		
		for (int i = 0; i < geneIDs.length; i++) {
			
			int distinctCount = getRowCount(con, "SELECT COUNT(DISTINCT(display_xref_id)) FROM transcript t WHERE gene_id=" + geneIDs[i]);
			if (distinctCount == 1) {
				sameNameCount++;
			}
		}
		
		System.out.println("Total genes with more than one transcript: " + geneIDs.length);
		System.out.println("Total genes where all transcripts have same name: " + sameNameCount);
		return result;

	} // run
	
} // TranscriptsSameName
