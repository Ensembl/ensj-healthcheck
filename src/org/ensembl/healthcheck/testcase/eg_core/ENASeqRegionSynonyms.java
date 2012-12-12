package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

public class ENASeqRegionSynonyms extends AbstractRowCountTestCase {

	public ENASeqRegionSynonyms() {
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		setDescription("Test to find toplevel seq_regions which do not have EMBL synonyms");
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
		return "select count(distinct(seq_region_id)) " +
				"from seq_region s " +
				"join seq_region_attrib sa using (seq_region_id) " +
				"join attrib_type a using (attrib_type_id) " +
				"where a.code='toplevel' and s.seq_region_id not in " +
				"(select seq_region_id from seq_region_synonym srs " +
				"join external_db e using (external_db_id) where e.db_name='EMBL')";
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
				+ " top_level seq_regions found without EMBL seq_region_synonym entries";
	}
	
	
	
}
