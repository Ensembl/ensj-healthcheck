/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that there are no translations for pseudogenes.
 */

public class Pseudogene extends SingleDatabaseTestCase {

	/**
	 * Check the assembly_exception table.
	 */
	public Pseudogene() {
		
		setDescription("Check that there are no translations for pseudogenes");
		setTeamResponsible(Team.GENEBUILD);
	}

	/**
	 * Check the data in the assembly_exception table. Note referential integrity checks are done in CoreForeignKeys.
	 * 
	 * @param dbre
	 *          The database to use.
	 * @return Result.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		String qry = "select count(*) from gene,transcript,translation " + "where gene.biotype like '%pseudogene%'" + " and transcript.gene_id=gene.gene_id "
				+ " and translation.transcript_id=transcript.transcript_id and gene.biotype!= 'polymorphic_pseudogene' ";
		if (dbre.getType() == DatabaseType.SANGER_VEGA) {// for sangervega ignore genes that do not have source havana or WU and allow
																											// polymorphic_pseudogene to have translations
			qry += " and (gene.source='havana' or gene.source='WU')";
		}
    if (dbre.getType() == DatabaseType.SANGER_VEGA ||
        dbre.getType() == DatabaseType.VEGA) {
      // Vega allows translations on translated_processed_pseudogene-s
      qry += " and gene.biotype != 'translated_processed_pseudogene'";
    }

		int rows = DBUtils.getRowCount(con, qry);
		if (rows > 0) {
			result = false;
			ReportManager.problem(this, con, "Translation table contains " + rows + " rows for pseudogene types - should contain none");
		}

		if (result) {
			ReportManager.correct(this, con, "No pseudogenes have translations");
		}

		return result;

	}

	/**
	 * This applies to 'core and 'vega' core schema databases
	 */
	public void types() {

		removeAppliesToType(DatabaseType.OTHERFEATURES);
		removeAppliesToType(DatabaseType.ESTGENE);
		removeAppliesToType(DatabaseType.VEGA);
		removeAppliesToType(DatabaseType.RNASEQ);

	}

} // Pseudogene
