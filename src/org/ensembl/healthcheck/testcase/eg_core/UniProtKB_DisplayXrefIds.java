package org.ensembl.healthcheck.testcase.eg_core;

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
		addToGroup(AbstractEgCoreTestCase.EG_GROUP);
	}


    /*

    public boolean run(DatabaseRegistryEntry dbre) {

		boolean passes = false;
		try {
			passes = runTest(dbre);
			if (passes) {
				ReportManager
						.correct(this, dbre.getConnection(), "Test passed");
			}
            else {
                String msg = "gene display_xref_ids attached to UniProt, instead of Uniprot_genename";
                problem(this, dbre.getConnection(), msg);
            }
		} catch (Throwable e) {
			e.printStackTrace();
			ReportManager
					.problem(this, dbre.getConnection(),
							"Test failed due to unexpected exception "
									+ e.getMessage());
		}
		return passes;
	}
    */

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
