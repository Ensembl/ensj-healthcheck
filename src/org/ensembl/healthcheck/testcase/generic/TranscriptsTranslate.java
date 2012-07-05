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
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
		
		setDescription("Check that all transcripts of protein_coding genes translate");
		setTeamResponsible(Team.GENEBUILD);

	}

	/**
	 * This only really applies to core databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.VEGA);
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
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {
			// sql +=" and (g.source='havana' or g.source='WU')";
			sql = "select count(*) from gene g where g.biotype='protein_coding' and g.gene_id NOT IN(select tr.gene_id  from transcript tr JOIN translation t ON t.transcript_id=tr.transcript_id) and (g.source='havana' or g.source='WU')";
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
