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

import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * @author mnuhn Checks that repeat.analysis keys have been set in the meta
 *         table for DUST, REPEATMASK, TRF.
 */
public class RepeatAnalysesInMeta extends AbstractEgCoreTestCase {

	@Override
	protected String getEgDescription() {
		return "Make sure repeat.analysis has been set, if repeat analyses are present in the analysis table";
	}

	/**
	 * <p>
	 * The list of repeat analyses that we expect to see in meta, if they have
	 * been run.
	 * </p>
	 */
	private final static String[] ANALYSES = { "dust", "repeatmask", "trf" };

	private final static String ANALYSIS_SQL = "select count(*) from coord_system "
			+ "join seq_region using (coord_system_id) "
			+ "join repeat_feature using (seq_region_id) "
			+ "join analysis using (analysis_id) "
			+ "where logic_name=? and species_id=?";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean passes = true;
		final SqlTemplate sqlTemplate = DBUtils.getSqlTemplate(dbre);
		for (final int speciesId : dbre.getSpeciesIds()) {
			passes &= checkMetaEntriesForSpecies(dbre, sqlTemplate, speciesId);
		}
		return passes;
	}

	/**
	 * <p>
	 * For a given species id, checks that the meta table has the necessary
	 * repeat.analysis entries for all the repeatLogicNames in the meta table.
	 * </p>
	 * 
	 * @param speciesId
	 * @return
	 */
	private boolean checkMetaEntriesForSpecies(DatabaseRegistryEntry dbre,
			SqlTemplate template, int speciesId) {
		boolean currentSpeciesPasses = true;
		// which analysis types do we have?
		for (final String analysis : ANALYSES) {
			final int cnt = template.queryForDefaultObject(ANALYSIS_SQL,
					Integer.class, analysis, speciesId);
			if (cnt > 0) {
				final List<Integer> meta_id = template
						.queryForDefaultObjectList(
								"select meta_id from meta where lower(meta_value)=? and meta_key='repeat.analysis' and species_id=?",
								Integer.class, analysis, speciesId);

				if (meta_id.size() == 0) {
					currentSpeciesPasses = false;
					ReportManager
							.problem(
									this,
									dbre.getConnection(),
									analysis
											+ " is a logic name for repeats, but has not been declared in meta for species "
											+ speciesId
											+ ". "
											+ "Fix this by running "
											+ "insert into meta (species_id, meta_key, meta_value) values ("
											+ speciesId
											+ ", 'repeat.analysis', '"
											+ analysis + "')");
				}
			}
		}
		return currentSpeciesPasses;
	}
}
