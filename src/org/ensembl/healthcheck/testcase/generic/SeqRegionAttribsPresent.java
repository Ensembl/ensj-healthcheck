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
 * Check that certain seq_region_attribs are present.
 */
public class SeqRegionAttribsPresent extends SingleDatabaseTestCase {

	private String[] attribCodes = { "GeneNo_knwCod" };

	/**
	 * Create a new SeqRegionAttribsPresent healthcheck.
	 */
	public SeqRegionAttribsPresent() {

		addToGroup("release");
		setDescription("Check that certain seq_region_attribs are present.");

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
		
		for (int i = 0; i < attribCodes.length; i++) {
			
			String code = attribCodes[i];
			int count = getRowCount(con, "SELECT COUNT(*) FROM attrib_type at, seq_region_attrib sa WHERE sa.attrib_type_id=at.attrib_type_id AND at.code='" + code + "'");
	
			if (count == 0) {
				
				ReportManager.problem(this, con, "No seq_region_attribs of code " + code + " - there should be some. This may indicate that the seq_region_stats.pl script needs to be run.");
				result = false;
				
			} else {
				
				ReportManager.correct(this, con, count + " seq_region_attrib entries for " + code);
				
			}
		}

		return result;

	} // run

	// -----------------------------------------------------------------

} // SeqRegionAttribsPresent
