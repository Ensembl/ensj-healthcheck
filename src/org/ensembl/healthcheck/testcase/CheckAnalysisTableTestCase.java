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

package org.ensembl.healthcheck.testcase;

import java.sql.*;
import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;

/**
 * Check the data in the analysis table.
 */
public class CheckAnalysisTableTestCase extends EnsTestCase {

	public CheckAnalysisTableTestCase() {
		addToGroup("post_genebuild");
		setDescription("Check the integrity of the analysis table");
	}

	/** 
	 * Rows in the analysis table that have a blank db field are likely to be erroneous.
	 * @return Result.
	 */
	public TestResult run() {

		boolean result = true;

		DatabaseConnectionIterator it = getDatabaseConnectionIterator();

		while (it.hasNext()) {

			Connection con = (Connection)it.next();
			String[] logicNames = getColumnValues(con, "SELECT logic_name FROM analysis WHERE db =''");
			for (int i = 0; i < logicNames.length; i++) {
				ReportManager.problem(this, con, "analysis table db field is blank for logic name " + logicNames[i]);
				result = false;
			}

		} // while connection

		return new TestResult(getShortTestName(), result);

	} // run

}