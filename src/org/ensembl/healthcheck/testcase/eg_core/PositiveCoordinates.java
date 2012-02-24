package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Checks that that start and end coordinates of genes, transcripts, 
 * translations and the other tables specified in the attribute "table" are 
 * positive integers.
 * </p>
 *
 */
public class PositiveCoordinates extends AbstractEgCoreTestCase {
	
	protected String[] table = { 
		"gene", 
		"transcript", 
		"translation",
		"protein_feature",
		"simple_feature",
		"exon",
		"dna_align_feature",
		"density_feature",
		"marker_feature",
		"misc_feature",
		"operon",
		"operon_transcript",
		"prediction_exon",
		"prediction_transcript",
		"protein_align_feature",
		"repeat_feature"
	};
	
	Set<String> just_seq_tables = new HashSet<String>() {{
		add("translation");
		add("protein_feature");
	}};
	
	protected String create_sql_for_table(String table) {
		
		String tableNamePrefix = "seq_region";
		
		if (just_seq_tables.contains(table)) {
			tableNamePrefix = "seq";
		}
		
		String sql 
			= "select count(*) as c "
			+ "from " + table 
			+ " where "+tableNamePrefix+"_start<1 or "+tableNamePrefix+"_end<1;"
		;
		return sql;
	}
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		
		boolean passed = true; 

		for (String currentTable : table) {
			
			String currentSql = create_sql_for_table(currentTable);
			
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(currentSql);
				if (rs != null) {
					
					while (rs.next()) {
						
						int numProblemRows = rs.getInt("c");
						
						if (numProblemRows>0) {
						
							ReportManager.problem(
									this, 
									con, 
									"The table " + currentTable + " has " 
									+ numProblemRows + " rows in which the start or "
									+ "end coordinates are not positive integers."
								);
							ReportManager.problem(
									this, 
									con, 
									"USEFUL SQL: " + currentSql
								);
							passed = false;
						}
					}
				}
				rs.close();
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return passed;
	}
}
