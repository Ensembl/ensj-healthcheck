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
import java.util.Iterator;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for columns of type ENUM that have blank values - probably means there was a problem importing them.
 */

public class BlankEnums extends SingleDatabaseTestCase {

	/**
	 * Create a new BlankEnums testcase.
	 */
	public BlankEnums() {

		addToGroup("post_genebuild");
		addToGroup("release");
		addToGroup("funcgen-release");
		addToGroup("funcgen");
		addToGroup("compara-ancestral");
		
		setDescription("Check for columns of type ENUM that have blank values - probably means there was a problem importing them.");
		setPriority(Priority.AMBER);
		setEffect("Will have blank values where NULL or one of the enum values is expected.");
		setFix("Re-import after identifying source of problem - possibly the word NULL in import files instead of \\N");
                setTeamResponsible("Relco and GeneBuilders");
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

		String[] tables = DBUtils.getTableNames(con);

		for (int i = 0; i < tables.length; i++) {

			String table = tables[i];
			
			List columnsAndTypes = DBUtils.getTableInfo(con, table, "enum");
			Iterator it = columnsAndTypes.iterator();
			while (it.hasNext()) {

				String[] columnAndType = (String[]) it.next();
				String column = columnAndType[0];
				
				int rows = getRowCount(con, "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=''");
				
				if (rows > 0) {
					
					ReportManager.problem(this, con, rows + " rows in " + table + "." + column + " are blank, should be NULL or one of the ENUM values");
					result = false;

				} 
				
			}
			
		}

		return result;

	} // run
	
} // BlankEnums
