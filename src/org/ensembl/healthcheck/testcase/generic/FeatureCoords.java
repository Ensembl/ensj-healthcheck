/*
 * Copyright [1999-2015] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * Copyright [2016-2021] EMBL-European Bioinformatics Institute
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


package org.ensembl.healthcheck.testcase.generic;

import java.sql.Connection;
import java.util.Set;
import java.util.Map;
import java.util.logging.Logger;



import org.ensembl.healthcheck.DatabaseRegistryEntry;
import org.ensembl.healthcheck.ReportManager;
import org.ensembl.healthcheck.Team;
import org.ensembl.healthcheck.testcase.SingleDatabaseTestCase;
import org.ensembl.healthcheck.util.DBUtils;
import org.ensembl.healthcheck.util.SqlTemplate;
import org.ensembl.healthcheck.util.Utils;
import org.ensembl.healthcheck.util.RowMapper;
import org.ensembl.healthcheck.util.DefaultMapRowMapper;
import org.ensembl.healthcheck.util.DefaultObjectRowMapper;




/**
 * Check that feature co-ords make sense.
 */
public class FeatureCoords extends SingleDatabaseTestCase {

	/**
	 * Creates a new instance of CheckFeatureCoordsTestCase
	 */
	public FeatureCoords() {

		setDescription("Check that feature co-ords make sense.");
		setHintLongRunning(true);
		setTeamResponsible(Team.GENEBUILD);
	}

	public Map<String,Integer> seq_regions;
	
	/**
	 * Iterate over each affected database and perform various checks.
	 * 
	 * @param dbre
	 *            The database to check.
	 * @return True if the test passed.
	 */
	public boolean run(DatabaseRegistryEntry dbre) {

		boolean result = true;

		String[] featureTables = getCoreFeatureTables();
        Connection con = dbre.getConnection();
        SqlTemplate t = DBUtils.getSqlTemplate(con);
        String sql = "SELECT s.seq_region_id,s.length FROM seq_region s join seq_region_attrib a USING (seq_region_id) WHERE a.attrib_type_id = 6";
        DefaultMapRowMapper<String, Integer> mapper = new DefaultMapRowMapper<String, Integer>(String.class, Integer.class);
                             
        seq_regions = t.queryForMap(sql,mapper);
        
		for (int tableIndex = 0; tableIndex < featureTables.length; tableIndex++) {

			String tableName = featureTables[tableIndex];
                        result &= checkStart(dbre, tableName);
                        result &= checkStartEnd(dbre, tableName);
                        result &= checkLength(dbre, tableName);

		} // foreach table

		return result;

	} // run


      protected boolean checkLength(DatabaseRegistryEntry dbre, String tableName) {
        SqlTemplate t = DBUtils.getSqlTemplate(dbre);
        boolean result = true;
        if (tableName.equals("repeat_feature")) {
                return true;
        }
        String featureSQL = "SELECT seq_region_id, max(seq_region_start) from " + tableName + " group by seq_region_id ";
        DefaultMapRowMapper<Integer, Integer> feature_mapper = new DefaultMapRowMapper<Integer, Integer>(Integer.class, Integer.class);
        Map<Integer, Integer> featureResults = t.queryForMap(featureSQL, feature_mapper);

        
        for (Map.Entry<Integer, Integer> entry : featureResults.entrySet()) {
          Integer max = entry.getValue();
          Integer region = entry.getKey();
          if (seq_regions.containsKey(region) ) {
            Integer length = seq_regions.get(region);
            if (max > length) {
              ReportManager.problem(this, dbre.getConnection(), "Some features in " + tableName + " start on position " + max + " when region " + region + " is only " + length + " long");
              result = false;
            }
            
          }

        }
        return result;

      }


      protected boolean checkStart(DatabaseRegistryEntry dbre, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE seq_region_start < 1";
        int rows = DBUtils.getRowCount(dbre.getConnection(), sql);
        if (rows > 0) {
                ReportManager.problem(this, dbre.getConnection(), rows + " rows in " + tableName + " have seq_region_start < 1");
                return false;
         } else {
                return true;
         }

      }
	/**
	 * Subroutine to carry out a check on whether the start is after the end.
	 * This is to allow EG to skip this check for circular molecules
	 * 
	 * @param dbre
	 * @param tableName
	 * @return true if start is after end
	 */
	protected boolean checkStartEnd(DatabaseRegistryEntry dbre, String tableName) {
		String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE seq_region_start > seq_region_end";
		int rows = DBUtils.getRowCount(dbre.getConnection(), sql);;
		if (rows > 0) {
			ReportManager.problem(this, dbre.getConnection(), rows + " rows in " + tableName + " have seq_region_start > seq_region_end");
			return false;
		} else {
                        return true;
		}
	}

} // FeatureCoords
