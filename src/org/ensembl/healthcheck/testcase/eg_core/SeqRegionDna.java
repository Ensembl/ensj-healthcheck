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

/**
 * File: SeqRegionDna.java
 * Created by: ckong
 * Created on: Sept 18, 2014
 * 
 * CVS:  $$
 */

package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test for seq_regions that are in a 'sequence_level' coordinate system 
 *  (having the cs.attrib of 'sequence_level') but 
 *  DON'T have corresponding rows in the dna table.
 * 
 * @author ckong
 */
public class SeqRegionDna extends AbstractRowCountTestCase {

	public SeqRegionDna() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		this.setDescription("TODO");
		this.removeAppliesToType(DatabaseType.OTHERFEATURES);
	}

	@Override
	protected int getExpectedCount() {
		return 0;
	}

	@Override
	protected String getSql() {
		return "SELECT count(*) FROM seq_region "
				+ "LEFT JOIN dna USING (seq_region_id) "
				+ "WHERE coord_system_id IN "
				+ "(SELECT coord_system_id FROM coord_system "
				+ "WHERE attrib RLIKE 'sequence_level') "
				+ "AND dna.seq_region_id IS NULL";
	}

	@Override
	protected String getErrorMessage(int value) {
		return value+" sequence_level seq_regions have no dna table entry: "+getSql();
	}

}
