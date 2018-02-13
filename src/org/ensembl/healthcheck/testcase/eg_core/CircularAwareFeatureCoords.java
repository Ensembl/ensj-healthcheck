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
