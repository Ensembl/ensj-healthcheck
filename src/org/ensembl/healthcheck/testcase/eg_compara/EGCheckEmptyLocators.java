package org.ensembl.healthcheck.testcase.eg_compara;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;

public class EGCheckEmptyLocators extends AbstractTemplatedTestCase {

	public EGCheckEmptyLocators() {
		setTeamResponsible(Team.ENSEMBL_GENOMES);
		appliesToType(DatabaseType.COMPARA);
		setDescription("Checks the consistency of GenomeDB locators");
		addToGroup("ensembl_genomes_compara");
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean pass = true;
		String sql = "select name from genome_db where locator IS NOT NULL";
		List<String> names = getTemplate(dbre).queryForDefaultObjectList(sql, String.class);
		if(! names.isEmpty()) {
			String joinedNames = StringUtils.join(names, ',');
			int count = names.size();
			String message = String.format("%d GenomeDB(s) [%s] did not have empty locators", count, joinedNames);
			ReportManager.problem(this, dbre.getConnection(), message);
			pass = false;
		}
		return pass;
	}
}
