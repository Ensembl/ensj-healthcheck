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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that no genes are on a sequence_level coord system. Single-coord system species are ignored.
 */
public class GeneCoordSystem extends SingleDatabaseTestCase {

	/**
	 * Check that no genes are on a sequence_level coord system.
	 */
	public GeneCoordSystem() {

		addToGroup("release");
		addToGroup("post_genebuild");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
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
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
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
			int rows = getRowCount(
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

		int rows = getRowCount(con, "SELECT COUNT(*) FROM meta WHERE meta_key = 'genebuild.level' and meta_value = 'toplevel'");
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
