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

package org.ensembl.healthcheck.testcase.eg_compara;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.compara.CheckSynteny;

/**
 * Override of {@link CheckSynteny} which does not assume the lack of
 * rows in synteny_region is a failure.
 * 
 * @author ayates
 */
public class EGCheckSynteny extends CheckSynteny {
	
  public EGCheckSynteny() {
  	removeFromAllGroups();
    setDescription("Check for missing syntenies in the compara database.");
    setTeamResponsible(Team.ENSEMBL_GENOMES);
  }
	
	public boolean run(DatabaseRegistryEntry dbre) {
		Connection con = dbre.getConnection();
		if (!tableHasRows(con, "synteny_region")) {
			return true;
		}
		return super.run(dbre);
	}
}
