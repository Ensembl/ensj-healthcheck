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
import org.ensembl.healthcheck.Species;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.Priority;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * Check that certain regions have a specific gene count.
 */
public class GeneCount extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of GeneCount
	 */
	public GeneCount() {

		setDescription("Check that certain regions have a specific gene count.");
		setPriority(Priority.AMBER);
		setEffect("Causes incorrect display of gene counts and confusing contigview displays.");
		setFix("Add/remove genes.");
		setTeamResponsible(Team.GENEBUILD);

	}

        /**
         * Data is only tested in core database, as the tables are in sync
         */
        public void types() {

                removeAppliesToType(DatabaseType.OTHERFEATURES);
                removeAppliesToType(DatabaseType.ESTGENE);
                removeAppliesToType(DatabaseType.RNASEQ);
                removeAppliesToType(DatabaseType.CDNA);

        }

	/**
	 * Run the test.
	 * 
	 * @param dbre
	 *            The database registry containing all the specified databases.
	 * @return true if test succeeds
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		// MT chromosome should have 13 protein coding genes, only applies to
		// core database
		if ( dbre.getType() == DatabaseType.CORE) {
			result &= countMTGenes(dbre.getConnection());
		}

		return result;

	} // run

	// -----------------------------------------------------------------------

	private boolean countMTGenes(Connection con) {

		boolean result = true;
                
                int MT = DBUtils.getRowCount( con, "SELECT COUNT(*) FROM seq_region WHERE name='MT'");   

                if (MT == 0) {
                        return result;
                }


		int genes = DBUtils
				.getRowCount(
						con,
						"SELECT COUNT(*) FROM coord_system cs, seq_region sr, seq_region_attrib sa, gene g WHERE cs.coord_system_id=sr.coord_system_id AND cs.attrib like 'default_version%' AND sr.name='MT' AND sr.seq_region_id=sa.seq_region_id AND sa.attrib_type_id=6 AND g.seq_region_id=sr.seq_region_id AND g.biotype='protein_coding'");

		if (genes != 13) {

			ReportManager.problem(this, con,
					"MT chromosome should have 13 protein coding genes, actually has "
							+ genes);
			result = false;

		} else {

			ReportManager.correct(this, con,
					"MT chromosome has 13 protein coding genes.");

		}

		return result;

	}

	// -----------------------------------------------------------------------

} // GeneCount

