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
 * Check that all transcripts of genes of protein coding genes translate.
 */

public class TranscriptsTranslate extends SingleDatabaseTestCase {

	/**
	 * Create a new TranscriptsTranslate testcase.
	 */
	public TranscriptsTranslate() {

		addToGroup("post_genebuild");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
                addToGroup("post-projection");
		
		setDescription("Check that all transcripts of protein_coding genes translate");
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
	 *          The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String sql = "SELECT COUNT(*) FROM gene g, transcript tr LEFT JOIN translation t ON t.transcript_id=tr.transcript_id WHERE t.translation_id IS NULL AND g.gene_id=tr.gene_id and g.biotype=\'protein_coding\'";
		// protein_coding genes for sangervega only need at least 1 transcript with a translation, they can have transcripts without
		// translations in addition to that
		// for sangervega ignore genes that do not have source havana or WU
		if (dbre.getType() == DatabaseType.SANGER_VEGA || dbre.getType() == DatabaseType.VEGA) {
			// sql +=" and (g.source='havana' or g.source='WU')";
			sql = "select count(*) from gene g where g.biotype='protein_coding' and g.gene_id NOT IN(select tr.gene_id  from transcript tr JOIN translation t ON t.transcript_id=tr.transcript_id) and (g.source='havana' or g.source='WU')  and g.gene_id NOT IN (select gene_id from gene_attrib join attrib_type using (attrib_type_id) where code = 'NoTransRefError')";
		}

		int rows = DBUtils.getRowCount(con, sql);
		if (rows != 0) {

			ReportManager.problem(this, con, rows + " transcript(s) in protein_coding genes do not have translations.");
			result = false;

		} else {

			ReportManager.correct(this, con, "All transcripts of protein_coding genes have translations");

		}

		return result;

	} // run

} // TranscriptsTranslate
