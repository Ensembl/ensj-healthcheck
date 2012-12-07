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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check for cases where components map to multiple parts of the assembly but the chained mapper is *not* specified for that pair of
 * coordinate systems in the meta table.
 */
public class MultipleComponentAssemblyMapping extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AssemblyMultipleOverlap.
	 */
	public MultipleComponentAssemblyMapping() {

		// addToGroup("post_genebuild");
		// addToGroup("release");

		setDescription("Check for cases where components map to multiple parts of the assembly but the chained mapper is *not* specified for that pair of coordinate systems in the meta table.");

		setPriority(Priority.AMBER);
		setEffect("Will cause problems with sequence retrieval for the affected region, and possibly website crashes.");
		setFix("Specify the chained mapper (#) in the assembly.mapping entry for these two coordinate systems.");
                addToGroup("production");
                addToGroup("release");
                addToGroup("pre-compara-handover");
                addToGroup("post-compara-handover");
		setTeamResponsible(Team.GENEBUILD);
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
		// cache coord system name to ID
		Map coordSystemNameAndVersionToID = new HashMap();

		Statement stmt = null;

		try {

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM coord_system");

			while (rs.next()) {
				if ("".equals(rs.getString("version"))) {
					System.err.println("\n\n Error ! Something seems to be wrong with your coord_system table.\n " + " The coord_system.version colum contains a string of length == 0\n"
							+ " Only proper mysql-NULL values or strings with length > 0 are allowed.\n\n");
					throw new Exception();
				}

				coordSystemNameAndVersionToID.put(rs.getString("name") + rs.getString("version"), new Long(rs.getLong("coord_system_id")));
				logger.fine(rs.getString("name") + rs.getString("version") + " -> " + rs.getLong("coord_system_id"));

			}

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		// build up SQL that filters out assembly mappings that already use the
		// chained mapper
		String constraint = "";

		String[] mappings = DBUtils.getColumnValues(con, "SELECT meta_value FROM meta WHERE meta_key='assembly.mapping'  AND meta_value LIKE '%#%'");

		Pattern assemblyMappingPattern = Pattern.compile("^([a-zA-Z0-9.]+):?([a-zA-Z0-9._]+)?[\\|#]([a-zA-Z0-9._]+):?([a-zA-Z0-9._]+)?([\\|#]([a-zA-Z0-9.]+):?([a-zA-Z0-9._]+)?)?$");

		for (int i = 0; i < mappings.length; i++) {

			Matcher matcher = assemblyMappingPattern.matcher(mappings[i]);

			if (matcher.matches()) {

				String cs1 = matcher.group(1);
				String v1 = matcher.group(2);
				String cs2 = matcher.group(3);
				String v2 = matcher.group(4);
				// TODO - ignoring third coordinate system for now

				long csID1 = ((Long) (coordSystemNameAndVersionToID.get(cs1 + v1))).longValue();
				long csID2 = ((Long) (coordSystemNameAndVersionToID.get(cs2 + v2))).longValue();

				logger.fine("Chained mapping for " + cs1 + ":" + v1 + " (ID " + csID1 + ") - " + cs2 + ":" + v2 + " (ID " + csID2 + ")");

				constraint += "AND NOT ((s2.coord_system_id = " + csID1 + " AND s1.coord_system_id = " + csID2 + ") OR (s2.coord_system_id = " + csID2 + " AND s1.coord_system_id = " + csID1 + "))";
			}
		}

		logger.fine(constraint);

		// now find components that map to multiple assemblies but whose coordinate
		// systems are not using a chained mapper
		String sql = "SELECT a.cmp_seq_region_id, a.asm_seq_region_id, s1.coord_system_id " + "FROM assembly a, seq_region s1, seq_region s2 "
				+ "WHERE a.asm_seq_region_id=s1.seq_region_id AND a.cmp_seq_region_id = s2.seq_region_id " + constraint + "ORDER BY s1.coord_system_id, a.cmp_seq_region_id";

		try {

			ResultSet rs = stmt.executeQuery(sql);

			long oldCoordSystemID = 0;
			Map list = new HashMap();

			while (rs.next()) {

				long cmpID = rs.getLong("cmp_seq_region_id");
				long asmID = rs.getLong("asm_seq_region_id");
				long coordSystemID = rs.getLong("coord_system_id");

				// allow mapping to different coordinate systems
				if (coordSystemID != oldCoordSystemID) {
					list.clear();
					oldCoordSystemID = coordSystemID;
				}

				Long LcmpID = new Long(cmpID);
				String value = (String) list.get(LcmpID);

				if (value != null) {

					String newValue = value + "," + asmID;

					list.put(new Long(cmpID), newValue);
					ReportManager.problem(this, con, "Component with ID " + cmpID + " is linked to more than one assembly mapping for coord_system with ID " + coordSystemID + " (" + newValue + ")");

					result = false;

				} else {

					list.put(LcmpID, "" + asmID);

				}

			} // while rs

		} catch (Exception e) {
			result = false;
			System.err.println("Error executing SQL: " + sql);
			e.printStackTrace();
		}

		if (result) {
			ReportManager.correct(this, con, "No problems with assembly - meta table mismatches");
		}

		return result;

	} // run

	// -------------------------------------------------------------------------

} // MultipleComponentAssemblyMapper
