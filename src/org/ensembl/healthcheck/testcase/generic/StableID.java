/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with
 * this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;

import org.ensembl.healthcheck.*;
import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.testcase.*;

/**
 * Checks the *_stable_id tables to ensure they are populated, have no orphan references,
 * and have valid versions. Also prints some examples from the table for checking by eye.
 * 
 * <p>
 * Group is <b>check_stable_ids </b>
 * </p>
 * 
 * <p>
 * To be run after the stable ids have been assigned.
 * </p>
 */
public class StableID extends SingleDatabaseTestCase {

	public StableID() {
		addToGroup("id_mapping");
		setDescription("Checks *_stable_id tables are valid.");
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		boolean exonResult = checkStableIDs(con, "exon");
		boolean translationResult = checkStableIDs(con, "translation");
		boolean transcriptResult = checkStableIDs(con, "transcript");
		boolean geneResult = checkStableIDs(con, "gene");

		result = result && exonResult && translationResult && transcriptResult && geneResult;

		return result;
	}

	/**
	 * Checks that the typeName_stable_id table is valid. The table is valid if it has >0
	 * rows, and there are no orphan references between typeName table and
	 * typeName_stable_id. Also prints some example data from the typeName_stable_id table
	 * via ReportManager.info().
	 * 
	 * @param con connection to run quries on.
	 * @param typeName name of the type to check, e.g. "exon"
	 * @return true if the table and references are valid, otherwise false.
	 */
	public boolean checkStableIDs(Connection con, String typeName) {

		boolean result = true;

		String nStableIDs = getRowColumnValue(con, "select count(*) from " + typeName + "_stable_id;");
		ReportManager.info(this, con, "Num " + typeName + "s stable ids = " + nStableIDs);

		if (Integer.parseInt(nStableIDs) < 1) {
			ReportManager.problem(this, con, typeName + "_stable_id table is empty.");
			result = false;
		}

		// print a few rows so we can check by eye that the table looks ok
		DBUtils.printRows(this, con, "select * from " + typeName + "_stable_id limit 10;");

		// look for orphans between type and type_stable_id tables
		int orphans = countOrphans(con, typeName, typeName + "_id", typeName + "_stable_id", typeName + "_id", false);
		if (orphans > 0) {
			ReportManager.problem(this, con, "Orphan references between " + typeName + " and " + typeName
					+ "_stable_id tables.");
			result = false;
		}

		String nInvalidVersionsStr = getRowColumnValue(con, "select count(*) as " + typeName + "_with_invalid_version"
				+ " from " + typeName + "_stable_id where version<1;");
		int nInvalidVersions = Integer.parseInt(nInvalidVersionsStr);
		if (nInvalidVersions > 0) {
			ReportManager.problem(this, con, "Invalid " + typeName + "versions in " + typeName + "_stable_id.");
			DBUtils.printRows(this, con, "select distinct(version) from " + typeName + "_stable_id;");
			result = false;
		}

		return result;
	}

}
