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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;

/**
 * Check that if the start and end of translation is on the same exon, that start < end. Also check that translation ends aren't
 * beyond exon ends.
 */
public class TranslationStartEnd extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckTranslationStartEnd
	 */
	public TranslationStartEnd() {
		addToGroup("post_genebuild");
		addToGroup("release");
		setDescription("Check that if the start and end of translation is on the same exon, that start < end. Also check that translation ends aren't beyond exon ends.");
                setTeamResponsible("GeneBuilders");
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

		// check start < end
		Connection con = dbre.getConnection();
		int rows = getRowCount(con, "SELECT COUNT(translation_id) FROM translation WHERE start_exon_id = end_exon_id AND seq_start > seq_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations have start > end");
		} else {
			ReportManager.correct(this, con, "No translations have start > end");
		}

		// check no translations overrun their exons
		rows = getRowCount(con, "SELECT COUNT(*) FROM translation t, exon e WHERE t.end_exon_id=e.exon_id AND e.seq_region_end-e.seq_region_start+1 < t.seq_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations end beyond the end of their exons");
		} else {
			ReportManager.correct(this, con, "No translations overrun exons");
		}

		return result;

	} // run

} // TranslationStartEnd
