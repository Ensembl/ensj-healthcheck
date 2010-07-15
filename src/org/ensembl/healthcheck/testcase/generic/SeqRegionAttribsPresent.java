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
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that certain seq_regions that have known, protein_coding genes have the GeneNo_knwCod attribute associated with them.
 */
public class SeqRegionAttribsPresent extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionAttribsPresent healthcheck.
	 */
	public SeqRegionAttribsPresent() {

		addToGroup("release");
		setDescription("Check that certain seq_regions that have known, protein_coding genes have the GeneNo_knwCod attribute associated with them.");
		setEffect("Webiste gene counts will be wrong");
		setFix("Re-run ensembl/misc-scripts/density_feature/seq_region_stats.pl script");
		
	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql = " FROM gene g WHERE g.biotype='protein_coding' AND g.status='KNOWN' AND g.seq_region_id NOT IN (SELECT DISTINCT(g.seq_region_id) FROM gene g LEFT JOIN seq_region_attrib sra ON g.seq_region_id=sra.seq_region_id WHERE g.biotype='protein_coding' AND g.status='KNOWN' AND sra.attrib_type_id=64 AND sra.seq_region_id IS NOT NULL)";
		
		int count = getRowCount(con, "SELECT COUNT(DISTINCT(g.seq_region_id))" + sql);
		
		if (count > 0) {
		
			String str = count + " seq_regions with known, protein_coding genes do not have the GeneNo_knwCod attribute associated";
			//str += "USEFUL SQL: SELECT DISTINCT(g.seq_region_id)" + sql;
			ReportManager.problem(this, con, str);
			result = false;
			
		} else {
			
			ReportManager.correct(this, con, "All seq_regions with known, protein_coding genes have a GeneNo_knwCod attribute associated with them");
			
		}
		
		return result;

	} // run

	// -----------------------------------------------------------------

} // SeqRegionAttribsPresent
