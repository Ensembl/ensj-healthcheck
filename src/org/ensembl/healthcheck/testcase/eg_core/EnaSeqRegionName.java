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

/**
 * EnaSeqRegionName
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

/**
 * @author dstaines
 * 
 */
public class EnaSeqRegionName extends AbstractIntegerTestCase {

	public EnaSeqRegionName() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		this.setDescription("Checks to see is seq_regions are annotated as ENA entries");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return "select count(*) from seq_region s join seq_region_attrib sa using (seq_region_id) "
				+ "join attrib_type at using (attrib_type_id) where s.name like '%.%' AND sa.value='ENA' and at.code='external_db'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#testValue(int)
	 */
	@Override
	protected boolean testValue(int value) {
		return value > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getErrorMessage
	 * ()
	 */
	@Override
	protected String getErrorMessage(int count) {
		return "No seq_regions annotated as ENA found "
				+ "- must have a name of the form %.% and a seq_region_attrib of type 'external_db' and value 'ENA'";
	}


}
