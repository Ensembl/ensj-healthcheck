/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*

 $Log$
 Revision 1.2.2.1  2004/02/23 14:26:57  gp1
 No longer depends on SchemaInfo etc

 Revision 1.2  2004/01/12 11:19:50  gp1
 Updated relevant dates (Copyright notices etc) to 2004.

 Revision 1.1  2003/11/04 16:43:51  dkeefe
 checks that tables do not contain duplicate rows

*/

package org.ensembl.healthcheck.testcase.mart;

import java.sql.*;
import java.util.Iterator;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check that all rows in each table are unique.
 */
public class NoDuplicateRows extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of MartNoDuplicateRowsTestCase
	 */
	public NoDuplicateRows() {

		addToGroup("post_ensmartbuild");
		setDescription("Checks that all rows in tables are distinct");

	}

	/**
	 * For each schema, check that every table has #rows = #distinct rows.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

			Connection con = dbre.getConnection();

			String[] tableNames = getTableNames(con);
			for (int i = 0; i < tableNames.length; i++) {

				String table = tableNames[i];
				logger.finest("Checking that " + table + " has all distinct rows");

				int total = getRowCount(con, "select count(*) from " + table);
				int distinct = total; // ie not a problem in the total > distinct test below

				if (total > 0) {
					// create a String listing the cols for this table
					java.util.List colList = DBUtils.getColumnsInTable(con, table);
					Iterator colIterator = colList.iterator();
					String colTxt = "";
					while (colIterator.hasNext()) {
						if (colTxt.length() > 0) {
							colTxt = colTxt + ",";
						}
						colTxt = colTxt + ((String)colIterator.next());
					}
					//System.out.print(colTxt + "\n");
					// query does group by all columns to simulate count distinct *
					distinct = getRowCount(con, "select count(*) from " + table + " group by " + colTxt);

				}
				if (total > distinct) {

					ReportManager.problem(this, con, table + " has " + total + " rows but only " + distinct + " distinct rows");
					result = false;
				}
			}

		return result;

	} // run

} // NoDuplicateRows
