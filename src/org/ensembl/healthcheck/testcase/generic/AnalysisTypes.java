/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that all chromosomes have at least some genes with certain analyses.
 */
public class AnalysisTypes extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of AnalysisTypes
	 */
	public AnalysisTypes() {

		addToGroup("release");
		addToGroup("post_genebuild");
		setDescription("Check that all chromsosomes have at least some genes with certain analyses.");
		setPriority(Priority.AMBER);
		setEffect("Some genes may have only Ensembl or Havana annotation.");
		setFix("Possibly indicates a problem with the Havana/Ensembl merge pipeline");
		setTeamResponsible("genebuilders");

	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.SANGER_VEGA);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbr
	 *          The database registry containing all the specified databases.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		// only applies to human, mouse and zebrafish at the moment
		Species species = dbre.getSpecies();

		if (!species.equals(Species.HOMO_SAPIENS) && !species.equals(Species.MUS_MUSCULUS) && !species.equals(Species.DANIO_RERIO)) {

			return true;

		}

		boolean result = true;

		Connection con = dbre.getConnection();

		String[] logicNames = { "ensembl", "havana", "ensembl_havana_gene" };

		// get all chromosomes
		String[] seqRegionNames = getColumnValues(con,
				"SELECT s.name FROM seq_region s, coord_system cs WHERE s.coord_system_id=cs.coord_system_id AND cs.name='chromosome' AND cs.version='GRCh37' AND s.name NOT LIKE 'LRG%'");

		// filter out patches
		String[] patches = getColumnValues(con, "SELECT sr.name FROM seq_region sr, assembly_exception ae WHERE sr.seq_region_id=ae.seq_region_id AND ae.exc_type IN ('PATCH_NOVEL', 'PATCH_FIX')");

		List<String> nonPatchSeqRegions = ListUtils.removeAll(Arrays.asList(seqRegionNames), Arrays.asList(patches));

		// loop over each seq region, check that each logic name is represented
		try {

			PreparedStatement stmt = con
					.prepareStatement("SELECT COUNT(*) FROM gene g, seq_region sr, analysis a WHERE a.analysis_id=g.analysis_id AND g.seq_region_id=sr.seq_region_id AND sr.name=? AND a.logic_name=?");

			for (String seqRegion : nonPatchSeqRegions) {

				for (String logicName : logicNames) {

					stmt.setString(1, seqRegion);
					stmt.setString(2, logicName);

					ResultSet rs = stmt.executeQuery();

					rs.first();
					int rows = rs.getInt(1);

					if (rows == 0) {

						result = false;
						ReportManager.problem(this, con, String.format("Chromosome %s has no genes with logic name %s", seqRegion, logicName));

					} else {

						ReportManager.correct(this, con, String.format("Chromosome %s has no genes with logic name %s", seqRegion, logicName));
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

