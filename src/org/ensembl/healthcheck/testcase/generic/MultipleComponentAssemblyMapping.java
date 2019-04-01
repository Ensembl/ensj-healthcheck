/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.ensembl.healthcheck.DatabaseType;
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

		setDescription("Check for cases where components map to multiple parts of the assembly but the chained mapper is *not* specified for that pair of coordinate systems in the meta table.");

		setPriority(Priority.AMBER);
		setEffect("Will cause problems with sequence retrieval for the affected region, and possibly website crashes.");
		setFix("Specify the chained mapper (#) in the assembly.mapping entry for these two coordinate systems.");
		setTeamResponsible(Team.GENEBUILD);
	}

        /**
         * This only applies to core databases.
         */
        public void types() {

                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.CDNA);
                removeAppliesToType(DatabaseType.VEGA);
                removeAppliesToType(DatabaseType.SANGER_VEGA);
                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.RNASEQ);

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
                HashMap<Long, String> coordSystemIDToNameAndVersion = new HashMap();

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
                                coordSystemIDToNameAndVersion.put(new Long(rs.getLong("coord_system_id")), rs.getString("name") + ":" + rs.getString("version"));
				logger.fine(rs.getString("name") + rs.getString("version") + " -> " + rs.getLong("coord_system_id"));

			}

		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}

		// build up SQL that filters out assembly mappings that already use the
		// chained mapper
		String constraint = "";

		String[] mappings = DBUtils.getColumnValues(con, "SELECT meta_value FROM meta WHERE meta_key='assembly.mapping'");

		Pattern assemblyMappingPattern = Pattern.compile("^([a-zA-Z0-9.]+):?([a-zA-Z0-9._-]+)?[\\|#]([a-zA-Z0-9._-]+):?([a-zA-Z0-9._-]+)?([\\|#]([a-zA-Z0-9.]+):?([a-zA-Z0-9._-]+)?)?$");

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
		String sql = "SELECT count(*), s1.coord_system_id, s2.coord_system_id " + "FROM assembly a, seq_region s1, seq_region s2 "
				+ "WHERE a.asm_seq_region_id=s1.seq_region_id AND a.cmp_seq_region_id = s2.seq_region_id AND s1.coord_system_id != s2.coord_system_id " + constraint + " group by s1.coord_system_id, s2.coord_system_id ";

		try {

			ResultSet rs = stmt.executeQuery(sql);

			long oldCoordSystemID = 0;
			Map list = new HashMap();

			while (rs.next()) {

                                long count = rs.getLong(1);
				long asmCoordSystemID = rs.getLong(2);
                                long cmpCoordSystemID = rs.getLong(3);

                                String asmCs = coordSystemIDToNameAndVersion.get(asmCoordSystemID);
                                String cmpCs = coordSystemIDToNameAndVersion.get(cmpCoordSystemID);
                                ReportManager.problem(this, con, count + " components for " + cmpCs + " are mapped to " + asmCs + " but this is not in the list of expected mappings. Did you forget to add a meta_key for " + asmCs + "#" + cmpCs + "?");


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
