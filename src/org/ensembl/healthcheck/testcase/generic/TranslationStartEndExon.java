/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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
public class TranslationStartEndExon extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckTranslationStartEndExon
	 */
	public TranslationStartEndExon() {
		
		setDescription("Check if the start- and end-exon mentioned in the translation-table exist in the database, too. ");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * This only applies to core databases.
	 */
	public void types() {

		setAppliesToType(DatabaseType.CORE);

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
