/**
 * UniprotGeneNameObjectXref
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractRowCountTestCase;

/**
 * Test to find uniprot gene names as object_xrefs (they should only ever be
 * used for display xrefs)
 * 
 * @author dstaines
 * 
 */
public class UniprotGeneNameObjectXref extends AbstractRowCountTestCase {

	public UniprotGeneNameObjectXref() {
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

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
		return "select count(*) from object_xref o,  xref x, external_db e where o.xref_id=x.xref_id and x.external_db_id=e.external_db_id and e.db_name='Uniprot_genename'";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getErrorMessage
	 * ()
	 */
	@Override
	protected String getErrorMessage() {
		return "Found more than "
				+ getExpectedCount()
				+ " Uniprot_genename entries in object_xref - these should be removed from object_xref";
	}

}
