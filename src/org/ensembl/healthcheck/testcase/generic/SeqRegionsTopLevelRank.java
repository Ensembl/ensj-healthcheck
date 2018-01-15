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

/*
 * Copyright (C) 2003 EBI, GRL
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
 * Check that we have at least 1 coord_system with rank=1
 */

public class SeqRegionsTopLevelRank extends SingleDatabaseTestCase {

	/**
	 * Create a new SeqRegionsTopLevel testcase.
	 */
	public SeqRegionsTopLevelRank() {

		setDescription("Check that all seq_regions comprising genes are marked as toplevel in seq_region_attrib, and that there is at least one toplevel seq_region. Also check that all toplevel seq regions are marked as such, and no seq regions that are marked as toplevel are not toplevel. Will check as well if the toplevel seqregions have information in the assembly table");
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
	 *            The database to use.
	 * @return true if the test passed.
	 * 
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		return checkRankOne(dbre);

	} // run


	// --------------------------------------------------------------------------

	private boolean checkRankOne(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		// check that there is one co-ordinate system with rank = 1
		if (!dbre.isMultiSpecies()) {

			int rows = DBUtils.getRowCount(con,
					"SELECT COUNT(*) FROM coord_system WHERE rank=1");
			if (rows == 0) {

				ReportManager.problem(this, con,
						"No co-ordinate systems have rank = 1");
				result = false;

			} else if (rows > 1) {

				if (rows != dbre.getSpeciesIds().size()) {
					ReportManager
							.problem(
									this,
									con,
									rows
											+ " rows in coord_system have a rank of 1. There should be "
											+ dbre.getSpeciesIds().size());
					result = false;
				} else {
					ReportManager.correct(this, con, dbre.getSpeciesIds()
							.size() + " co-ordinate systems with rank = 1");
				}

			} else {

				ReportManager.correct(this, con,
						"One co-ordinate system has rank = 1");

			}
		}
		return result;

	}

} // SeqRegionsTopLevel
