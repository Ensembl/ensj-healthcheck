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
import static org.ensembl.healthcheck.DatabaseType.EST;
import static org.ensembl.healthcheck.DatabaseType.ESTGENE;
import static org.ensembl.healthcheck.DatabaseType.OTHERFEATURES;
import static org.ensembl.healthcheck.DatabaseType.RNASEQ;
import static org.ensembl.healthcheck.DatabaseType.SANGER_VEGA;
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
	protected void addGroups() {
		addToGroup("release");
		addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");		
	}

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
		addAppliesToType(EST);
		addAppliesToType(ESTGENE);
		addAppliesToType(OTHERFEATURES);
		addAppliesToType(RNASEQ);
		addAppliesToType(SANGER_VEGA);
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

