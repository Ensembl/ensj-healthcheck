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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that if the start and end of translation is on the same exon, that start < end. Also check that translation ends aren't
 * beyond exon ends.
 */
public class TranslationStartEndExon extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckTranslationStartEndExon
	 */
	public TranslationStartEndExon() {
		
		addToGroup("post_genebuild");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check if the start- and end-exon mentioned in the translation-table exist in the database, too. ");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core and Vega databases.
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

	/**
	 * Find any matching databases that have start > end.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// check if the start_exon of a translation exists in the exon_table

		Connection con = dbre.getConnection();
		int rows = DBUtils.getRowCount(con, " SELECT COUNT(*) FROM translation tl  LEFT JOIN exon e ON e.exon_id=tl.start_exon_id WHERE e.exon_id IS NULL");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations refer to an start_exon which doesn't exist ");
		} else {
			ReportManager.correct(this, con, "All translations refer to existing start_exons");
		}

		// check if the end_exon of a translation exists in the exon_table
		rows = DBUtils.getRowCount(con, " SELECT COUNT(*) FROM translation tl  LEFT JOIN exon e ON e.exon_id=tl.end_exon_id WHERE e.exon_id IS NULL");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations refer to an end_exon which doesn't exist in the exon-table");
		} else {
			ReportManager.correct(this, con, "All translations refer to existing end_exons");
		}

		return result;
	} // run

} // TranslationStartEndExon
