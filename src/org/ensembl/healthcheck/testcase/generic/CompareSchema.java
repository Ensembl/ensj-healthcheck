/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2017] EMBL-European Bioinformatics Institute
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
 * Copyright (C) 2011 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.healthcheck.testcase.generic;

import static org.ensembl.healthcheck.DatabaseType.CDNA;
import static org.ensembl.healthcheck.DatabaseType.CORE;
import static org.ensembl.healthcheck.DatabaseType.OTHERFEATURES;
import static org.ensembl.healthcheck.DatabaseType.RNASEQ;
import org.ensembl.healthcheck.Team;
import static org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema.TestTypes.CHARSET;
import static org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema.TestTypes.ENGINE;
import static org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema.TestTypes.IGNORE_AUTOINCREMENT_OPTION;

import java.util.EnumSet;
import java.util.Set;

/**
 * Extension of the compare schema code for working with core like databases
 */
public class CompareSchema extends AbstractCompareSchema {
	
	@Override
	protected void addResponsible() {
		setTeamResponsible(Team.GENEBUILD);
                setSecondTeamResponsible(Team.RELEASE_COORDINATOR);
	}
	
	@Override
	protected boolean skipCheckingIfTablesAreUnequal() {
	  return false;
	}

	@Override
	protected void addTestTypes() {
		Set<TestTypes> tt = EnumSet.of(IGNORE_AUTOINCREMENT_OPTION, CHARSET, ENGINE);
		getTestTypes().addAll(tt);
	}

	@Override
	public void types() {
		addAppliesToType(CORE);
		addAppliesToType(CDNA);
		addAppliesToType(OTHERFEATURES);
		addAppliesToType(RNASEQ);
	}

	@Override
	protected String getDefinitionFileKey() {
		return "schema.file";
	}
	
	@Override
	protected String getMasterSchemaKey() {
		return "master.schema";
	}
}

