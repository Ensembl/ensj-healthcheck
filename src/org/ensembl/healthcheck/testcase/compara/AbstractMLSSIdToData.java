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


package org.ensembl.healthcheck.testcase.compara;

import java.sql.Connection;

/**
 * An abstract EnsEMBL Healthcheck test case that implements a method to
 * look for mlss_ids that are not linked to a given table
 */

public abstract class AbstractMLSSIdToData extends AbstractComparaTestCase {

	public boolean checkMLSSIdLink(Connection con, String dataTableName, String constraintOnMLSS) {
		return checkMLSSIdLink(con, dataTableName, constraintOnMLSS, true);
	}

	public boolean checkMLSSIdLink(Connection con, String dataTableName, String constraintOnMLSS, boolean checkOrphanedData) {
		boolean result = true;
		result &= checkForOrphansWithConstraint(con, "method_link_species_set", "method_link_species_set_id", dataTableName, "method_link_species_set_id", constraintOnMLSS);
		if (checkOrphanedData) {
			result &= checkForOrphans(con, dataTableName, "method_link_species_set_id", "method_link_species_set", "method_link_species_set_id");
		}
		return result;
	}

} // AbstractMLSSIdToData
