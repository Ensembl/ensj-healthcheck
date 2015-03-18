package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

public class DescriptionXrefs extends AbstractIntegerTestCase {

	public DescriptionXrefs() {
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.CORE);
		this.setDescription("Tests for {ECO: } blocks from Uniprot in descriptions.");
	}
	@Override
	protected String getSql() {
		return "SELECT count(*) FROM xref x WHERE x.description like '%{ECO:%}%'";
	}

	@Override
	protected boolean testValue(int value) {
		if (value > 0) {return false;};
		return true;
	}

	@Override
	protected String getErrorMessage(int value) {
		return "ECO evidence codes found in "+value+" xref descriptions";
	}

}
