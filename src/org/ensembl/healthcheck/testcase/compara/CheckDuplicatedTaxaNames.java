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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.AbstractComparaTestCase;
import org.ensembl.healthcheck.util.DBUtils;


/**
 * Check that the ncbi_taxa_name doesn't contain any duplicated rows
 */

public class CheckDuplicatedTaxaNames extends AbstractComparaTestCase {

	/**
	 * Create a new instance of CheckDuplicatedTaxaNames
	 */
	public CheckDuplicatedTaxaNames() {
		setDescription("Check that the ncbi_taxa_name doesn't contain any duplicated rows");
		setTeamResponsible(Team.COMPARA);
	}

	public boolean run(DatabaseRegistryEntry comparaDbre) {

		boolean result = true;
		Connection comparaCon = comparaDbre.getConnection();

		//Check that don't have duplicate entries in the ncbi_taxa_name table
		String useful_sql = "SELECT taxon_id,name,name_class,count(*) FROM ncbi_taxa_name GROUP BY taxon_id,name,name_class HAVING count(*) > 1;";
		String[] failures = DBUtils.getColumnValues(comparaCon, useful_sql);
		if (failures.length > 0) {
			ReportManager.problem(this, comparaCon, "FAILED ncbi_taxa_name contains duplicate entries ");
			ReportManager.problem(this, comparaCon, "FAILURE DETAILS: There are " + failures.length + " ncbi_taxa_names with more than 1 entry");
			ReportManager.problem(this, comparaCon, "USEFUL SQL: " + useful_sql);
			result = false;
		} else {
			result = true;
		}
		return result;
	}

} // CheckDuplicatedTaxaNames
