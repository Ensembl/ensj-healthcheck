package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.util.DBUtils;

/**
 * @author mnuhn
 * 
 * <p>
 * 	Checks that the length given in seq_region is the length of the sequence
 * in the dna table.
 * </p>
 *
 */
public class SeqRegionLength extends AbstractEgCoreTestCase {
	
	private final String sql_find_wrong_lengths 
	
		= "select seq_region_id, name, length(sequence) as len, length "
		+ "from dna join seq_region using (seq_region_id) "
		+ "where length(sequence) != length"
	;

	private final String error_msg 
	
		= "Some seq region lengths do not match the actual length of the "
		+ "sequence stored in the dna table. The ones found by this test can "
		+ "be corrected like this:"
	;
	
	@Override
	protected boolean runTest(DatabaseRegistryEntry dbre) {

		Connection con = dbre.getConnection();
		
		int numRows = DBUtils.getRowCount(con, sql_find_wrong_lengths);
		
		boolean passed = numRows==0; 
		
		if (!passed) {

			ReportManager.problem(this, con, error_msg + "\n");
			
			try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql_find_wrong_lengths);
				if (rs != null) {
					
					while (rs.next()) {
						
						ReportManager.problem(
							this, 
							con, 
							"update seq_region set length=" 
							+ rs.getInt("len") 
							+ " where seq_region_id=" 
							+ rs.getInt("seq_region_id")
							+ ";"
						);
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
