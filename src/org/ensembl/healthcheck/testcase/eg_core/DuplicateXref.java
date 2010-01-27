/**
 * File: DisplayXrefIdTest.java
 * Created by: dstaines
 * Created on: May 27, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

/**
 * Test for where xrefs have been added twice
 * @author dstaines
 *
 */
public class DuplicateXref extends AbstractEgCoreTestCase {

	private final static String DUPLICATE_XREF = "select count(*) from (select count(*) from xref x group by x.dbprimary_acc,x.external_db_id having count(*)>1) cc";
	private final static String DUPLICATE_OBJ_XREF = "select count(*) from (select count(*) from xref x join object_xref ox using (xref_id) group by ox.ensembl_id, ox.ensembl_object_type,x.dbprimary_acc,x.external_db_id having count(*)>1) cc";

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		int nDupX =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_XREF, Integer.class);
		if(nDupX>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupX+" duplicates found in xref");
		}
		int nDupOX =  getTemplate(dbre).queryForDefaultObject(DUPLICATE_OBJ_XREF, Integer.class);
		if(nDupOX>0) {
			passes = false;
			ReportManager.problem(this, dbre.getConnection(), nDupOX+" duplicates found in object_xref");
		}
		return passes;
	}

}
