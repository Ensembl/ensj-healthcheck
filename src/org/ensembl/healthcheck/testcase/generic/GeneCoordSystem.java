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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that no genes are on a sequence_level coord system. Single-coord system species are ignored.
 */
public class GeneCoordSystem extends SingleDatabaseTestCase {

	/**
	 * Check that no genes are on a sequence_level coord system.
	 */
	public GeneCoordSystem() {

		setDescription("Check that no genes are on a sequence_level coord system.");
		setPriority(Priority.AMBER);
		setEffect("Having genes on a sequence_level co-ordinate system will slow down the mapper and affect website speed and dumping speed.");
		setFix("Move affected genes off the sequence_level co-ordinate system");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *          The database to check.
	 * @return true if the test passes.
	 */
	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check buil level in Meta table, flag toplevel should be set

		result &= checkBuildLevel(dbre);

		// if flag set to toplevel, check all genes are in seq_region with attrib_type_id = toplevel
		if (result) {
			int rows = DBUtils.getRowCount(
					con,
					"select count(*) from gene where gene_id not in (select g.gene_id from gene g, seq_region_attrib sra, attrib_type a where g.seq_region_id = sra.seq_region_id and sra.attrib_type_id = a.attrib_type_id and a.code = 'toplevel')");

			if (rows > 0) {
				ReportManager.problem(this, con, rows + " genes are not on toplevel seq_regions.");
				result = false;
			} else {
				ReportManager.correct(this, con, "All genes on a top_level seq_regions");
			}
		}
		return result;

	} // run

	/**
	 * Check that at least some sort of genebuild.level-type key is present.
	 */
	private boolean checkBuildLevel(DatabaseRegistryEntry dbre) {

		boolean result = false;

		Connection con = dbre.getConnection();

		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key = 'genebuild.level' and meta_value = 'toplevel'");
		if (rows >= 1) {
			ReportManager.correct(this, con, " Toplevel flag set in genebuild.level in Meta table");
			result = true;
		} else {
			ReportManager.problem(this, con, "No genebuild.level key in the meta table with a value of toplevel");
		}

		return result;

	}

	// -----------------------------------------------------------------

} // GeneCoordSystem
