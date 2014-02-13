/*
 * Copyright [1999-2014] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
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
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

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
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
                addToGroup("post-projection");
		
		setDescription("Check that if the start and end of translation is on the same exon, that start < end. Also check that translation ends aren't beyond exon ends.");
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

		// check start < end
		Connection con = dbre.getConnection();
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(translation_id) FROM translation WHERE start_exon_id = end_exon_id AND seq_start > seq_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations have start > end");
		} else {
			ReportManager.correct(this, con, "No translations have start > end");
		}

		// check no translations overrun their exons
		rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM translation t, exon e WHERE t.end_exon_id=e.exon_id AND cast(e.seq_region_end as signed int)-cast(e.seq_region_start as signed int)+1 < t.seq_end");
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, rows + " translations end beyond the end of their exons");
		} else {
			ReportManager.correct(this, con, "No translations overrun exons");
		}

                // check the start and end exon have a correct phase
                rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM translation t, exon e WHERE t.start_exon_id=e.exon_id AND start_exon_id <> end_exon_id and end_phase = -1");
                if (rows > 0) {
                        result = false;
                        ReportManager.problem(this, con, rows + " translations have start exon with a -1 end phase");
                } else {
                        ReportManager.correct(this, con, "Start exons for translations have correct end phase");
                }

                rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM translation t, exon e WHERE t.end_exon_id=e.exon_id AND start_exon_id <> end_exon_id and phase = -1");
                if (rows > 0) {
                        result = false;
                        ReportManager.problem(this, con, rows + " translations have end exon with -1 phase");
                } else {
                        ReportManager.correct(this, con, "End exons for translations have correct phase");
                }


		return result;

	} // run

} // TranslationStartEnd
