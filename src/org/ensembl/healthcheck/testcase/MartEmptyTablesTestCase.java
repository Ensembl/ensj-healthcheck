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
 Revision 1.2  2004/01/12 11:19:50  gp1
 Updated relevant dates (Copyright notices etc) to 2004.

 Revision 1.1  2003/11/04 12:09:52  dkeefe
 checks that all tables which should contain data do contain data


 */

package org.ensembl.healthcheck.testcase;

import java.sql.*;
import java.util.*;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check that all tables which should be filled have data.
 */
public class MartEmptyTablesTestCase extends EnsTestCase {

	/**
	 * Creates a new instance of EmptyTablesTestCase
	 */
	public MartEmptyTablesTestCase() {

		addToGroup("post_ensmartbuild");
		databaseRegexp = ".*"; // any 
		setDescription("Checks that all tables which must be filled, have data");

	}

	// a small number of tables are allowed to be empty so mustBeFilled is false
	private boolean mustBeFilled(String table) {

		//if (table.equals("")){return false;}
		if (table.equals("hsapiens_expression_gnf_pathology_support")) {
			return false;
		}
		if (table.equals("hsapiens_expression_gnf_preparation_support")) {
			return false;
		}
		return true;

	} // mustBeFilled

	/**
	 * For each schema, check that every table has more than 0 rows.
	 */
	public TestResult run() {

		boolean result = true;

		DatabaseConnectionIterator it = getDatabaseConnectionIterator();

		while (it.hasNext()) {

			Connection con = (Connection)it.next();

			Iterator tableIterator = getTableNames(con).iterator();
			while (tableIterator.hasNext()) {

				String table = (String)tableIterator.next();
				logger.finest("Checking that " + table + " has rows");

				if (!tableHasRows(con, table) && mustBeFilled(table)) {

					ReportManager.problem(this, con, table + " has zero rows");
					result = false;

				}
			}

		} // while connection

		return new TestResult(getShortTestName(), result);

	} // run

} // MartEmptyTablesTestCase
