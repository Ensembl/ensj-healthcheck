/*
 * Copyright (C) 2003 EBI, GRL
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
		databaseRegexp = ".*";// any 
		setDescription("Checks that all tables which must be filled, have data");

	}

        // a small number of tables are allowed to be empty so mustBeFilled is false
	private boolean mustBeFilled(String table) {

	    //if (table.equals("")){return false;}
	    if (table.equals("hsapiens_expression_gnf_pathology_support")){return false;}
	    if (table.equals("hsapiens_expression_gnf_preparation_support")){return false;}
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

			SchemaInfo si = SchemaManager.getSchema(con);

			Iterator tableIterator = si.getTables().iterator();
			while (tableIterator.hasNext()) {

				String table = ((TableInfo)tableIterator.next()).getName();
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
