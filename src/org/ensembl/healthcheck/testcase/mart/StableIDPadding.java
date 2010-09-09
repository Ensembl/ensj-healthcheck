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
package org.ensembl.healthcheck.testcase.mart;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check the padding in the stable_id_1051 column in certain species.
 */

public class StableIDPadding extends SingleDatabaseTestCase {
	
	String column = "stable_id_1051";
	Map<String,Integer> tablePaddingLength;

	/**
	 * Constructor.
	 */
	public StableIDPadding() {

		setTeamResponsible("biomart");
		addToGroup("post_martbuild");
		setDescription("Check the padding in the stable_id_1051 column in certain species.");

		tablePaddingLength = new HashMap<String,Integer>();
		tablePaddingLength.put("hsapiens_functional_genomics__regulatory_feature__dm",  15);
		tablePaddingLength.put("mmusculus_functional_genomics__regulatory_feature__dm", 18);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		
		for (String table : tablePaddingLength.keySet()) {
		
			int paddingLength = tablePaddingLength.get(table);
			
			int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE LENGTH(" + column + ") != " + paddingLength);
			
			if (rows > 0) {
	
				ReportManager.problem(this, con, rows + " rows in " + table + " do not have padded stable IDs of required length (" + paddingLength + ")");
				result = false;
				
			} else {
				
				ReportManager.correct(this, con, "All rows in " + table + " have padded stable IDs of required length (" + paddingLength + ")");

			}
		}
		
		return result;

	} // run

} // StableIDPadding
