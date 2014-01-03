package org.ensembl.healthcheck.testcase.eg_core;

import java.sql.Connection;

import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.DatabaseType;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;

public class SimpleFeatureEnds extends SingleDatabaseTestCase {

	private static int THRESHOLD = 0;

	public SimpleFeatureEnds() {
		setDescription("Check that simple feature co-ordinates make sense");
	}

	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		Connection con = dbre.getConnection();

		logger.fine("Checking simple features for " + DBUtils.getShortDatabaseName(con) + " ...");
		int rows = DBUtils.getRowCount(con, "SELECT COUNT(*) FROM simple_feature INNER JOIN seq_region USING (seq_region_id) WHERE seq_region_end > length");
		if (rows > THRESHOLD) {
			result = false;
			ReportManager.problem(this, con, rows + " simple features have seq_region_end > seq_region.length");
		}

		return result;
	}
}
