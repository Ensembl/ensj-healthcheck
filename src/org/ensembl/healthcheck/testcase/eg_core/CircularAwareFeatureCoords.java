package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.generic.FeatureCoords;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.TemplateBuilder;

public class CircularAwareFeatureCoords extends FeatureCoords {

	public final static String START_END_SQL = "SELECT COUNT(*) FROM $tableName$ " + "WHERE seq_region_start > seq_region_end and " + "seq_region_id not in "
			+ "(select seq_region_id from seq_region_attrib " + "join attrib_type using (attrib_type_id) " + "where code = 'circular_seq' and value=1)";

	public CircularAwareFeatureCoords() {
	
		super();
		removeFromAllGroups();
		
	}
	
	@Override
	protected boolean checkStartEnd(DatabaseRegistryEntry dbre, String tableName) {
		Connection con=dbre.getConnection(); 
		String sql = TemplateBuilder.template(START_END_SQL, "tableName", tableName);
		boolean result = true;
		// ------------------------
		logger.info("Checking " + tableName + " for start > end");
		int rows = DBUtils.getRowCount(con, sql);
		if (rows > 0) {
			ReportManager.problem(this, con, rows + " rows in " + tableName + " have seq_region_start > seq_region_end");
			result = false;
		} else {
			ReportManager.correct(this, con, "All rows in " + tableName + " have seq_region_start < seq_region_end");
		}
		return result;
	}

}
