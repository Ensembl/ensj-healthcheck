/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2020] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * File: GenesDisplayable.java
 * Created by: dstaines
 * Created on: Feb 9, 2010
 * CVS:  $$
 */
package org.ensembl.healthcheck.testcase.eg_core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.CollectionUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.TemplateBuilder;
import org.ensembl.healthcheck.util.TestCaseUtils;

/**
 * 
 * @author dstaines
 */
public class GenesDisplayable extends AbstractEgCoreTestCase {

	private static Pattern DEF_PATTERN = Pattern
			.compile(".*'?default'? *=> *\\{([^}]+)\\}.*");

	private static Pattern KEY_VAL_PATTERN = Pattern
			.compile("\\s*'?(\\w+)'?\\s*=>\\s*'?(\\w+)'?\\s*");

	private static Map<String, String> DEF_REF = parseDefault("'default' => {'MultiTop' => 'gene_label','contigviewbottom' => 'transcript_label','MultiBottom' => 'collapsed_label','contigviewtop' => 'gene_label','alignsliceviewbottom' => 'as_collapsed_label','cytoview' => 'gene_label'}");

	private static final String QUERY = "select logic_name,web_data from $table$ "
			+ "join analysis using (analysis_id) "
			+ "left join analysis_description using (analysis_id) group by logic_name,web_data";

	private static final String[] TABLES = { "gene", "transcript" };

	public static final void main(String[] args) {
		String v = "'default' => {'MultiTop' => 'gene_label','contigviewbottom' => 'transcript_label','MultiBottom' => 'collapsed_label','contigviewtop' => 'gene_label','alignsliceviewbottom' => 'as_collapsed_label','cytoview' => 'gene_label'}";
		parseDefault(v);
	}

	public static Map<String, String> parseDefault(String webdata) {
		Map<String, String> defaults = CollectionUtils.createHashMap();
		Matcher matcher = DEF_PATTERN.matcher(webdata);
		if (matcher.matches()) {
			String defVal = matcher.group(1);
			for (String pair : defVal.split(",")) {
				Matcher m = KEY_VAL_PATTERN.matcher(pair);
				if (m.matches()) {
					defaults.put(m.group(1), m.group(2));
				}
			}
		}
		return defaults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.healthcheck.testcase.eg_core.AbstractEgCoreTestCase#runTest
	 * (org.ensembl.healthcheck.DatabaseRegistryEntry)
	 */
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		SqlTemplate template = getTemplate(dbre);
		for (String table : TABLES) {
			Map<String, String> webdatas = template.queryForMap(TemplateBuilder
					.template(QUERY, "table", table), TestCaseUtils.singleValueMapper);
			if (webdatas.size() == 0) {
				ReportManager.problem(this, dbre.getConnection(),
						"No analysis types found for table " + table);
				passes = false;
			} else {
				for (Entry<String, String> e : webdatas.entrySet()) {
					String logicName = e.getKey();
					String webdata = e.getValue();
					if (webdata == null) {
						ReportManager.problem(this, dbre.getConnection(),
								"No web data value found for analysis type "
										+ logicName + " table " + table);
						passes = false;
					} else {
						Map<String, String> defs = parseDefault(webdata);
						for (Entry<String, String> e2 : DEF_REF.entrySet()) {
							if (!e2.getValue().equals(defs.get(e2.getKey()))) {
								ReportManager
										.problem(
												this,
												dbre.getConnection(),
												"Default entry for analysis type "
														+ logicName
														+ " table "
														+ table
														+ " does not contain the value "
														+ e2.getKey() + "=>"
														+ e2.getValue() + ":"
														+ webdata);
								passes = false;
							}
						}
					}
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
		return "Check to ensure that gene analysis is displayed";
	}

}
