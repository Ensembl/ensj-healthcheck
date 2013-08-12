package org.ensembl.healthcheck.testcase.generic;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

/**
 * Test to see if schema uses non-myisam storage engines
 * 
 * @author dstaines
 * 
 */
public class MySQLStorageEngine extends AbstractTemplatedTestCase {

	private final static String MYISAM = "MyISAM";

	private final static String ENGINE_QUERY = "select count(*) from information_schema.tables where table_schema=? and engine<>'" + MYISAM + "'";

	public MySQLStorageEngine() {

                addToGroup("pre-compara-handover");
		addToGroup("post-compara-handover");
                setTeamResponsible(Team.RELEASE_COORDINATOR);
	}

	public void types() {
		this.addAppliesToType(DatabaseType.CORE);
		this.addAppliesToType(DatabaseType.VARIATION);
		this.addAppliesToType(DatabaseType.FUNCGEN);
		this.addAppliesToType(DatabaseType.COMPARA);
                this.addAppliesToType(DatabaseType.CDNA);
                this.addAppliesToType(DatabaseType.OTHERFEATURES);
                this.addAppliesToType(DatabaseType.RNASEQ);
                this.addAppliesToType(DatabaseType.VEGA);
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		int count = getTemplate(dbre).queryForDefaultObject(ENGINE_QUERY, Integer.class, dbre.getName());
		if (count > 0) {
			ReportManager.problem(this, dbre.getConnection(), count + " tables from " + dbre.getName() + " do not use " + MYISAM);
			return false;
		} else {
			return true;
		}
	}

}
