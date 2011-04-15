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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check whether the assembly.name meta entry changes between releases, and if so, check that the assembly_exception table (patches
 * only) has changed. Currently only for human.
 */

public class AssemblyNameChanged extends SingleDatabaseTestCase {

	/**
	 * Constructor.
	 */
	public AssemblyNameChanged() {

		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check whether the assembly.name meta entry changes between releases, and if so, check that the assembly_exception table (patches only) has changed. Currently only for human.");
                setTeamResponsible("GeneBuilders");
	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry currentDBRE) {

		// currently for human only
		if (currentDBRE.getSpecies() != Species.HOMO_SAPIENS) {
			return true;
		}

		boolean result = true;

		// get assembly names from previous and current databases
		Connection currentCon = currentDBRE.getConnection();
		String currentAssemblyName = DBUtils.getMetaValue(currentCon, "assembly.name");

		DatabaseRegistryEntry previousDBRE = getEquivalentFromSecondaryServer(currentDBRE);
		Connection previousCon = previousDBRE.getConnection();
		String previousAssemblyName = DBUtils.getMetaValue(previousCon, "assembly.name");

		// compare assembly_exception tables (patches only) from each database
		try {

			Statement previousStmt = previousCon.createStatement();
			Statement currentStmt = currentCon.createStatement();

			String sql = "SELECT * FROM assembly_exception WHERE exc_type LIKE ('PATCH_%') ORDER BY assembly_exception_id";
			ResultSet previousRS = previousStmt.executeQuery(sql);
			ResultSet currentRS = currentStmt.executeQuery(sql);

			boolean assExSame = DBUtils.compareResultSets(currentRS, previousRS, this, "", false, false, "assembly_exception", false);

			currentRS.close();
			previousRS.close();
			currentStmt.close();
			previousStmt.close();

			// if the assembly name has changed, then the assembly_exception table should have changed as well
			if (!previousAssemblyName.equals(currentAssemblyName)) {

				if (assExSame) {

					ReportManager.problem(this, currentCon, String.format("Assembly name has changed from previous database (%s -> %s) but assembly_exception table does not contain different patches",
							previousAssemblyName, currentAssemblyName));
					result = false;

				} else {

					ReportManager.correct(this, currentCon, "Assembly names have changed, but so have assembly_exception tables.");

				}

			} else {

				// if we are here, the assembly names are the SAME, and assembly_exception patches should be the same as well
				if (!assExSame) {
					
					ReportManager.problem(this, currentCon, String.format("Assembly name (%s)is the same as the previous release, but assembly_exception tables contain different patches", currentAssemblyName));
					result = false;
					
				} else {
					
					ReportManager.correct(this, currentCon, "Assembly names and assembly_exception patches are the same.");

					
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;

	} // run

} // AssemblyNameChanged
