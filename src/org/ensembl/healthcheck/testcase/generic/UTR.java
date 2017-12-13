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

/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.text.DecimalFormat;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Checks that changes between releases (assembly, repeatmasking or gene set)
 * have been declared.
 */

public class UTR extends SingleDatabaseTestCase {

	public UTR() {

		setTeamResponsible(Team.GENEBUILD);
		setDescription("Check that coding transcripts have UTR attached");
	}

	/**
	 * This test applies only to core dbs
	 */
	public void types() {
		removeAppliesToType(DatabaseType.SANGER_VEGA);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.CDNA);
		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.RNASEQ);
	}

	/**
	 * Look for UTRs
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passed.
	 */

	public boolean run(final DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		result &= countUTR(dbre);

		return result;
	}

	private boolean countUTR(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		String utr_sql = "SELECT count(distinct(gene_id)) FROM exon e, exon_transcript et, transcript t WHERE e.exon_id=et.exon_id AND et.transcript_id=t.transcript_id AND t.biotype = 'protein_coding' AND phase = -1";
		int utrTranscript = DBUtils.getRowCount(con, utr_sql);
		String coding_sql = "SELECT count(distinct(gene_id)) FROM transcript WHERE biotype = 'protein_coding'";
		int codingTranscript = DBUtils.getRowCount(con, coding_sql);

		double percentage = (((double) utrTranscript / (double) codingTranscript) * 100);
		float comp = Float.valueOf(twoDForm.format(percentage));

		if (comp < 50) {
			ReportManager.info(this, con, "Only " + comp + " % coding transcripts have UTRs");
		}

		return result;
	}

}
