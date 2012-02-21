/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * Test to see if Uniprot misspelling is present in descriptions
 * 
 * @author dstaines
 * 
 */
public class DbDisplayNameUniProt extends AbstractEgCoreTestCase {

	private final static String QUERY = "select db_display_name from external_db where db_display_name like binary ?";
	private final static String[] TERMS = { "%SPTREMBL%", "%SWISSPROT%",
			"%Uniprot%", "%UniProt/%" };

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String term : TERMS) {
			for (String entry : template.queryForDefaultObjectList(QUERY,
					String.class, term)) {
				passes = false;
				ReportManager.problem(this, dbre.getConnection(),
						"An external_db entry has the display name " + entry
								+ " which is incorrect");
			}
		}
		return passes;
	}
}
