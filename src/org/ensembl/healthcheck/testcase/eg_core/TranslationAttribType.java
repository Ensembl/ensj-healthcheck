/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Set;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.TemplateBuilder;

/**
 * Check that translation_attribs are valid
 * 
 * @author dstaines
 * 
 */
public class TranslationAttribType extends AbstractEgCoreTestCase {

	private final static String QUERY = "select a.code from translation_attrib ta join attrib_type a "
			+ "using (attrib_type_id) where code not in ($inlist$)";

	private final String query;

	public TranslationAttribType() {
		super();
		query = TemplateBuilder
				.template(
						QUERY,
						"inlist",
						resourceToInList("/org/ensembl/healthcheck/testcase/eg_core/translation_attribs.txt"));
	}

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		Set<String> unknownCodes = CollectionUtils.createHashSet();
		for (String code : getTemplate(dbre).queryForDefaultObjectList(query,
				String.class)) {
			passes = false;
			unknownCodes.add(code);
		}
		for (String code : unknownCodes) {
			ReportManager.problem(this, dbre.getConnection(),
					"Translation attrib found with unexpected code " + code);
		}
		return passes;
	}

}
