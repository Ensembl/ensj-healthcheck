/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * An EnsEMBL Healthcheck test case that checks that
 * wga_coverage and goc_score thresholds are defined for some mlss_ids
 */

public class CheckOrthologQCThresholds extends SingleDatabaseTestCase {

	public CheckOrthologQCThresholds() {
		setDescription("Check that some wga_coverage and goc_score thresholds are defined");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();

		boolean result = true;

		boolean hasGOCScore = DBUtils.getColumnValues(con, "SELECT 1 FROM homology WHERE goc_score IS NOT NULL LIMIT 1").length > 0;
		if (hasGOCScore) {
			result &= checkCountIsNonZero(con, "method_link_species_set_attr", "goc_quality_threshold IS NOT NULL");
		} else {
			result &= checkCountIsZero(con, "method_link_species_set_attr", "goc_quality_threshold IS NOT NULL");
		}

		boolean hasWGAScore = DBUtils.getColumnValues(con, "SELECT 1 FROM homology WHERE wga_coverage IS NOT NULL LIMIT 1").length > 0;
		if (hasWGAScore) {
			result &= checkCountIsNonZero(con, "method_link_species_set_attr", "wga_quality_threshold IS NOT NULL");
		} else {
			result &= checkCountIsZero(con, "method_link_species_set_attr", "wga_quality_threshold IS NOT NULL");
		}

		return result;
	}

} // CheckOrthologQCThresholds
