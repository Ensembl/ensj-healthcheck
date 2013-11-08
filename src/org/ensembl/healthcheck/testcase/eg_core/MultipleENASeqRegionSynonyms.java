package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

public class MultipleENASeqRegionSynonyms extends AbstractRowCountTestCase {

	private final String MULTIPLE_SYNONYMS = "select count(*) from (select seq_region_id from coord_system "
			+ "join seq_region using (coord_system_id) "
			+ "join seq_region_synonym using (seq_region_id) "
			+ "join external_db using (external_db_id) "
			+ "join seq_region_attrib using (seq_region_id) "
			+ "join attrib_type using (attrib_type_id) "
			+ "where db_name='EMBL' and code='toplevel' "
			+ "group by seq_region_id having count(*)>1) s";

	public MultipleENASeqRegionSynonyms() {
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		setDescription("Test to find toplevel seq_regions which have multiple EMBL synonyms");
	}

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
		return MULTIPLE_SYNONYMS;
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
		return count
				+ " top_level seq_regions found with multiple EMBL seq_region_synonym entries: "
				+ getSql();
	}

}
