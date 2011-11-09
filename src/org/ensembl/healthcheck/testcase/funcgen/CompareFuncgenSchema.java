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

package org.ensembl.healthcheck.testcase.funcgen;

import java.util.EnumSet;
import java.util.Set;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.generic.AbstractCompareSchema;

/**
 * Extension of the compare schema functionality customised to run only
 * on funcgen databases
 */
public class CompareFuncgenSchema extends AbstractCompareSchema {

	@Override
	protected void addGroups() {
		addToGroup("funcgen");
		addToGroup("funcgen-release");
		addToGroup("release");
	}
	
	@Override
	public void types() {
		addAppliesToType(DatabaseType.FUNCGEN);
	}

	@Override
	protected void addResponsible() {
		setTeamResponsible(Team.FUNCGEN);
	}

	@Override
	protected void addTestTypes() {
		Set<TestTypes> types = EnumSet.allOf(TestTypes.class);
		getTestTypes().addAll(types);
	}

	@Override
	protected String getDefinitionFileKey() {
		return "funcgen_schema.file";
	}

	@Override
	protected String getMasterSchemaKey() {
		return "master.funcgen_schema";
	}

}
