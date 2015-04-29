/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.ensembl.healthcheck.DatabaseRegistry;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that looks for broken foreign-key
 * relationships.
 */

public class CheckSpeciesSetTag extends AbstractComparaTestCase {

	/**
	 * Create an ForeignKeyMethodLinkSpeciesSetId that applies to a specific set
	 * of databases.
	 */
	public CheckSpeciesSetTag() {

		addToGroup("compara_genomic");
		setDescription("Check the content of the species_set_tag table");
		setTeamResponsible(Team.COMPARA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;

		DatabaseRegistryEntry[] allSecondaryComparaDBs = DBUtils
				.getSecondaryDatabaseRegistry().getAll(DatabaseType.COMPARA);

			// ... check that we have one name tag for every MSA
			result &= checkNameTagForMultipleAlignments(comparaDbre);

			if (allSecondaryComparaDBs.length == 0) {
				result = false;
				ReportManager.problem(this,
						comparaDbre.getConnection(),
						"Cannot find the compara database in the secondary server. This check expects to find a previous version of the compara database for checking that all the *named* species_sets are still present in the current database.");
			}
			for (int j = 0; j < allSecondaryComparaDBs.length; j++) {
				// Check vs previous compara DB.
				result &= checkSetOfSpeciesSets(comparaDbre,
						allSecondaryComparaDBs[j]);
			}

		return result;
	}

	public boolean checkSetOfSpeciesSets(
			DatabaseRegistryEntry primaryComparaDbre,
			DatabaseRegistryEntry secondaryComparaDbre) {

		boolean result = true;
		Connection con1 = primaryComparaDbre.getConnection();
		Connection con2 = secondaryComparaDbre.getConnection();
		HashMap<String,Integer> primarySets = new HashMap<String,Integer>();
		HashMap<String,Integer> secondarySets = new HashMap<String,Integer>();

		// Get list of species_set sets in the secondary server
		String sql = "SELECT value, count(*) FROM species_set_tag WHERE tag = 'name' GROUP BY value";
		try {
			Statement stmt1 = con1.createStatement();
			ResultSet rs1 = stmt1.executeQuery(sql);
			while (rs1.next()) {
				primarySets.put(rs1.getString(1), rs1.getInt(2));
			}
			rs1.close();
			stmt1.close();

			Statement stmt2 = con2.createStatement();
			ResultSet rs2 = stmt2.executeQuery(sql);
			while (rs2.next()) {
				secondarySets.put(rs2.getString(1), rs2.getInt(2));
			}
			rs2.close();
			stmt2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Iterator<String> it = secondarySets.keySet().iterator();
		while (it.hasNext()) {
			String next = it.next();
			Integer primaryValue = primarySets.get(next);
			Integer secondaryValue = secondarySets.get(next);
			if (primaryValue == null) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is missing (it appears %d time(s) in %s", next, secondaryValue, DBUtils.getShortDatabaseName(con2)));
				result = false;
			} else if (primaryValue < secondaryValue) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is present only %d times instead of %d as in %s", next, primaryValue, secondaryValue, DBUtils.getShortDatabaseName(con2)));
				result = false;
			}
		}
		it = primarySets.keySet().iterator();
		while (it.hasNext()) {
			String next = it.next();
			if (!secondarySets.containsKey(next)) {
				ReportManager.problem(this, con1, String.format("Species set '%s' is new (compared to %s)", next, DBUtils.getShortDatabaseName(con2)));
				result = false;
			}
		}

		return result;
	}

	public boolean checkNameTagForMultipleAlignments(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		HashMap<Integer,String> allSetsWithAName = new HashMap<Integer,String>();
		HashMap<Integer,String> allSetsForMultipleAlignments = new HashMap<Integer,String>();

		if (tableHasRows(con, "species_set_tag")) {

			// Get list of species_set sets in the secondary server
			String sql1 = "SELECT species_set_id, value FROM species_set_tag WHERE tag = 'name'";

			// Find all the species_set_ids for multiple alignments
			String sql2 = "SELECT species_set_id, name FROM method_link_species_set JOIN method_link USING (method_link_id) WHERE"
					+ " class LIKE '%multiple_alignment%' OR class LIKE '%tree_alignment%' OR class LIKE '%ancestral_alignment%'";

			try {
				Statement stmt1 = con.createStatement();
				ResultSet rs1 = stmt1.executeQuery(sql1);
				while (rs1.next()) {
					allSetsWithAName.put(rs1.getInt(1), rs1.getString(2));
				}
				rs1.close();
				stmt1.close();

				Statement stmt2 = con.createStatement();
				ResultSet rs2 = stmt2.executeQuery(sql2);
				while (rs2.next()) {
					allSetsForMultipleAlignments.put(rs2.getInt(1),
							rs2.getString(2));
				}
				rs2.close();
				stmt2.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Iterator<Integer> it = allSetsForMultipleAlignments.keySet().iterator();
			while (it.hasNext()) {
				Integer next = it.next();
				String setName = allSetsWithAName.get(next);
				String multipleAlignmentName = allSetsForMultipleAlignments.get(next);
				if (setName == null) {
					ReportManager.problem(this, con,
							"There is no name entry in species_set_tag for MSA \""
									+ multipleAlignmentName + "\".");
					result = false;
				}
			}

		} else {
			ReportManager.problem(this, con, "species_set_tag table is empty. There will be no aliases for multiple alignments");
			result = false;
		}

		return result;

	}

} // CheckSpeciesSetTag

