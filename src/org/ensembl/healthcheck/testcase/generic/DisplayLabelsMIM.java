package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

public class DisplayLabelsMIM extends AbstractIntegerTestCase{

	public DisplayLabelsMIM() {
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.CORE);
		this.setDescription("Tests for improper import of MIM data.");
	}
	
	protected String getSql (){
		return "SELECT x.display_label, count(x.xref_id) FROM xref x, external_db e WHERE e.`db_name` "
				+ "IN ('MIM','MIM_MORBID','MIM_GENE') AND e.external_db_id = x.external_db_id "
				+ "AND x.display_label REGEXP '\\n'";
	}
	
	protected boolean testValue(int count) {
		if (count == 0) { return true; }
		return false;
	}
	
	protected String getErrorMessage (int count) {
		return "Found linefeeds in MIM xrefs";
	}
}
