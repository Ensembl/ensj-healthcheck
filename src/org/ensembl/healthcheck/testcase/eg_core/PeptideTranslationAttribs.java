/**
 * File: GeneDescriptionSourceTest.java
 * Created by: dstaines
 * Created on: May 26, 2009
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.TemplateBuilder;

/**
 * Check that translation_attribs are valid
 * 
 * @author dstaines
 * 
 */
public class PeptideTranslationAttribs extends AbstractEgCoreTestCase {

	private final static String QUERY = "select a.code,count(*) from translation_attrib ta join attrib_type a "
			+ "using (attrib_type_id) where code in ($inlist$) group by a.code";

	private final List<String> res;
	private final String query;

	public PeptideTranslationAttribs() {
		super();
		res = resourceToStringList("/org/ensembl/healthcheck/testcase/eg_core/peptide_translation_attribs.txt");
		query = TemplateBuilder.template(QUERY, "inlist", listToInList(res));
	}
	

	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		Map<String, String> attribs = getTemplate(dbre).queryForMap(query,
				singleValueMapper);
		for (String exp : res) {
			String cnt = attribs.get(exp);
			if (StringUtils.isEmpty(cnt) || !StringUtils.isNumeric(cnt)
					|| Integer.parseInt(cnt) == 0) {
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"Translation attrib "
										+ exp
										+ " not found - please run misc-scripts/translation_attribs.pl");
				passes = false;
			}
		}
		return passes;
	}
}
