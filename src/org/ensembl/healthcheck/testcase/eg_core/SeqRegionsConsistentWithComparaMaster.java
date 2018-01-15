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
 * Checks whether all toplevel sequences in a core database are present as 
 * dnafrag regions in the compara master database, if the genome is in 
 * the compara master database.
 * 
 * @author mnuhn
 *
 */
public class SeqRegionsConsistentWithComparaMaster extends AbstractControlledRows {

	final int reportMaxMissingRows = 20;
	
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
		
		List<Integer> allSpeciesIds = sqlTemplateTestDb.queryForDefaultObjectList(
			"select distinct species_id from meta where species_id is not null", 
			Integer.class
		);	

		if (allSpeciesIds.size() == 0) {
			ReportManager.problem(this, testDbConn, "No species configured!");
		}
		
		boolean allSpeciesPassed = true;
		
		for(int speciesId : allSpeciesIds) {
			allSpeciesPassed &= runTestForSpecies(dbre, speciesId);
		}
		
		return allSpeciesPassed;
	}
	
	protected boolean runTestForSpecies(DatabaseRegistryEntry dbre, int speciesId) {
		
		String productionName     = fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "species.production_name");
		String assemblyDefault    = fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "assembly.default");
		String genebuildStartDate = fetchSingleMetaValueFor(sqlTemplateTestDb, speciesId, "genebuild.start_date");
		
		if (!speciesConfiguredForDnaCompara(productionName)) {
			getLogger().info("Skipping species " + productionName + ", because it is not linked to any method involving DNA comparisons in the compara master.");
			return true;
		}
		
		getLogger().info("Testing species " + productionName);
		
		boolean hasEntryInMasterDb = fetchHasGenomeDbId(
				productionName, 
				assemblyDefault,
				genebuildStartDate 
		);
		
		if (!hasEntryInMasterDb) {
			ReportManager.correct(this, testDbConn, "Species " + productionName + " has no genome_db entry in the master database.");
			return true;
		}

		int genomeDbId = fetchGenomeDbId(
				productionName, 
				assemblyDefault,
				genebuildStartDate
		);
		
		int toplevelSeqRegionCount = fetchToplevelSeqRegionCount();
		int dnaFragRowCountFor     = fetchDnaFragRowCountFor(genomeDbId);
		
		boolean sequenceCountsOk = toplevelSeqRegionCount == dnaFragRowCountFor;
		
		if (sequenceCountsOk) {
			ReportManager.correct(this, testDbConn, "Sequence counts for this "
					+ "species are " + toplevelSeqRegionCount + " both in "
					+ "core and compara master database.");
		} else {
			ReportManager.problem(this, testDbConn, "Sequence counts for this "
					+ "species are " + toplevelSeqRegionCount + " toplevel "
					+ "sequence regions in the core database and " 
					+ dnaFragRowCountFor + " dna frags in the compara "
					+ "master database. The counts should be equal.\n"
					+ "This can happen, if the assembly has been changed, "
					+ "but the assembly.default entry in the meta table has "
					+ "not been changed. In that case the dna_frag table in "
					+ "the compara master database is not updated "
					+ "by the populate_mdb.pl script."
			);
		}
		
		boolean allToplevelSeqRegionInDnaFragTable = assertToplevelSeqRegionInDnaFragTable(genomeDbId);
		
		return sequenceCountsOk && allToplevelSeqRegionInDnaFragTable;
	}
	
	protected int fetchToplevelSeqRegionCount() {
		
		List<Integer> numSeqRegionsList = sqlTemplateTestDb.queryForDefaultObjectList(
				"select count(*) from seq_region join seq_region_attrib using (seq_region_id) join attrib_type using (attrib_type_id) where code='toplevel'", Integer.class);
		
		assertLengthIsOne(numSeqRegionsList);
		Integer numSeqRegions = numSeqRegionsList.get(0);		
		return numSeqRegions;
	}

	protected class SeqRegionData {
		
		public int seq_region_id;
		public String seq_region_name;
		public int seq_region_length;
		public String coord_system_name;
		
		public String toString() {
			return 
				"seq_region_id = " + seq_region_id + "\n"
				+ "seq_region.name = " + seq_region_name + "\n"
				+ "seq_region.length = " + seq_region_length + "\n"
				+ "coord_system.name = " + coord_system_name
			;
		}
	}
	
	protected boolean assertToplevelSeqRegionInDnaFragTable(final int genomeDbId) {
		
		final EnsTestCase thisTest = this;
		
		Boolean allRowsExistInDnaFragTable = sqlTemplateTestDb.execute(
			"select" 
			+ "	seq_region.seq_region_id, "
			+ "	seq_region.name, "
			+ "	seq_region.length, "
			+ "	coord_system.name "
			+ "from  "
			+ "	seq_region join seq_region_attrib using (seq_region_id) " 
			+ "	join attrib_type using (attrib_type_id)  "
			+ "	join coord_system using (coord_system_id)  "
			+ "where "
			+ "code='toplevel' ", 
			new ResultSetCallback<Boolean>() {

				@Override
				public Boolean process(ResultSet rs)
						throws SQLException {
					
					SeqRegionData seqRegionData = new SeqRegionData();
					
					int missingRows = 0;					
					
					boolean allRowsExistInDnaFragTable = true;
					
					while (rs.next()) {					
						seqRegionData.seq_region_id     = rs.getInt(1);
						seqRegionData.seq_region_name   = rs.getString(2);
						seqRegionData.seq_region_length = rs.getInt(3);
						seqRegionData.coord_system_name = rs.getString(4);
						
						int numCorrespondingRowsInDnaFragTable = fetchNumCorrespondingRowsInDnaFragTable(
								seqRegionData,
								genomeDbId
							);
						
						boolean currentRowExistsInDnaFragTable = false;
						
						if (numCorrespondingRowsInDnaFragTable == 1) {
							currentRowExistsInDnaFragTable = true;
						}
						if (numCorrespondingRowsInDnaFragTable == 0) {
							
							ReportManager.problem(thisTest, testDbConn, "The following seq region is not in the dnafrag table in the master database:\n" + seqRegionData);
							
							ReportManager.problem(thisTest, testDbConn, "The seq region that comes up with this sql in the core database:\n\n"
									+ createUsefulSqlCore(seqRegionData) + "\n\n"
									+ "should come up with this sql:\n\n"
									+ createUsefulSqlMaster(seqRegionData, genomeDbId)
							);
							
							missingRows++;
							if (missingRows>=reportMaxMissingRows) {
								ReportManager.problem(thisTest, testDbConn, "No more rows will be reported, because the maximum of " + reportMaxMissingRows + " has been reached.");
								return false;
							}
							currentRowExistsInDnaFragTable = false;
						}
						if (numCorrespondingRowsInDnaFragTable > 1) {
							throw new RuntimeException("Unexpected value for numCorrespondingRowsInDnaFragTable:" + numCorrespondingRowsInDnaFragTable);
						}
						
						allRowsExistInDnaFragTable &= currentRowExistsInDnaFragTable;
					}
					
					return allRowsExistInDnaFragTable;
				}
			},
			// No bound parameters
			//
			new Object[0]
		);		
		return allRowsExistInDnaFragTable;
	}
	
	protected String createUsefulSqlMaster(final SeqRegionData seqRegionData, final int genomeDbId) {
		return "select * "
			+ "from dnafrag "
			+ "where genome_db_id = "+genomeDbId+" "
			+ "and name = '" + seqRegionData.seq_region_name + "' "
			+ "and length = " + seqRegionData.seq_region_length + " "
			+ "and coord_system_name='" + seqRegionData.coord_system_name + "'";
	}

	protected String createUsefulSqlCore(final SeqRegionData seqRegionData) {
		return "select \n"
		+  "	seq_region.seq_region_id, \n"
		+  "	seq_region.name, \n"
		+  "	seq_region.length, \n"
		+  "	coord_system.name \n"
		+  "from  \n"
		+  "	seq_region join seq_region_attrib using (seq_region_id) \n" 
		+  "	join attrib_type using (attrib_type_id)  \n"
		+  "	join coord_system using (coord_system_id)  \n"
		+  "where  \n"
		+  "	code='toplevel' \n"
		+  "	and seq_region_id="+seqRegionData.seq_region_id+"; \n"
		;
	}

	protected int fetchNumCorrespondingRowsInDnaFragTable(
			final SeqRegionData seqRegionData,
			final int genomeDbId
		) {
		List<Integer> numSeqRegionsInDnaFragTableList = 
				masterSqlTemplate.queryForDefaultObjectList(
					  "select " 
					+ "  count(*) "
					+ "from "
					+ "	 dnafrag " 
					+ "where "
					+ "  genome_db_id=?"
					+ "  and name=?"
					+ "  and length=?"
					+ "  and coord_system_name=?", 
					Integer.class,
					genomeDbId,
					seqRegionData.seq_region_name,
					seqRegionData.seq_region_length,
					seqRegionData.coord_system_name
		);
		assertLengthIsOne(numSeqRegionsInDnaFragTableList);
		
		return numSeqRegionsInDnaFragTableList.get(0);		
	}

	protected void assertLengthIsOne(List<?> list) {

		if (list.size()>1) {
			throw new RuntimeException("Got more than one return value. Expected only one!");
		}
		if (list.size()==0) {
			throw new RuntimeException("Got no return value!");
		}
	}
	
	protected int fetchDnaFragRowCountFor(int genomeDbId) {
		
		List<Integer> metaValueList = masterSqlTemplate.queryForDefaultObjectList(
				"select count(*) from dnafrag where genome_db_id=" + genomeDbId, Integer.class);
		assertLengthIsOne(metaValueList);
		return metaValueList.get(0);
	}

	/**
	 * @param productionName
	 * @param assemblyDefault
	 * @param genebuildStartDate
	 */
	protected int fetchGenomeDbId(
			String productionName, String assemblyDefault,
			String genebuildStartDate) {
		
		ResultSet rs = fetchFromGenomeDbId(productionName, assemblyDefault, genebuildStartDate, "genome_db_id");
		
		int genomeDbId;
		
		try {
			boolean hasResult = rs.next();

			if (!hasResult) {
				throw new RuntimeException("Can't fetch Species " + productionName + " from genome_db table!");
			}			
			genomeDbId = rs.getInt(1);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
		return genomeDbId;			
	}

	/**
	 * Check in master database, if there is a genome_db entry for this 
	 * species.
	 */
	protected boolean fetchHasGenomeDbId(
			String productionName, String assemblyDefault,
			String genebuildStartDate) {
		
		ResultSet rs = fetchFromGenomeDbId(productionName, assemblyDefault, genebuildStartDate, "count(genome_db_id)");
		
		int genomeDbId;
		
		try {
			boolean hasResult = rs.next();

			if (!hasResult) {
				throw new RuntimeException("Can't count rows on genome_db table!");
			}			
			genomeDbId = rs.getInt(1);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}		
		if (genomeDbId==1) {
			return true;
		}		
		if (genomeDbId==0) {
			return false;
		}		
		throw new RuntimeException("Unexpected number of matching rows for " + productionName + " in master database!");			
	}
	
	protected ResultSet fetchFromGenomeDbId(
			String productionName, 
			String assemblyDefault,
			String genebuildStartDate, 
			String column
	) {
		String sql = "select "+column+" from genome_db where name=? and assembly=? and genebuild=? and genome_component IS NULL";
		
		ResultSet rs = null;
		
		try {
			
			PreparedStatement stmt = this.masterDbConn.prepareStatement(sql);
			
			stmt.setString(1, productionName);
			stmt.setString(2, assemblyDefault);
			stmt.setString(3, genebuildStartDate);
			
			rs = stmt.executeQuery();
						
		} catch (SQLException e) {
			
			throw new SqlUncheckedException(e.getMessage());
		}
		return rs;
	}
	
	/**
	 * @param sqlTemplateTestDb
	 * @param metaKey
	 * @return metaValue
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
		if (metaValueList.size()==0) {
			throw new RuntimeException("Metakey "+metaKey+" is missing in the meta table!\n"+sql);
		}
		
		String metaValue = metaValueList.get(0);
		
		return metaValue;
	}
}
