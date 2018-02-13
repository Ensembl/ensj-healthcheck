/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2018] EMBL-European Bioinformatics Institute
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

package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.RowMapper;

/**
 * Test case for testing whether ENA-specific analysis types have been left in a
 * core database
 * 
 * @author dstaines
 * 
 */
public class EnaAnalysisTypes extends AbstractEgCoreTestCase {

	private final static String TEST_SQL = "select a.logic_name, "
			+ "ad.description, ad.display_label, ad.web_data "
			+ "from analysis a join analysis_description ad using (analysis_id) "
			+ "where a.logic_name like '%ena%' or ad.description like '%ena%' "
			+ "or ad.display_label like '%ena%' or ad.web_data like '%ena%'";

	public EnaAnalysisTypes() {
		super();
	}

	@Override
	protected String getEgDescription() {
		return "Test for whether ENA-specific analysis types remain";
	}

	@Override
	protected boolean runTest(final DatabaseRegistryEntry dbre) {
		final Pattern enaPattern = Pattern.compile(".*ena.*",
				Pattern.CASE_INSENSITIVE);
		final EnsTestCase test = this;
		List<String> failures = getTemplate(dbre).queryForList(TEST_SQL,
				new RowMapper<String>() {
					@Override
					public String mapRow(ResultSet resultSet, int position)
							throws SQLException {
						String logicName = resultSet.getString(1);
						String description = resultSet.getString(2);
						String displayLabel = resultSet.getString(3);
						String webData = resultSet.getString(4);
						if (!StringUtils.isEmpty(logicName)
								&& enaPattern.matcher(logicName).matches()) {
							ReportManager.problem(
									test,
									dbre.getConnection(),
									"Analysis "
											+ logicName
											+ " logic_name contains the string 'ena'");
						}
						if (!StringUtils.isEmpty(description)
								&& enaPattern.matcher(description).matches()) {
							ReportManager.problem(
									test,
									dbre.getConnection(),
									"Analysis "
											+ logicName
											+ " description contains the string 'ena': "
											+ description);
						}
						if (!StringUtils.isEmpty(displayLabel)
								&& enaPattern.matcher(displayLabel).matches()) {
							ReportManager.problem(
									test,
									dbre.getConnection(),
									"Analysis "
											+ logicName
											+ " display_Label contains the string 'ena': "
											+ displayLabel);
						}
						if (!StringUtils.isEmpty(webData)
								&& enaPattern.matcher(webData).matches()) {
							ReportManager.problem(
									test,
									dbre.getConnection(),
									"Analysis "
											+ logicName
											+ " web_Data contains the string 'ena': "
											+ webData);
						}
						return logicName;
					}
				});
		return failures.size() == 0;
	}

}
