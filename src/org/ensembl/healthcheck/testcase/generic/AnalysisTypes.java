/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that all chromosomes have at least some genes with certain analyses.
 */
public class AnalysisTypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalysisTypes
	 */
	public AnalysisTypes() {

		setDescription("Check that all chromsosomes have at least some genes with certain analyses.");
		setPriority(Priority.AMBER);
		setEffect("Some genes may have only Ensembl or Havana annotation.");
		setFix("Possibly indicates a problem with the Havana/Ensembl merge pipeline");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		if (!dbre.getSpecies().equals(DatabaseRegistryEntry.HOMO_SAPIENS)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.MUS_MUSCULUS)
				&& !dbre.getSpecies().equals(DatabaseRegistryEntry.DANIO_RERIO)) {
			return true;
		}

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] logicNames = { "ensembl", "havana", "ensembl_havana_gene" };

		// get all chromosomes, ignore LRG and MT
		String[] seqRegionNames = DBUtils.getColumnValues(con,
				"SELECT s.name FROM seq_region s, seq_region_attrib sa, attrib_type at WHERE s.seq_region_id = sa.seq_region_id AND sa.attrib_type_id = at.attrib_type_id AND code = 'karyotype_rank' AND s.name NOT LIKE 'MT'");

		// loop over each seq region, check that each logic name is represented
		try {

			PreparedStatement stmt = con.prepareStatement(
					"SELECT COUNT(*) FROM gene g, seq_region sr, analysis a WHERE a.analysis_id=g.analysis_id AND g.seq_region_id=sr.seq_region_id AND sr.name=? AND a.logic_name=?");

			for (String seqRegion : seqRegionNames) {

				for (String logicName : logicNames) {

					stmt.setString(1, seqRegion);
					stmt.setString(2, logicName);

					ResultSet rs = stmt.executeQuery();

					rs.first();
					int rows = rs.getInt(1);

					if (rows == 0) {

						result = false;
						ReportManager.problem(this, con,
								String.format("Chromosome %s has no genes with logic name %s", seqRegion, logicName));

					}

					rs.close();

				}

			}

			stmt.close();

		} catch (SQLException e) {

			System.err.println("Error executing SQL");
			e.printStackTrace();

		}

		return result;

	} // run

	// --------------------------------------------------------------------------

} // AnalysisTypes
