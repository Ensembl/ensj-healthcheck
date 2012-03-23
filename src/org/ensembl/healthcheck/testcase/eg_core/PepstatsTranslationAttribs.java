/**
 * TranslationAttribs
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractIntegerTestCase;

/**
 * @author dstaines
 * 
 */
public class PepstatsTranslationAttribs extends AbstractIntegerTestCase {

	/**
	 * 
	 */
	public PepstatsTranslationAttribs() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#getSql()
	 */
	@Override
	protected String getSql() {
		return "select count(*) from translation t join transcript using (transcript_id) "
				+ "where biotype='protein_coding' and translation_id not in "
				+ "(select translation_id from translation_attrib ta join attrib_type at using (attrib_type_id) "
				+ "where at.code in ('IsoPoint','Charge','MolecularWeight','NumResidues','AvgResWeight'))";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.AbstractIntegerTestCase#testValue(int)
	 */
	@Override
	protected boolean testValue(int value) {
		return value==0;
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
		return "Translations without pepstats attribs found";
	}

}
