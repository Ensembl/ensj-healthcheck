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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.testcase.eg_compara.AbstractControlledRows;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;
import org.ensembl.healthcheck.util.SqlTemplate.ResultSetCallback;

/**
 * 
 * If the genome is polyploid (ie if the meta key 'ploidy' is set to more than 2), checks whether all toplevel sequences in a core database 
 * have a seq_region_attrib with attrib_type 'genome_component' associated with them. Basically, it tells which genome component, this seq_region belongs to.
 * 
 * @author arnaud
 *
 */
public class SeqRegionAttribForPolyploidGenome extends AbstractControlledRows {

	final int reportMaxMissingRows = 1;
	
	protected Connection testDbConn;
	protected SqlTemplate sqlTemplateTestDb;
	
	protected void init(DatabaseRegistryEntry dbre) {
		
		super.init();
		
		testDbConn   = dbre.getConnection();
		sqlTemplateTestDb = getSqlTemplate(testDbConn);
		
		setTeamResponsible(Team.ENSEMBL_GENOMES);
	}
	

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		init(dbre);
		int speciesId = 1;
		
		if (fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "ploidy") == null) {
			// ploidy is not defined, fine as this meta_key is optional
			// In that case, we don't need to perform this test
			return true;
		}
		
		int ploidy = 0;
		try {
			ploidy = Integer.parseInt(fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "ploidy"));
		}
		catch (NumberFormatException nfe) {
			ReportManager.problem(this, testDbConn, "Value for 'ploidy' meta_key is not numerical, " + fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "ploidy"));
			return false;
		}	

		boolean allSpeciesPassed = true;
		
		boolean isPolyploid = false;
		if (ploidy > 2) {
			isPolyploid = true;
		}
		if (isPolyploid == true) {
			allSpeciesPassed &= runTestForSpecies(dbre, speciesId);
		}
		
		return allSpeciesPassed;
	}
	
	protected boolean runTestForSpecies(DatabaseRegistryEntry dbre, int speciesId) {
				
		int toplevelSeqRegionCount = fetchToplevelSeqRegionCount();
		int componentGenomeSetCount = fetchComponentGenomeCount();
		
		boolean sequenceCountsOk = toplevelSeqRegionCount == componentGenomeSetCount;
		
		if (sequenceCountsOk) {
			ReportManager.correct(this, testDbConn, "All " + toplevelSeqRegionCount + " toplevel sequences have a genome_component attrib set "
					+ "for this polyploid species");
		} else {
			ReportManager.problem(this, testDbConn, "Sequence counts for this "
					+ "polyploid species are " + toplevelSeqRegionCount + " toplevel "
					+ "sequence regions in the core database and " 
					+ componentGenomeSetCount + " sequences associated with a genome component. "
					+ "The counts should be equal.\n"
			);
		}
		
		return sequenceCountsOk;
	}
	
	protected int fetchToplevelSeqRegionCount() {
		
		List<Integer> numSeqRegionsList = sqlTemplateTestDb.queryForDefaultObjectList(
				"select count(*) from seq_region join seq_region_attrib using (seq_region_id) join attrib_type using (attrib_type_id) where code='toplevel'", Integer.class);
		
		assertLengthIsOne(numSeqRegionsList);
		Integer numSeqRegions = numSeqRegionsList.get(0);
		return numSeqRegions;
	}

	protected int fetchComponentGenomeCount() {
		
		List<Integer> numSeqRegionsWithGenomeComponentAttribList = sqlTemplateTestDb.queryForDefaultObjectList(
				"select count(distinct seq_region.seq_region_id) from seq_region join seq_region_attrib sa1 using (seq_region_id) join attrib_type a1 on sa1.attrib_type_id = a1.attrib_type_id join seq_region_attrib sa2 using (seq_region_id) join attrib_type a2 on sa2.attrib_type_id = a2.attrib_type_id where a1.code='toplevel' and a2.code='genome_component'", Integer.class);
		
		assertLengthIsOne(numSeqRegionsWithGenomeComponentAttribList);
		Integer numSeqRegions = numSeqRegionsWithGenomeComponentAttribList.get(0);
		return numSeqRegions;
	}

	/**
	 * @param sqlTemplateTestDb
	 * @param metaKey
	 * @return meta value
	 */
	protected String fetchSingleMetaValueFor(
			final SqlTemplate sqlTemplateTestDb,
			int speciesId,
			String metaKey
		) {
		
		String sql = "select meta_value from meta where meta.meta_key = '"+metaKey+"' and species_id="+speciesId;
		
		List<String> metaValueList = sqlTemplateTestDb.queryForDefaultObjectList(
				sql, String.class
		);
		
		if (metaValueList.size()>1) {
			throw new RuntimeException("Got more than one meta_value for metaKey "+metaKey+". Expected only one!\n"+sql);
		}
		String metaValue = null;
		if (metaValueList.size()==0) {
			// throw new RuntimeException("Metakey "+metaKey+" is missing in the meta table!\n"+sql);
			// that's fine as the met_key we are looking for is optional
		}
		else {
			metaValue = metaValueList.get(0);
		}
		
		return metaValue;
	}

	protected void assertLengthIsOne(List<?> list) {

		if (list.size()>1) {
			throw new RuntimeException("Got more than one return value. Expected only one!");
		}
		if (list.size()==0) {
			throw new RuntimeException("Got no return value!");
		}
	}

}
