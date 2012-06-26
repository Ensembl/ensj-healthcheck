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
import org.ensembl.healthcheck.util.TemplateBuilder;

/**
 * Test to see if Uniprot misspelling is present in descriptions
 * 
 * @author dstaines
 * 
 */
public class GeneDescriptionUniProtSource extends AbstractEgCoreTestCase {

	private final static String QUERY = "select count(*) from $obj$ where description like binary ?";
	private final static String[] TERMS = { "%SPTREMBL%", "%SWISSPROT%",
			"%Uniprot%", "%UniProt/%" };
	private final static String[] OBJS = {"gene","transcript"};

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for(String obj: OBJS) {
			String query = TemplateBuilder.template(QUERY, "obj",obj);
		for(String term: TERMS) {
			int count = template.queryForDefaultObject(query,
				Integer.class,term);
			if(count>0) {
				passes= false;
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								count+" "+obj+"s have descriptions matching the incorrect name "+term);
			}
		}
		}
		return passes;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#getEgDescription()
	 */
	@Override
	protected String getEgDescription() {
		return "Test to see if Uniprot misspelling is present in descriptions";
	}
}
