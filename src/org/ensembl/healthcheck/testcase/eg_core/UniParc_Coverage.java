/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2019] EMBL-European Bioinformatics Institute
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

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;

/**
 * test for minimal level of uniprot coverage to ensure all cases are checked
 * manually
 * 
 * @author dstaines
 * 
 */
public class UniParc_Coverage extends AbstractTemplatedTestCase {

	public UniParc_Coverage() {
		super();
		this.addToGroup(AbstractEgCoreTestCase.EG_GROUP);
		this.appliesToType(DatabaseType.CORE);
		this.setTeamResponsible(Team.ENSEMBL_GENOMES);
	}

	private final static double THRESHOLD = 100.0;

	private final static String QUERY_UNIPARC = "SELECT count(distinct(g.gene_id)) "
			+ "FROM gene g join transcript t using (gene_id) "
			+ "join translation tl using (transcript_id) "
			+ "join object_xref ox on (tl.translation_id=ox.ensembl_id and ox.ensembl_object_type='Translation') "
			+ "join xref x using (xref_id) join external_db d using (external_db_id) "
			+ "join seq_region s on (s.seq_region_id=g.seq_region_id) "
			+ "join coord_system using (coord_system_id) "
			+ "WHERE g.biotype='protein_coding' "
			+ "AND d.db_name ='UniParc' "
			+ "and species_id=?";

	private final static String QUERY_GENES = "select count(*) from gene "
			+ "join seq_region using (seq_region_id) "
			+ "join coord_system using (coord_system_id) where biotype='protein_coding' and species_id=?";

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		boolean result = true;
		SqlTemplate template = getSqlTemplate(dbre);
		for (int speciesId : dbre.getSpeciesIds()) {
			int nProteinCoding = template.queryForDefaultObject(QUERY_GENES,
					Integer.class, speciesId);
			if (nProteinCoding == 0) {
				ReportManager.problem(this, dbre.getConnection(),
						"No protein coding genes found!");
				result = false;
				continue;
			}
			int nUniParc = template.queryForDefaultObject(QUERY_UNIPARC,
					Integer.class, speciesId);
			double ratio = (100.0 * nUniParc) / nProteinCoding;
			if (ratio < THRESHOLD) {
				ReportManager
						.problem(
								this,
								dbre.getConnection(),
								"Less than "
										+ THRESHOLD
										+ "% of protein_coding genes for species "
										+ speciesId
										+ " have a UniParc xref ("
										+ nUniParc
										+ "/"
										+ nProteinCoding
										+ "): this may be correct for some genomes so please check and annotate accordingly");
				result = false;
			}
		}
		return result;
	}

}
