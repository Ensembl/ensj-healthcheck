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

import java.sql.*;

import org.ensembl.healthcheck.testcase.*;
import org.ensembl.healthcheck.util.*;
import org.ensembl.healthcheck.*;

/**
 * Check that feature co-ords make sense.
 */
public class ProteinFeatures extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckFeatureCoordsTestCase
	 */
	public ProteinFeatures() {
		setDescription("Check that protein annotation feature coords make sense and that all translations exist in the database");
		addToGroup("post_genebuild");
		addToGroup("release");
	}

	/**
	 * Iterate over each affected database and perform various checks.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		int rows = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE seq_start > seq_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " have protein features where seq_start > seq_end");
		} else {
			ReportManager.correct(this, con, "No protein features where seq_start > seq_end");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE seq_start < 0");
		if (rows > 0) {
			ReportManager.problem(this, con, rows + " have protein features where seq_start < 0");
		} else {
			ReportManager.correct(this, con, "No protein features where seq_start < 0");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) FROM protein_feature WHERE hit_start < 0");
		if (rows > 0) {
			ReportManager.problem(this, con, rows + " have protein features where hit_start < 0");
		} else {
			ReportManager.correct(this, con, "No protein features where hit_start < 0");
		}

		logger.info("Checking protein features for " + DBUtils.getShortDatabaseName(con) + " ...");
		rows = getRowCount(con, "SELECT COUNT(*) from protein_feature WHERE hit_start > hit_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " have protein features where hit_start > hit_end");
		} else {
			ReportManager.correct(this, con, "No protein features where hit_start > hit_end");
		}

		return result;

	} // run

} // ProteinFeatures
