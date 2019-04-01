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

/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to see if we have any protein_coding transcripts shorter than 3 bp
 * 
 * @author dstaines
 * 
 */
public class SubCodonTranscript extends AbstractRowCountTestCase {

	public SubCodonTranscript() {
		super();
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.GENEBUILD);
        this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		this.setDescription("Test to see if we have any protein_coding transcripts shorter than 3 bp");
	}

	private final static String QUERY = "select count(*) from transcript "
	        + "where biotype='protein_coding' and abs(CAST(seq_region_end AS SIGNED)-CAST(seq_region_start AS SIGNED))<2";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
	 * ()
	 */
	@Override
	protected int getExpectedCount() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return QUERY;
	}

	@Override
	protected String getErrorMessage(int count) {
		return count+" transcripts found shorter than 3 basepairs";
	}


}
