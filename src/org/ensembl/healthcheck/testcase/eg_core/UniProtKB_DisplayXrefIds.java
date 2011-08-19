package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;


/**
 * Created by IntelliJ IDEA.
 * User: axk
 * Date: 15/03/2011
 * Time: 10:21
 */
public class UniProtKB_DisplayXrefIds extends AbstractRowCountTestCase {

	public UniProtKB_DisplayXrefIds() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}


	private final static String QUERY =
            "SELECT count(*) " +
            "FROM gene g, xref x, external_db d " +
            "WHERE g.display_xref_id = x.xref_id AND x.external_db_id = d.external_db_id " +
            "AND d.db_name IN ('Uniprot/SPTREMBL','Uniprot/SPTREMBL_predicted','Uniprot/SWISSPROT','Uniprot/SWISSPROT_predicted')";

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractRowCountTestCase#getExpectedCount
	 * ()
	 */
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
		return QUERY;
	}

    @Override
	protected String getErrorMessage() {
		return "$actual$ gene display_xref_ids attached to Uniprot/*, instead of Uniprot_genename";
	}


}
