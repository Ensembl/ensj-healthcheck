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
 * 
 * $Log$
 * Revision 1.2  2004/03/25 13:47:41  gp1
 * Merged v2 branch into HEAD.
 *
 * Revision 1.1.2.3  2004/03/18 14:19:46  gp1
 * Corrected Javdoc. 
 * 
 * Various other tidyings up.
 *
 * Revision 1.1.2.2  2004/03/17 17:31:53  gp1
 * Changes to nearly every file in the project; many are stylistic only, but many remove potential errors (e.g. tab characters in SQL) or improve performance (removal of use of on-demand imports)
 * Revision 1.1.2.1 2004/03/01 09:42:08 gp1
 * Moved into mart subdirectory. Some tests renamed
 * 
 * Revision 1.2.2.1 2004/02/23 14:26:57 gp1 No longer depends on SchemaInfo etc
 * 
 * Revision 1.2 2004/01/12 11:19:50 gp1 Updated relevant dates (Copyright
 * notices etc) to 2004.
 * 
 * Revision 1.1 2003/11/04 16:43:51 dkeefe checks that tables do not contain
 * duplicate rows
 *  
 */

package org.ensembl.healthcheck.testcase.mart;

import java.sql.Connection;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all rows in each table are unique.
 */
public class NoDuplicateRows extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of NoDuplicateRows
	 */
	public NoDuplicateRows() {

		addToGroup("post_martbuild");
		setDescription("Checks that all rows in tables are distinct");

	}

	/**
	 * For each schema, check that every table has #rows = #distinct rows.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		for (String table : getTableNames(con)) {

			if (!table.startsWith("a")) {
				continue;
			}

			logger.finest("Checking that " + table + " has all distinct rows");

			int total = countRowsInTable(con, table);
			int distinct = total; // i.e. not a problem in the total > distinct test below

			if (total > 0) {
				// create a String listing the columns for this table
				String colTxt = StringUtils.join(DBUtils.getColumnsInTable(con, table), ',');
System.out.println(colTxt);
				// query does group by all columns to simulate count distinct *
				distinct = getRowCount(con, "SELECT COUNT(*) FROM " + table + " GROUP BY " + colTxt);

			}
			if (total > distinct) {

				ReportManager.problem(this, con, table + " has " + total + " rows but only " + distinct + " distinct rows");
				result = false;
			}
		}

		return result;

	} // run

} // NoDuplicateRows
