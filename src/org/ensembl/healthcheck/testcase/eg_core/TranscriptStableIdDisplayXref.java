/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

/**
 * Test to see if we're using display_xrefs that are the same as the stable_id
 * 
 * @author dstaines
 * 
 */
public class TranscriptStableIdDisplayXref extends AbstractIntegerTestCase {

	public TranscriptStableIdDisplayXref() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
		this.setFix("update transcript t, xref x set t.display_xref_id=NULL "
				+ "where t.display_xref_id=x.xref_id and x.display_label=t.stable_id");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return "select count(*) from transcript g join xref x on (g.display_xref_id=x.xref_id) where x.display_label=g.stable_id";
	}

	@Override
	protected String getErrorMessage(int count) {
		return count + " transcripts found with stable_id set as display_xrefs";
	}

	@Override
	protected boolean testValue(int value) {
		return value == 0;
	}


}
