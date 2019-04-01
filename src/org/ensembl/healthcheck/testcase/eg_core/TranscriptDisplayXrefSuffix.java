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
 * File: TranscriptDisplayXrefSuffix.java
 * Created by: jallen
 * Created on: June 04, 2014
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

/**
 * Test to see if a transcript has a display xref with a '-20*' suffix.
 * This is an Ensembl-specific thing which the xref pipeline does unless
 * you explicitly tell it not too.
 * cf: http://www.ebi.ac.uk/seqdb/confluence/display/EnsGen/Xref+mapping#Xrefmapping-CustomisingXrefMapping(DisplayXrefs)
 * 
 * @author jallen
 * 
 */
public class TranscriptDisplayXrefSuffix extends AbstractIntegerTestCase {

	public TranscriptDisplayXrefSuffix() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return
      "select count(distinct t.stable_id) from transcript t inner join xref x on t.display_xref_id = x.xref_id " +
      "where x.dbprimary_acc regexp '-20[[:digit:]]$' and x.dbprimary_acc = x.display_label";
	}

	@Override
	protected String getErrorMessage(int count) {
		return count + " transcripts found with display xrefs that have a '-20*' suffix";
	}

	@Override
	protected boolean testValue(int value) {
		return value == 0;
	}


}
