package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.AbstractTemplatedTestCase;
import org.ensembl.healthcheck.testcase.EnsTestCase;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.SqlUncheckedException;
import org.ensembl.healthcheck.util.SqlTemplate.ResultSetCallback;


/**
 * 
 * Todo: Make it able to deal with multi species databases.
 * 
 * @author mnuhn
 *
 */
public class SeqRegionsConsistentWithComparaMaster extends AbstractTemplatedTestCase {
	
	protected Connection testDbConn;
	protected SqlTemplate sqlTemplateTestDb;
	
	protected Connection masterDbConn;
	protected SqlTemplate masterSqlTemplate;
	
	protected void init(DatabaseRegistryEntry dbre) {
		
		testDbConn   = dbre.getConnection();
		sqlTemplateTestDb = getSqlTemplate(testDbConn);
		DatabaseRegistryEntry masterDbRe = getComparaMasterDatabase();
		masterDbConn = masterDbRe.getConnection();
		masterSqlTemplate = getSqlTemplate(masterDbConn);
	}

	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {
		
		init(dbre);
		
		String productionName     = fetchSingleMetaValueFor(sqlTemplateTestDb, "species.production_name");
		String assemblyDefault    = fetchSingleMetaValueFor(sqlTemplateTestDb, "assembly.default");
		String genebuildStartDate = fetchSingleMetaValueFor(sqlTemplateTestDb, "genebuild.start_date");
		
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
					+ "master database.");
		}
		
		boolean allToplevelSeqRegionInDnaFragTable = assertToplevelSeqRegionInDnaFragTable(genomeDbId);
		
		return allToplevelSeqRegionInDnaFragTable;
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
	 * @param masterDbRe
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
		throw new RuntimeException("Unexpected number of matching rows for " + productionName + "in master database!");			
	}
	
	protected ResultSet fetchFromGenomeDbId(
			String productionName, 
			String assemblyDefault,
			String genebuildStartDate, 
			String column
	) {
		String sql = "select "+column+" from genome_db where name=? and assembly=? and genebuild=?";
		
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
	 * @return
	 */
	protected String fetchSingleMetaValueFor(final SqlTemplate sqlTemplateTestDb,
			String metaKey) {
		
		List<String> metaValueList = sqlTemplateTestDb.queryForDefaultObjectList(
				"select meta_value from meta where meta.meta_key = '"+metaKey+"';", String.class);
		
		if (metaValueList.size()>1) {
			throw new RuntimeException("Got more than one meta_value for metaKey "+metaKey+". Expected only one!");
		}
		if (metaValueList.size()==0) {
			throw new RuntimeException("Metakey "+metaKey+" is missing in the meta table!");
		}
		
		String metaValue = metaValueList.get(0);
		
		return metaValue;
	}

}
